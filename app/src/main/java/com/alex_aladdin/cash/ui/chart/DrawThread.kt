package com.alex_aladdin.cash.ui.chart

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.ui.chart.ChartDrawer.CENTRAL_CHART_WIDTH
import com.alex_aladdin.cash.ui.chart.ChartDrawer.SIDE_CHART_WIDTH
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import org.jetbrains.anko.dip
import org.koin.core.KoinComponent
import org.koin.core.inject

class DrawThread(
    private val surfaceHolder: SurfaceHolder,
    private val context: Context,
    private val latency: Long
) : Thread(), KoinComponent {

    companion object {

        private const val TEXT_PADDING = 0.01f
        private const val CHECKED_TEXT_PADDING = 0.02f
        private const val TAP_TO_RETURN_TEXT_PADDING = 0.08f

    }


    @Volatile
    var isRunning = false

    @Volatile
    var chartAnimator: ChartAnimator? = null

    @Volatile
    var checkedCategory: Categories? = null

    private val currencyManager: CurrencyManager by inject()

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

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(12).toFloat()
        isAntiAlias = true
        alpha = (0.8f * 255).toInt()
    }

    private val checkedTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(16).toFloat()
        isAntiAlias = true
        alpha = (0.8f * 255).toInt()
        isFakeBoldText = true
        letterSpacing = 0.02f
    }

    private val tapToReturnTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(16).toFloat()
        isAntiAlias = true
        alpha = (0.5f * 255).toInt()
    }

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
                        val paint = gainPaints.getValue(category)
                        drawRect(rect1, paint)
                        drawRect(rect2, paint)
                    }
                },
                forEachLoss = { category, (rect1, rect2) ->
                    if (checkedCategory == null || checkedCategory == category) {
                        val paint = lossPaints.getValue(category)
                        drawRect(rect1, paint)
                        drawRect(rect2, paint)
                    }
                }
            )
        }

        // Draw line
        drawLine(0f, height - lineWidth / 2f, width, height - lineWidth / 2f, getLinePaint(width))

        // Draw text
        if (checkedCategory == null) {
            // Gain
            chartAnimator?.let { chartAnimator ->
                val gain = currencyManager.formatMoney(chartAnimator.totalGain)
                val textRect = Rect()
                textPaint.getTextBounds(gain, 0, gain.length, textRect)
                val top = minOf(
                    height * (1f - chartAnimator.totalGain / chartAnimator.maxValue) + textRect.height(),
                    height - textRect.height()
                )
                val left = sideChartWidth + TEXT_PADDING * width
                drawText(gain, left, top, textPaint)
            }

            // Loss
            chartAnimator?.let { chartAnimator ->
                val loss = currencyManager.formatMoney(chartAnimator.totalLoss)
                val textRect = Rect()
                textPaint.getTextBounds(loss, 0, loss.length, textRect)
                val top = minOf(
                    height * (1f - chartAnimator.totalLoss / chartAnimator.maxValue) + textRect.height(),
                    height - textRect.height()
                )
                val left = width - sideChartWidth - textRect.width() - TEXT_PADDING * width
                drawText(loss, left, top, textPaint)
            }
        } else {
            checkedCategory?.let { checkedCategory ->
                val textName = context.getString(checkedCategory.stringRes)
                val textNameRect = Rect()
                checkedTextPaint.getTextBounds(textName, 0, textName.length, textNameRect)

                val textValue = currencyManager.formatMoney(chartAnimator?.getCurrentValue(checkedCategory))
                val textValueRect = Rect()
                checkedTextPaint.getTextBounds(textValue, 0, textValue.length, textValueRect)

                val textTapToReturn = context.getString(R.string.tap_to_return)
                val textTapToReturnRect = Rect()
                tapToReturnTextPaint.getTextBounds(textTapToReturn, 0, textTapToReturn.length, textTapToReturnRect)

                val textNameTop = (height - textNameRect.height() - textValueRect.height()) / 2f
                val textNameLeft = (width - textNameRect.width()) / 2f
                drawText(textName, textNameLeft, textNameTop, checkedTextPaint)

                val textValueTop = textNameTop + CHECKED_TEXT_PADDING * height + textNameRect.height()
                val textValueLeft = (width - textValueRect.width()) / 2f
                drawText(textValue, textValueLeft, textValueTop, checkedTextPaint)

                val textTapToReturnTop = textValueTop + TAP_TO_RETURN_TEXT_PADDING * height + textValueRect.height()
                val textTapToReturnLeft = (width - textTapToReturnRect.width()) / 2f
                drawText(textTapToReturn, textTapToReturnLeft, textTapToReturnTop, tapToReturnTextPaint)
            }
        }
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