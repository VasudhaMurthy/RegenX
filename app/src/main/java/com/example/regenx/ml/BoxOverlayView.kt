package com.example.regenx.ml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class BoxOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var results: List<DetectionResult> = emptyList()

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f // Bigger text
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        alpha = 180
        style = Paint.Style.FILL
    }

    fun setResults(newResults: List<DetectionResult>) {
        results = newResults
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        for (res in results) {
            // CONVERT PERCENTAGE TO SCREEN PIXELS
            // res.boundingBox contains values like 0.5 (50%)
            // We multiply by screen width/height to get actual pixels
            val left = res.boundingBox.left * w
            val top = res.boundingBox.top * h
            val right = res.boundingBox.right * w
            val bottom = res.boundingBox.bottom * h

            val box = RectF(left, top, right, bottom)

            // Draw the Box
            canvas.drawRect(box, boxPaint)

            // Draw the Text
            val label = "${res.label} ${(res.confidence * 100).toInt()}%"

            // Ensure text doesn't go off the top of the screen
            var textY = box.top - 15f
            if (textY < 60f) textY = box.top + 60f

            val textWidth = textPaint.measureText(label)

            // Draw background for text
            canvas.drawRect(
                box.left,
                textY - 50f,
                box.left + textWidth + 20f,
                textY + 10f,
                bgPaint
            )

            // Draw text on top of the bounding box
            canvas.drawText(label, box.left + 10f, textY, textPaint)
        }
    }
}