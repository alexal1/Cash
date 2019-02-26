package com.alex_aladdin.cash.viewmodels.enums

import android.support.annotation.ColorRes
import com.alex_aladdin.cash.R

enum class LossCategories(@ColorRes val colorRes: Int) : Categories {
    CAFES_AND_RESTAURANTS(R.color.red),
    FOODSTUFF(R.color.redGradColor1)
}