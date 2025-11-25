package com.example.regenx.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.example.regenx.ml.WasteDetector
import com.example.regenx.ml.BoxOverlayView
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private var detector: WasteDetector? = null
    private lateinit var overlayView: BoxOverlayView
    private lateinit var previewView: PreviewView
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup UI programmatically
        val container = FrameLayout(this)
        previewView = PreviewView(this)
        overlayView = BoxOverlayView(this, null)
        container.addView(previewView)
        container.addView(overlayView)
        setContentView(container)

        // 2. Init Detector Safely
        try {
            detector = WasteDetector(this)
        } catch (e: Exception) {
            Toast.makeText(this, "AI Init Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // 3. Check Permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCamera() else finish()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888) // Standard
                .build()

            analyzer.setAnalyzer(cameraExecutor) { proxy ->
                // Manual Conversion (Safe for all versions)
                val bitmap = proxy.toBitmap()
                if (bitmap != null && detector != null) {
                    // Rotate to Portrait
                    val rotation = proxy.imageInfo.rotationDegrees.toFloat()
                    val matrix = Matrix().apply { postRotate(rotation) }
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    val results = detector!!.detect(rotatedBitmap)

                    runOnUiThread { overlayView.setResults(results) }
                }
                proxy.close() // CRITICAL
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
            } catch (e: Exception) {
                Log.e("ReGenX", "Camera Fail", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Manual YUV -> Bitmap Helper
    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector?.close()
    }
}