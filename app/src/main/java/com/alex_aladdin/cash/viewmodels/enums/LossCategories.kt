package com.alex_aladdin.cash.viewmodels.enums

import com.alex_aladdin.cash.R

enum class LossCategories : Categories {

    CAFES_AND_RESTAURANTS {
        override val isGain = false
        override val colorRes = R.color.red
        override val stringRes = R.string.loss_cafes_and_restaurants
    },
    FOODSTUFF {
        override val isGain = false
        override val colorRes = R.color.redGradColor1
        override val stringRes = R.string.loss_foodstuff
    }

}