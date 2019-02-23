package com.alex_aladdin.cash.ui.chart

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.view.SurfaceHolder
import com.alex_aladdin.cash.R
import org.jetbrains.anko.dip
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class DrawThread(private val surfaceHolder: SurfaceHolder, private val context: Context) : Thread() {

    companion object {

        private const val ANIMATION_DURATION = 400L
        private const val FRAMES_COUNT = 25
        private const val LATENCY = ANIMATION_DURATION / FRAMES_COUNT

        private const val SIDE_CHART_WIDTH = 0.06f
        private const val CENTRAL_CHART_WIDTH = 0.25f

    }


    val runFlag = AtomicBoolean()
    val drawQueue = ConcurrentLinkedQueue<ChartData>()

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.palladium)
        isAntiAlias = false
    }

    private val lineWidth = context.dip(1).toFloat()

    private var linePaint: Paint? = null

    var prevFrameTime = Long.MAX_VALUE


    override fun run() {
        while (runFlag.get()) {
            val chartData = drawQueue.poll() ?: continue

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas(null)
                synchronized(surfaceHolder) {
                    drawChart(canvas, chartData)
                }
            } finally {
                canvas?.let(surfaceHolder::unlockCanvasAndPost)
            }

            val realLatency = System.currentTimeMillis() - prevFrameTime
            if (realLatency < LATENCY) {
                Thread.sleep(minOf(LATENCY, LATENCY - realLatency))
            }
        }
    }

    private fun drawChart(canvas: Canvas, chartData: ChartData) = canvas.apply {
        val width = width.toFloat()
        val height = height.toFloat()
        val sideChartWidth = SIDE_CHART_WIDTH * width
        val centralChartWidth = CENTRAL_CHART_WIDTH * width

        // Draw background
        drawColor(ContextCompat.getColor(context, R.color.deepDark))

        // Draw chart background
        drawRect(0f, 0f, sideChartWidth, height, backgroundPaint)
        drawRect(width - sideChartWidth, 0f, width, height, backgroundPaint)
        drawRect((width - centralChartWidth) / 2f, 0f, (width + centralChartWidth) / 2f, height, backgroundPaint)

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