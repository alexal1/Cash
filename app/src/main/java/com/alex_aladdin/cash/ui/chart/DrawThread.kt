package com.alex_aladdin.cash.ui.chart

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.chart.ChartDrawer.CENTRAL_CHART_WIDTH
import com.alex_aladdin.cash.ui.chart.ChartDrawer.SIDE_CHART_WIDTH
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import org.jetbrains.anko.dip

class DrawThread(
    private val surfaceHolder: SurfaceHolder,
    private val context: Context,
    private val latency: Long
) : Thread() {

    @Volatile
    var isRunning = false

    @Volatile
    var chartAnimator: ChartAnimator? = null

    @Volatile
    var checkedCategory: Categories? = null

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
            ChartDrawer.categoriesRects(
                width = width,
                height = height,
                chartAnimator = chartAnimator,
                forEachGain = { category, (rect1, rect2) ->
                    if (checkedCategory == null || checkedCategory == category) {
                        val paint = gainPaints[category]
                        drawRect(rect1, paint)
                        drawRect(rect2, paint)
                    }
                },
                forEachLoss = { category, (rect1, rect2) ->
                    if (checkedCategory == null || checkedCategory == category) {
                        val paint = lossPaints[category]
                        drawRect(rect1, paint)
                        drawRect(rect2, paint)
                    }
                }
            )
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