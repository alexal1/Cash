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
    private val savingsFormat = DecimalFormat("##'%'")


    fun formatMoney(money: Money, withGrouping: Boolean = true, withFixedFraction: Boolean = true, withPositivePrefix: Boolean = false, withNegativePrefix: Boolean = true, withSpaceAfterSign: Boolean = true): String {
        return formatMoney(money.amount, withGrouping, withFixedFraction, withPositivePrefix, withNegativePrefix, withSpaceAfterSign)
    }

    fun formatMoney(money: Number, withGrouping: Boolean = true, withFixedFraction: Boolean = true, withPositivePrefix: Boolean = false, withNegativePrefix: Boolean = true, withSpaceAfterSign: Boolean = true): String {
        decimalFormat.isGroupingUsed = withGrouping
        decimalFormat.minimumFractionDigits = if (withFixedFraction) 2 else 0
        decimalFormat.maximumFractionDigits = if (withFixedFraction) 2 else Int.MAX_VALUE
        decimalFormat.positivePrefix = if (withPositivePrefix && withSpaceAfterSign) "+ " else if (withPositivePrefix) "+" else ""
        decimalFormat.negativePrefix = if (withNegativePrefix && withSpaceAfterSign) "- " else if (withNegativePrefix) "-" else ""
        return decimalFormat.format(money)
    }

    fun formatSavingsRatio(ratio: Float): String {
        return savingsFormat.format(ratio * 100)
    }

    @Deprecated("Old concept")
    fun formatDouble(value: Double): String {
        var stringValue = BigDecimal.valueOf(value).toPlainString()
        while (stringValue.contains('.') && (stringValue.last() == '0' || stringValue.last() == '.')) {
            stringValue = stringValue.dropLast(1)
        }
        return stringValue
    }

}