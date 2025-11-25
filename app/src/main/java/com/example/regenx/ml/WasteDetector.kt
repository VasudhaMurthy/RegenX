package com.example.regenx.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import java.nio.FloatBuffer
import java.util.Collections
import kotlin.math.max
import kotlin.math.min

data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF // Stores Percentages (0.0 to 1.0)
)

class WasteDetector(context: Context) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var inputSize = 320
    private var inputName: String = "images"

    init {
        try {
            val modelBytes = context.assets.open("regenx_waste.onnx").use { it.readBytes() }
            session = env.createSession(modelBytes, OrtSession.SessionOptions())

            val inputInfo = session?.inputInfo
            if (inputInfo != null) {
                inputName = inputInfo.keys.first()
                val tensorInfo = inputInfo[inputName]?.info as? ai.onnxruntime.TensorInfo
                val shape = tensorInfo?.shape
                if (shape != null && shape.size == 4) {
                    inputSize = shape[3].toInt()
                    Log.i("ReGenX", "🔹 Model Loaded! Input Size: $inputSize")
                }
            }
        } catch (e: Exception) {
            Log.e("ReGenX", "Model Load Failed", e)
            session = null
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (session == null) return emptyList()

        try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val floatBuffer = convertBitmapToFloatBuffer(resizedBitmap)

            val inputTensor = OnnxTensor.createTensor(env, floatBuffer, longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong()))
            val results = session?.run(Collections.singletonMap(inputName, inputTensor))

            val outputTensor = results?.get(0) as? OnnxTensor
            val rawOutput = outputTensor?.floatBuffer
            val shape = outputTensor?.info?.shape

            val detections = if (rawOutput != null && shape != null) {
                val rawList = processOutput(rawOutput, shape)
                applyNMS(rawList)
            } else {
                emptyList()
            }

            inputTensor.close()
            results?.close()
            return detections

        } catch (e: Exception) {
            Log.e("ReGenX", "Detection Error", e)
            return emptyList()
        }
    }

    private fun convertBitmapToFloatBuffer(bitmap: Bitmap): FloatBuffer {
        val count = inputSize * inputSize
        val buffer = FloatBuffer.allocate(1 * 3 * count)
        buffer.rewind()
        val intValues = IntArray(count)
        bitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

        for (i in 0 until count) buffer.put(((intValues[i] shr 16) and 0xFF) / 255.0f)
        for (i in 0 until count) buffer.put(((intValues[i] shr 8) and 0xFF) / 255.0f)
        for (i in 0 until count) buffer.put((intValues[i] and 0xFF) / 255.0f)

        buffer.rewind()
        return buffer
    }

    private fun processOutput(buffer: FloatBuffer, shape: LongArray): List<DetectionResult> {
        val detections = ArrayList<DetectionResult>()

        val dim1 = shape[1].toInt()
        val dim2 = shape[2].toInt()
        val isTransposed = dim1 > dim2
        val numAnchors = if (isTransposed) dim1 else dim2
        val numClasses = (if (isTransposed) dim2 else dim1) - 4

        buffer.rewind()
        val data = FloatArray(buffer.remaining())
        buffer.get(data)

        for (i in 0 until numAnchors) {
            var maxScore = 0f
            var maxClass = -1
            for (c in 0 until numClasses) {
                val score = if (isTransposed) data[i * (numClasses + 4) + (4 + c)]
                else data[(4 + c) * numAnchors + i]
                if (score > maxScore) {
                    maxScore = score
                    maxClass = c
                }
            }

            if (maxScore > 0.40f) {
                val cx: Float; val cy: Float; val w: Float; val h: Float

                if (isTransposed) {
                    val offset = i * (numClasses + 4)
                    cx = data[offset]; cy = data[offset+1]; w = data[offset+2]; h = data[offset+3]
                } else {
                    cx = data[i]; cy = data[numAnchors + i]; w = data[2*numAnchors + i]; h = data[3*numAnchors + i]
                }

                // --- FORCE NORMALIZATION (0.0 to 1.0) ---
                // We divide by inputSize (320) to get a percentage.
                // Example: If box is at 160, 160/320 = 0.5 (50% of screen)

                val left = (cx - w/2) / inputSize
                val top = (cy - h/2) / inputSize
                val right = (cx + w/2) / inputSize
                val bottom = (cy + h/2) / inputSize

                detections.add(DetectionResult(
                    label = getLabelName(maxClass),
                    confidence = maxScore,
                    boundingBox = RectF(left, top, right, bottom)
                ))
            }
        }
        return detections
    }

    private fun applyNMS(detections: List<DetectionResult>): List<DetectionResult> {
        val sorted = detections.sortedByDescending { it.confidence }
        val selected = ArrayList<DetectionResult>()

        for (candidate in sorted) {
            var suppress = false
            for (existing in selected) {
                if (calculateIoU(candidate.boundingBox, existing.boundingBox) > 0.5f) {
                    suppress = true
                    break
                }
            }
            if (!suppress) selected.add(candidate)
        }
        return selected
    }

    private fun calculateIoU(a: RectF, b: RectF): Float {
        val intersection = RectF(max(a.left, b.left), max(a.top, b.top), min(a.right, b.right), min(a.bottom, b.bottom))
        if (intersection.left >= intersection.right || intersection.top >= intersection.bottom) return 0f
        val interArea = intersection.width() * intersection.height()
        val unionArea = (a.width() * a.height()) + (b.width() * b.height()) - interArea
        return interArea / unionArea
    }

    private fun getLabelName(index: Int): String {
        val labels = listOf("Biodegradable", "Cardboard", "Glass", "Metal", "Paper", "Plastic", "Trash")
        return if (index in labels.indices) labels[index] else "Item"
    }

    fun close() { session?.close(); env.close() }
}