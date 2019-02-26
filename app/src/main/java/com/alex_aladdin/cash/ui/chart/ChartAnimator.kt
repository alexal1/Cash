package com.alex_aladdin.cash.ui.chart

import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import kotlin.math.pow

class ChartAnimator(
    prevChartData: ChartData,
    private val newChartData: ChartData,
    private val framesCount: Int
) {

    @Volatile
    private var isRunning = true

    private val currentGain = HashMap(prevChartData.gain)
    private val currentLoss = HashMap(prevChartData.loss)

    private val step = ChartData(
        gain = GainCategories.values()
            .map { gainCategory ->
                val start = prevChartData.gain[gainCategory] ?: 0f
                val end = newChartData.gain[gainCategory] ?: 0f
                gainCategory to (end - start) / framesCount
            }
            .toMap(),
        loss = LossCategories.values()
            .map { lossCategory ->
                val start = prevChartData.loss[lossCategory] ?: 0f
                val end = newChartData.loss[lossCategory] ?: 0f
                lossCategory to (end - start) / framesCount
            }
            .toMap()
    )

    private val decelerator = { step: Float, counter: Int ->
        val x = counter.toFloat()
        val l = framesCount.toFloat()
        1.5f * step * (2 * x.pow(2) + 2 * l.pow(2) - 4 * x * l + 2 * x - 2 * l + 1) / l.pow(2)
    }

    private val isAnimationOnEmptyChart = prevChartData == ChartData()

    private val maxValueByNewChart by lazy {
        maxOf(newChartData.gain.values.sum(), newChartData.loss.values.sum())
    }

    private var gainStepsCounter = 0
    private var lossStepsCounter = 0

    val maxValue get() = if (!isAnimationOnEmptyChart) {
        maxOf(currentGain.values.sum(), currentLoss.values.sum())
    } else {
        maxValueByNewChart
    }


    fun nextGain(category: GainCategories): Float {
        var value = currentGain[category] ?: 0f

        if (!isRunning) {
            return value
        }

        if (gainStepsCounter == framesCount - 1) {
            return newChartData.gain[category] ?: 0f
        }

        val step = step.gain[category] ?: 0f
        value += decelerator(step, gainStepsCounter)
        currentGain[category] = value
        gainStepsCounter++
        return value
    }

    fun nextLoss(category: LossCategories): Float {
        var value = currentLoss[category] ?: 0f

        if (!isRunning) {
            return value
        }

        if (lossStepsCounter == framesCount - 1) {
            return newChartData.loss[category] ?: 0f
        }

        val step = step.loss[category] ?: 0f
        value += decelerator(step, lossStepsCounter)
        currentLoss[category] = value
        lossStepsCounter++
        return value
    }

    fun stop(): ChartData {
        isRunning = false
        return ChartData(currentGain, currentLoss)
    }

}