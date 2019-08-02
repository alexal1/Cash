/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.chart

import android.graphics.RectF
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories

object ChartDrawer {

    const val SIDE_CHART_WIDTH = 0.06f
    const val CENTRAL_CHART_WIDTH = 0.25f

    private val currentLoss = HashMap<LossCategories, Float>()
    private val currentGain = HashMap<GainCategories, Float>()


    fun categoriesRects(
        width: Float,
        height: Float,
        chartAnimator: ChartAnimator,
        forEachGain: (category: GainCategories, rects: Pair<RectF, RectF>) -> Unit,
        forEachLoss: (category: LossCategories, rects: Pair<RectF, RectF>) -> Unit) {

        // Calculate X coordinates

        val sideChartWidth = SIDE_CHART_WIDTH * width
        val centralChartWidth = CENTRAL_CHART_WIDTH * width

        val columnLeft = (width - centralChartWidth) / 2f
        val columnRight = (width + centralChartWidth) / 2f

        // Obtain loss and gain values for this frame

        LossCategories.values().forEach { lossCategory ->
            currentLoss[lossCategory] = chartAnimator.nextLoss(lossCategory)
        }

        GainCategories.values().forEach { gainCategory ->
            currentGain[gainCategory] = chartAnimator.nextGain(gainCategory)
        }

        // Draw gains
        var lastTop = 0f
        for (gainCategory in GainCategories.values()) {
            val value = currentGain.getValue(gainCategory)
            if (value == 0f) {
                continue
            }

            val columnHeight = height * value / chartAnimator.maxValue

            val rect1 = RectF(0f, height - lastTop - columnHeight, sideChartWidth, height - lastTop)
            val rect2 = RectF(columnLeft, height - lastTop - columnHeight, columnRight, height - lastTop)

            forEachGain(gainCategory, rect1 to rect2)

            lastTop += columnHeight
        }

        // Draw losses
        lastTop = 0f
        for (lossCategory in LossCategories.values()) {
            val value = currentLoss.getValue(lossCategory)
            if (value == 0f) {
                continue
            }

            val columnHeight = height * value / chartAnimator.maxValue

            val rect1 = RectF(width - sideChartWidth, height - lastTop - columnHeight, width, height - lastTop)
            val rect2 = RectF(columnLeft, height - lastTop - columnHeight, columnRight, height - lastTop)

            forEachLoss(lossCategory, rect1 to rect2)

            lastTop += columnHeight
        }
    }

}