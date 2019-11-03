/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import com.madewithlove.daybalance.dto.Money
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object TextFormatter {

    private val decimalFormatSymbols = DecimalFormatSymbols().apply {
        decimalSeparator = '.'
    }

    private val decimalFormat = DecimalFormat("#.##", decimalFormatSymbols).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }


    fun formatMoney(money: Money, withGrouping: Boolean = true): String {
        decimalFormat.isGroupingUsed = withGrouping
        return decimalFormat.format(money.amount)
    }

    fun formatMoney(money: Float, withGrouping: Boolean = true): String {
        decimalFormat.isGroupingUsed = withGrouping
        return decimalFormat.format(money)
    }

    fun formatDouble(value: Double): String {
        var stringValue = BigDecimal.valueOf(value).toPlainString()
        while (stringValue.contains('.') && (stringValue.last() == '0' || stringValue.last() == '.')) {
            stringValue = stringValue.dropLast(1)
        }
        return stringValue
    }

}