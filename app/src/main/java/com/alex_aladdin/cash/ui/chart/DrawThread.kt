package com.alex_aladdin.cash.ui.chart

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import org.jetbrains.anko.dip

class DrawThread(
    private val surfaceHolder: SurfaceHolder,
    private val context: Context,
    private val latency: Long
) : Thread() {

    companion object {

        private const val SIDE_CHART_WIDTH = 0.06f
        private const val CENTRAL_CHART_WIDTH = 0.25f

    }

    @Volatile
    var isRunning = false

    @Volatile
    var chartAnimator: ChartAnimator? = null

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.palladium)
        isAntiAlias = false
    }

    private val gainPaints: Map<GainCategories, Paint> = GainCategories.values()
        .map { it to Paint().apply { color = ContextCompat.getColor(context, it.colorRes); isAntiAlias = false } }
        .toMap()
    private val lossPaints: Map<LossCategories, Paint> = LossCategories.values()
        .map { it to Paint().apply { color = ContextCompat.getColor(context, it.colorRes); isAntiAlias = false } }
        .toMap()

    private val lineWidth = context.dip(1).toFloat()

    private var linePaint: Paint? = null
    private var prevFrameTime = 0L


    override fun run() {
        while (isRunning) {
            val now = System.currentTimeMillis()
            if (now >= prevFrameTime + latency) {
                prevFrameTime = now
            } else {
                continue
            }

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas(null)
                synchronized(surfaceHolder) {
                    canvas?.let(this::drawChart)
                }
            } finally {
                canvas?.let(surfaceHolder::unlockCanvasAndPost)
            }
        }
    }

    private fun drawChart(canvas: Canvas) = canvas.apply {
        val width = width.toFloat()
        val height = height.toFloat()
        val sideChartWidth = SIDE_CHART_WIDTH * width
        val centralChartWidth = CENTRAL_CHART_WIDTH * width

        // Clear background
        drawColor(0, PorterDuff.Mode.CLEAR)

        // Draw chart background
        drawRect(0f, 0f, sideChartWidth, height, backgroundPaint)
        drawRect(width - sideChartWidth, 0f, width, height, backgroundPaint)

        val columnLeft = (width - centralChartWidth) / 2f
        val columnRight = (width + centralChartWidth) / 2f
        drawRect(columnLeft, 0f, columnRight, height, backgroundPaint)

        chartAnimator?.let { chartAnimator ->
            // Draw gains
            var lastTop = 0f
            GainCategories.values().forEach { gainCategory ->
                val value = chartAnimator.nextGain(gainCategory)
                val columnHeight = height * value / chartAnimator.maxValue
                val paint = gainPaints[gainCategory]

                drawRect(0f, height - lastTop - columnHeight, sideChartWidth, height - lastTop, paint)
                drawRect(columnLeft, height - lastTop - columnHeight, columnRight, height - lastTop, paint)

                lastTop += columnHeight
            }

            // Draw losses
            lastTop = 0f
            LossCategories.values().forEach { lossCategory ->
                val value = chartAnimator.nextLoss(lossCategory)
                val columnHeight = height * value / chartAnimator.maxValue
                val paint = lossPaints[lossCategory]

                drawRect(columnLeft, height - lastTop - columnHeight, columnRight, height - lastTop, paint)
                drawRect(width - sideChartWidth, height - lastTop - columnHeight, width, height - lastTop, paint)

                lastTop += columnHeight
            }
        }

        // Draw line
        drawLine(0f, height - lineWidth / 2f, width, height - lineWidth / 2f, getLinePaint(width))
    }

    private fun getLinePaint(width: Float): Paint {
        if (linePaint != null) {
            return linePaint!!
        }

        linePaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f,
                0f,
                width,
                0f,
                intArrayOf(
                    Color.WHITE,
                    Color.WHITE,
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    Color.WHITE,
                    Color.WHITE,
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    Color.WHITE,
                    Color.WHITE
                ),
                floatArrayOf(
                    0f,
                    SIDE_CHART_WIDTH,
                    SIDE_CHART_WIDTH + 0.1f,
                    (1f - CENTRAL_CHART_WIDTH) / 2f - 0.1f,
                    (1f - CENTRAL_CHART_WIDTH) / 2f,
                    (1f + CENTRAL_CHART_WIDTH) / 2f,
                    (1f + CENTRAL_CHART_WIDTH) / 2f + 0.1f,
                    1f - SIDE_CHART_WIDTH - 0.1f,
                    1f - SIDE_CHART_WIDTH,
                    1f
                ),
                Shader.TileMode.CLAMP
            )
            strokeWidth = lineWidth
        }

        return linePaint!!
    }

}