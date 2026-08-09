package com.findisce.mobile.ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = floatArrayOf(0.7f, 0.75f, 0.68f, 0.60f, 0.15f, 0.72f)
    private val labels = arrayOf("Mar", "Apr", "May", "Jun", "Jul", "Aug")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2563EB")
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2563EB")
        style = Paint.Style.FILL
    }

    private val pointInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9CA3AF")
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val fillPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val paddingLeft = 60f
        val paddingRight = 60f
        val paddingTop = 40f
        val paddingBottom = 70f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val stepX = chartWidth / (dataPoints.size - 1)

        path.reset()
        fillPath.reset()

        val points = mutableListOf<PointF>()

        for (i in dataPoints.indices) {
            val x = paddingLeft + i * stepX
            val y = paddingTop + (1f - dataPoints[i]) * chartHeight
            points.add(PointF(x, y))

            // Draw X-axis label
            canvas.drawText(labels[i], x, height - 20f, textPaint)
        }

        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, height - paddingBottom)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                val cx1 = prev.x + (current.x - prev.x) / 2
                val cy1 = prev.y
                val cx2 = prev.x + (current.x - prev.x) / 2
                val cy2 = current.y

                path.cubicTo(cx1, cy1, cx2, cy2, current.x, current.y)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, current.x, current.y)
            }

            fillPath.lineTo(points.last().x, height - paddingBottom)
            fillPath.close()

            // Create gradient fill
            val gradient = LinearGradient(
                0f, paddingTop, 0f, height - paddingBottom,
                Color.parseColor("#332563EB"), Color.parseColor("#002563EB"),
                Shader.TileMode.CLAMP
            )
            fillPaint.shader = gradient
            canvas.drawPath(fillPath, fillPaint)

            // Draw line
            canvas.drawPath(path, linePaint)

            // Draw points
            for (p in points) {
                canvas.drawCircle(p.x, p.y, 10f, pointPaint)
                canvas.drawCircle(p.x, p.y, 5f, pointInnerPaint)
            }
        }
    }
}
