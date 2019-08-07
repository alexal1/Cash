/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.chart

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CategoriesManager
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.ui.chart.ChartDrawer.CENTRAL_CHART_WIDTH
import com.madewithlove.daybalance.ui.chart.ChartDrawer.SIDE_CHART_WIDTH
import com.madewithlove.daybalance.viewmodels.enums.Categories
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories
import org.jetbrains.anko.dimen
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

    @Volatile
    var topPadding = 0

    private val currencyManager: CurrencyManager by inject()
    private val categoriesManager: CategoriesManager by inject()

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.palladium)
        isAntiAlias = false
    }

    private val gainPaints: Map<GainCategories, Paint> = GainCategories.values()
        .map { it to Paint().apply { color = categoriesManager.categoriesColors.getValue(it); isAntiAlias = false } }
        .toMap()
    private val lossPaints: Map<LossCategories, Paint> = LossCategories.values()
        .map { it to Paint().apply { color = categoriesManager.categoriesColors.getValue(it); isAntiAlias = false } }
        .toMap()

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(12).toFloat()
        isAntiAlias = true
        alpha = (0.8f * 255).toInt()
        typeface = ResourcesCompat.getFont(context, R.font.currencies)
    }

    private val checkedTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(16).toFloat()
        isAntiAlias = true
        alpha = (0.8f * 255).toInt()
        isFakeBoldText = true
        letterSpacing = 0.02f
        typeface = ResourcesCompat.getFont(context, R.font.currencies)
    }

    private val tapToReturnTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(16).toFloat()
        isAntiAlias = true
        alpha = (0.5f * 255).toInt()
    }

    private val diffTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = context.dip(12).toFloat()
        isAntiAlias = true
        typeface = ResourcesCompat.getFont(context, R.font.currencies)
        isFakeBoldText = true
    }

    private val diffBackgroundPaintPositive = Paint().apply {
        color = ContextCompat.getColor(context, R.color.green)
        isAntiAlias = true
    }

    private val diffBackgroundPaintNegative = Paint().apply {
        color = ContextCompat.getColor(context, R.color.red)
        isAntiAlias = true
    }

    private val lineWidth = context.dip(1).toFloat()
    private val diffTextHorizontalPadding = context.dimen(R.dimen.chart_diff_text_horizontal_padding).toFloat()
    private val diffTextVerticalPadding = context.dimen(R.dimen.chart_diff_text_vertical_padding).toFloat()
    private val textRect = Rect()
    private val textNameRect = Rect()
    private val textValueRect = Rect()
    private val textTapToReturnRect = Rect()

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
        drawRectWithPadding(0f, 0f, sideChartWidth, height, backgroundPaint)
        drawRectWithPadding(width - sideChartWidth, 0f, width, height, backgroundPaint)

        val columnLeft = (width - centralChartWidth) / 2f
        val columnRight = (width + centralChartWidth) / 2f
        drawRectWithPadding(columnLeft, 0f, columnRight, height, backgroundPaint)

        chartAnimator?.let { chartAnimator ->
            ChartDrawer.categoriesRects(
                width = width,
                height = height,
                chartAnimator = chartAnimator,
                forEachGain = { category, (rect1, rect2) ->
                    if (checkedCategory == null || checkedCategory == category) {
                        val paint = gainPaints.getValue(category)
                        drawRectWithPadding(rect1, paint)
                        drawRectWithPadding(rect2, paint)
                    }
                },
                forEachLoss = { category, (rect1, rect2) ->
                    if (checkedCategory == null || checkedCategory == category) {
                        val paint = lossPaints.getValue(category)
                        drawRectWithPadding(rect1, paint)
                        drawRectWithPadding(rect2, paint)
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
                textPaint.getTextBounds(gain, 0, gain.length, textRect)
                val top = height * (1f - chartAnimator.totalGain / chartAnimator.maxValue) + textRect.height()
                val left = sideChartWidth + TEXT_PADDING * width
                drawTextWithPadding(gain, left, top, textPaint, textRect)
            }

            // Loss
            chartAnimator?.let { chartAnimator ->
                val loss = currencyManager.formatMoney(chartAnimator.totalLoss)
                textPaint.getTextBounds(loss, 0, loss.length, textRect)
                val top = height * (1f - chartAnimator.totalLoss / chartAnimator.maxValue) + textRect.height()
                val left = width - sideChartWidth - textRect.width() - TEXT_PADDING * width
                drawTextWithPadding(loss, left, top, textPaint, textRect)
            }
        } else {
            checkedCategory?.let { checkedCategory ->
                val textName = context.getString(checkedCategory.stringRes)
                checkedTextPaint.getTextBounds(textName, 0, textName.length, textNameRect)

                val textValue = currencyManager.formatMoney(chartAnimator?.getCurrentValue(checkedCategory))
                checkedTextPaint.getTextBounds(textValue, 0, textValue.length, textValueRect)

                val textTapToReturn = context.getString(R.string.tap_to_return)
                tapToReturnTextPaint.getTextBounds(textTapToReturn, 0, textTapToReturn.length, textTapToReturnRect)

                val textNameTop = (height - textNameRect.height() - textValueRect.height()) / 2f
                val textNameLeft = (width - textNameRect.width()) / 2f
                drawTextWithPadding(textName, textNameLeft, textNameTop, checkedTextPaint, textNameRect)

                val textValueTop = textNameTop + CHECKED_TEXT_PADDING * height + textNameRect.height()
                val textValueLeft = (width - textValueRect.width()) / 2f
                drawTextWithPadding(textValue, textValueLeft, textValueTop, checkedTextPaint, textValueRect)

                val textTapToReturnTop = textValueTop + TAP_TO_RETURN_TEXT_PADDING * height + textValueRect.height()
                val textTapToReturnLeft = (width - textTapToReturnRect.width()) / 2f
                drawTextWithPadding(textTapToReturn, textTapToReturnLeft, textTapToReturnTop, tapToReturnTextPaint, textTapToReturnRect)
            }
        }

        // Draw diff text
        chartAnimator?.let { chartAnimator ->
            val diff = chartAnimator.totalGain - chartAnimator.totalLoss
            val gainRatio = chartAnimator.totalGain / chartAnimator.maxValue
            val lossRatio = chartAnimator.totalLoss / chartAnimator.maxValue

            if (diff > 0) {
                val centerX = (columnRight + width - sideChartWidth) / 2f
                val centerY = topPadding.toFloat() + (height - topPadding.toFloat()) * (2 - gainRatio - lossRatio) / 2f

                val text = "+ ${currencyManager.formatMoney(diff)}"
                diffTextPaint.getTextBounds(text, 0, text.length, textRect)

                val bgWidth = textRect.width().toFloat() + diffTextHorizontalPadding * 2
                val bgHeight = textRect.height().toFloat() + diffTextVerticalPadding * 2

                val bgLeft = centerX - bgWidth / 2f
                val bgTop = minOf(maxOf(centerY - bgHeight / 2f, topPadding.toFloat()), height - bgHeight)
                val bgRight = centerX + bgWidth / 2f
                val bgBottom = bgTop + bgHeight

                drawRoundRect(bgLeft, bgTop, bgRight, bgBottom, bgHeight / 2f, bgHeight / 2f, diffBackgroundPaintPositive)

                val textLeft = centerX - textRect.width() / 2f
                val textBottom = minOf(maxOf(centerY + textRect.height() / 2f, topPadding.toFloat() + diffTextVerticalPadding + textRect.height()), height - diffTextVerticalPadding)

                drawText(text, textLeft, textBottom, diffTextPaint)
            } else if (diff < 0) {
                val centerX = (sideChartWidth + columnLeft) / 2f
                val centerY = topPadding.toFloat() + (height - topPadding.toFloat()) * (2 - gainRatio - lossRatio) / 2f

                val text = "- ${currencyManager.formatMoney(-diff)}"
                diffTextPaint.getTextBounds(text, 0, text.length, textRect)

                val bgWidth = textRect.width().toFloat() + diffTextHorizontalPadding * 2
                val bgHeight = textRect.height().toFloat() + diffTextVerticalPadding * 2

                val bgLeft = centerX - bgWidth / 2f
                val bgTop = minOf(maxOf(centerY - bgHeight / 2f, topPadding.toFloat()), height - bgHeight)
                val bgRight = centerX + bgWidth / 2f
                val bgBottom = bgTop + bgHeight

                drawRoundRect(bgLeft, bgTop, bgRight, bgBottom, bgHeight / 2f, bgHeight / 2f, diffBackgroundPaintNegative)

                val textLeft = centerX - textRect.width() / 2f
                val textBottom = minOf(maxOf(centerY + textRect.height() / 2f, topPadding.toFloat() + diffTextVerticalPadding + textRect.height()), height - diffTextVerticalPadding)

                drawText(text, textLeft, textBottom, diffTextPaint)
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

    private fun Canvas.drawRectWithPadding(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        val compressionCoefficient = (height - topPadding).toFloat() / height.toFloat()
        val newTop = topPadding.toFloat() + top * compressionCoefficient
        val newHeight = (bottom - top) * compressionCoefficient

        drawRect(left, newTop, right, newTop + newHeight, paint)
    }

    private fun Canvas.drawRectWithPadding(rect: RectF, paint: Paint) {
        drawRectWithPadding(rect.left, rect.top, rect.right, rect.bottom, paint)
    }

    private fun Canvas.drawTextWithPadding(text: String, left: Float, top: Float, paint: Paint, rect: Rect) {
        val compressionCoefficient = (height - topPadding).toFloat() / height.toFloat()
        val newTop = topPadding.toFloat() + top * compressionCoefficient
        val maxTop = height.toFloat() - rect.height()

        drawText(text, left, minOf(newTop, maxTop), paint)
    }

}