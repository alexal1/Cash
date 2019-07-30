package com.madewithlove.daybalance.ui.chart

import android.graphics.RectF
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories

object ChartDrawer {

    const val SIDE_CHART_WIDTH = 0.06f
    const val CENTRAL_CHART_WIDTH = 0.25f


    fun categoriesRects(
        width: Float,
        height: Float,
        chartAnimator: ChartAnimator,
        forEachGain: (category: GainCategories, rects: Pair<RectF, RectF>) -> Unit,
        forEachLoss: (category: LossCategories, rects: Pair<RectF, RectF>) -> Unit) {

        val sideChartWidth = SIDE_CHART_WIDTH * width
        val centralChartWidth = CENTRAL_CHART_WIDTH * width

        val columnLeft = (width - centralChartWidth) / 2f
        val columnRight = (width + centralChartWidth) / 2f

        // Draw gains
        var lastTop = 0f
        GainCategories.values().forEach { gainCategory ->
            val value = chartAnimator.nextGain(gainCategory)
            val columnHeight = if (chartAnimator.maxValue == 0f) 0f else height * value / chartAnimator.maxValue

            val rect1 = RectF(0f, height - lastTop - columnHeight, sideChartWidth, height - lastTop)
            val rect2 = RectF(columnLeft, height - lastTop - columnHeight, columnRight, height - lastTop)

            forEachGain(gainCategory, rect1 to rect2)

            lastTop += columnHeight
        }

        // Draw losses
        lastTop = 0f
        LossCategories.values().forEach { lossCategory ->
            val value = chartAnimator.nextLoss(lossCategory)
            val columnHeight = if (chartAnimator.maxValue == 0f) 0f else height * value / chartAnimator.maxValue

            val rect1 = RectF(width - sideChartWidth, height - lastTop - columnHeight, width, height - lastTop)
            val rect2 = RectF(columnLeft, height - lastTop - columnHeight, columnRight, height - lastTop)

            forEachLoss(lossCategory, rect1 to rect2)

            lastTop += columnHeight
        }
    }

}