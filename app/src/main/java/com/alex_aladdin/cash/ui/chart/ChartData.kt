package com.alex_aladdin.cash.ui.chart

import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories

data class ChartData(
    val gain: Map<GainCategories, Float> = emptyMap(),
    val loss: Map<LossCategories, Float> = emptyMap()
)