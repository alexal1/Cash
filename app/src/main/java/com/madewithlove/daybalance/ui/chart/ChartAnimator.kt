package com.madewithlove.daybalance.ui.chart

import com.madewithlove.daybalance.viewmodels.enums.Categories
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories
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

    private val gainCounters = HashMap(GainCategories.values().map { it to 0 }.toMap())
    private val lossCounters = HashMap(LossCategories.values().map { it to 0 }.toMap())

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

    private val isPrevChartDataEmpty = prevChartData == ChartData()
    private val isNewChartDataEmpty = newChartData == ChartData()

    private val maxValueByPrevChart by lazy {
        maxOf(prevChartData.gain.values.sum(), prevChartData.loss.values.sum())
    }

    private val maxValueByNewChart by lazy {
        maxOf(newChartData.gain.values.sum(), newChartData.loss.values.sum())
    }

    val maxValue get() = when {
        isPrevChartDataEmpty -> maxValueByNewChart
        isNewChartDataEmpty -> maxValueByPrevChart
        else -> maxOf(currentGain.values.sum(), currentLoss.values.sum())
    }

    val totalGain get() = currentGain.values.sum()
    val totalLoss get() = currentLoss.values.sum()


    fun nextGain(category: GainCategories): Float {
        var value = currentGain[category] ?: 0f
        var counter = gainCounters[category]!!

        if (!isRunning) {
            return value
        }

        if (counter == framesCount - 1) {
            val finalGain = newChartData.gain[category] ?: 0f
            currentGain[category] = finalGain
            return finalGain
        }

        val step = step.gain[category] ?: 0f
        value += decelerator(step, counter)
        currentGain[category] = value

        counter++
        gainCounters[category] = counter

        return value
    }

    fun nextLoss(category: LossCategories): Float {
        var value = currentLoss[category] ?: 0f
        var counter = lossCounters[category]!!

        if (!isRunning) {
            return value
        }

        if (counter == framesCount - 1) {
            val finalLoss = newChartData.loss[category] ?: 0f
            currentLoss[category] = finalLoss
            return finalLoss
        }

        val step = step.loss[category] ?: 0f
        value += decelerator(step, counter)
        currentLoss[category] = value

        counter++
        lossCounters[category] = counter

        return value
    }

    fun stop(): ChartData {
        isRunning = false
        return ChartData(currentGain.filterValues { it > 0f }, currentLoss.filterValues { it > 0f })
    }

    fun getCurrentValue(category: Categories) = if (category.isGain) {
        currentGain[category] ?: 0f
    } else {
        currentLoss[category] ?: 0f
    }

}