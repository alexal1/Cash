/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.chart

import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories

data class ChartData(
    val gain: Map<GainCategories, Float> = emptyMap(),
    val loss: Map<LossCategories, Float> = emptyMap()
)