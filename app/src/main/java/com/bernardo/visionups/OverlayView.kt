package com.bernardo.visionups

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var results = listOf<DetectionResult>()
    private val paintBox = Paint().apply { color = Color.GREEN; style = Paint.Style.STROKE; strokeWidth = 8f }
    private val paintText = Paint().apply { color = Color.WHITE; textSize = 40f; style = Paint.Style.FILL }

    fun setResults(list: List<DetectionResult>) {
        results = list
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        results.forEach {
            canvas.drawRect(it.rect, paintBox)
            canvas.drawText(it.text, it.rect.left, it.rect.top - 10f, paintText)
        }
    }

    data class DetectionResult(val rect: RectF, val text: String)
}