package com.alex_aladdin.cash.viewmodels.enums

import com.alex_aladdin.cash.R

enum class GainCategories : Categories {

    SALARY {
        override val id: String = name
        override val isGain = true
        override val stringRes = R.string.gain_salary
    }

}