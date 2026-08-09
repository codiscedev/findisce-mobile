package com.findisce.mobile.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class DonutSegment(
    val label: String,
    val value: Double,
    val colorHex: String
)

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var segments: List<DonutSegment> = emptyList()
    private var totalAmountText: String = "₹0"

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 32f
        strokeCap = Paint.Cap.ROUND
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111827")
        textSize = 36f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()

    fun setData(newSegments: List<DonutSegment>, totalFormatted: String) {
        this.segments = newSegments
        this.totalAmountText = totalFormatted
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val size = Math.min(width, height)
        val padding = arcPaint.strokeWidth + 12f

        rectF.set(
            (width - size) / 2f + padding,
            (height - size) / 2f + padding,
            (width + size) / 2f - padding,
            (height + size) / 2f - padding
        )

        val totalValue = segments.sumOf { it.value }.let { if (it == 0.0) 1.0 else it }
        var startAngle = -90f

        if (segments.isEmpty()) {
            arcPaint.color = Color.parseColor("#E5E7EB")
            canvas.drawArc(rectF, 0f, 360f, false, arcPaint)
        } else {
            for (segment in segments) {
                val sweepAngle = ((segment.value / totalValue) * 360f).toFloat()
                if (sweepAngle > 0f) {
                    arcPaint.color = Color.parseColor(segment.colorHex)
                    canvas.drawArc(rectF, startAngle, Math.max(sweepAngle - 4f, 2f), false, arcPaint)
                    startAngle += sweepAngle
                }
            }
        }

        // Draw Center Labels
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawText("Total", centerX, centerY - 10f, titlePaint)
        canvas.drawText(totalAmountText, centerX, centerY + 30f, valuePaint)
    }
}
