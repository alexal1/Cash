package com.alex_aladdin.cash.ui.chart

import android.content.Context
import android.graphics.PointF
import android.util.Log.e
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.contains
import com.alex_aladdin.cash.viewmodels.enums.Categories
import io.reactivex.functions.Consumer

class ChartView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {

        private const val TAG = "CashChartView"
        private const val FPS = 60L
        private const val LATENCY = 1000L / FPS
        private const val ANIMATION_DURATION = 1000L
        private const val FRAMES_COUNT = ANIMATION_DURATION * FPS / 1000L

    }


    val chartDataConsumer = Consumer<ChartData> { chartData ->
        synchronized(holder) {
            val lastChartData = drawThread?.chartAnimator?.stop() ?: ChartData()
            lastChartAnimator = ChartAnimator(lastChartData, chartData, FRAMES_COUNT.toInt())
            drawThread?.chartAnimator = lastChartAnimator
        }
    }

    private var drawThread: DrawThread? = null
    private var lastChartAnimator: ChartAnimator? = null


    init {
        holder.addCallback(this)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        drawThread = DrawThread(holder, context, LATENCY).apply {
            chartAnimator = lastChartAnimator
            isRunning = true
            start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawThread?.apply {
            var retry = true
            isRunning = false

            while (retry) {
                try {
                    join()
                    retry = false
                } catch (exception: InterruptedException) {
                    e(TAG, "Error when trying to stop draw thread", exception)
                }
            }
        }
    }

    fun click(point: PointF) {
        drawThread?.let { drawThread ->
            val oldCheckedCategory = drawThread.checkedCategory
            val newCheckedCategory = findCategoryByClick(point)

            if (oldCheckedCategory == null) {
                drawThread.checkedCategory = newCheckedCategory
            } else if (oldCheckedCategory != newCheckedCategory) {
                drawThread.checkedCategory = null
            }
        }
    }

    private fun findCategoryByClick(point: PointF): Categories? = lastChartAnimator?.let { chartAnimator ->
        var resultCategory: Categories? = null

        ChartDrawer.categoriesRects(
            width = width.toFloat(),
            height = height.toFloat(),
            chartAnimator = chartAnimator,
            forEachGain = { category, (rect1, rect2) ->
                if (rect1.contains(point) || rect2.contains(point)) {
                    resultCategory = category
                }
            },
            forEachLoss = { category, (rect1, rect2) ->
                if (rect1.contains(point) || rect2.contains(point)) {
                    resultCategory = category
                }
            }
        )

        return resultCategory
    }

}