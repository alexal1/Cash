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
        groupingSeparator = ' '
    }

    private val decimalFormat = DecimalFormat("#,###.##", decimalFormatSymbols)


    fun formatMoney(money: Money, withGrouping: Boolean = true, withFixedFraction: Boolean = true): String {
        return formatMoney(money.amount, withGrouping, withFixedFraction)
    }

    fun formatMoney(money: Number, withGrouping: Boolean = true, withFixedFraction: Boolean = true): String {
        decimalFormat.isGroupingUsed = withGrouping
        decimalFormat.minimumFractionDigits = if (withFixedFraction) 2 else 0
        decimalFormat.maximumFractionDigits = if (withFixedFraction) 2 else Int.MAX_VALUE
        decimalFormat.negativePrefix = "- "
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