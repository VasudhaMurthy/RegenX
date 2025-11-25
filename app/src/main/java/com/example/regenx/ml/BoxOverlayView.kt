package com.example.regenx.ml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
        textSize = 40f
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
        for (res in results) {
            val box = res.boundingBox
            canvas.drawRect(box, boxPaint)

            val label = "${res.label} ${(res.confidence * 100).toInt()}%"

            // Calculate text position
            // If box is at the very top, draw text INSIDE the box
            var textY = box.top - 10f
            if (textY < 50f) textY = box.top + 50f

            val textWidth = textPaint.measureText(label)
            canvas.drawRect(box.left, textY - 40f, box.left + textWidth + 20f, textY + 10f, bgPaint)
            canvas.drawText(label, box.left + 10f, textY, textPaint)
        }
    }
}