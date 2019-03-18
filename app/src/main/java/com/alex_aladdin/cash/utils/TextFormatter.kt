package com.alex_aladdin.cash.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object TextFormatter {

    private const val currencySymbol = "₽"

    private val decimalFormat = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.CEILING
    }


    fun formatMoney(value: Float?) = if (value != null) {
        "${decimalFormat.format(value)} $currencySymbol"
    } else {
        "0"
    }

    fun formatDouble(value: Double): String {
        var stringValue = BigDecimal.valueOf(value).toPlainString()
        while (stringValue.contains('.') && (stringValue.last() == '0' || stringValue.last() == '.')) {
            stringValue = stringValue.dropLast(1)
        }
        return stringValue
    }

}