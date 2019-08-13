/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.chart

import android.content.Context
import android.graphics.PointF
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.contains
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.viewmodels.enums.Categories
import io.reactivex.functions.Consumer
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class ChartView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, KoinComponent {

    companion object {

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

            Timber.i("prevChartData = $lastChartData, newChartData = $chartData")
        }
    }

    @Volatile
    var topPadding = 0
        set(value) {
            field = value
            drawThread?.topPadding = value
        }

    private val analytics: Analytics by inject()
    private var drawThread: DrawThread? = null
    private var lastChartAnimator: ChartAnimator? = null


    init {
        holder.addCallback(this)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        drawThread = DrawThread(holder, context, LATENCY).apply {
            chartAnimator = lastChartAnimator
            isRunning = true
            topPadding = this@ChartView.topPadding
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
                    Timber.e(exception, "Error when trying to stop draw thread")
                }
            }
        }
    }

    fun click(point: PointF) {
        drawThread?.let { drawThread ->
            if (drawThread.checkedCategory == null) {
                val clickedCategory = findCategoryByClick(point)
                if (clickedCategory != null) {
                    analytics.pickChartCategory(clickedCategory)
                }
                drawThread.checkedCategory = clickedCategory
            } else {
                drawThread.checkedCategory = null
            }
        }
    }

    fun onBackPressed(): Boolean = drawThread?.let { drawThread ->
        if (drawThread.checkedCategory != null) {
            drawThread.checkedCategory = null
            true
        } else {
            false
        }
    } ?: false

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