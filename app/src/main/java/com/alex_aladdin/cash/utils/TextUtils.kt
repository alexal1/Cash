package com.alex_aladdin.cash.utils

import java.math.RoundingMode
import java.text.DecimalFormat

object TextUtils {

    private const val currencySymbol = "₽"

    private val decimalFormat = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.CEILING
    }


    fun formatMoney(value: Float?) = if (value != null) {
        "${decimalFormat.format(value)} $currencySymbol"
    } else {
        "0"
    }

}