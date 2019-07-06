package com.alex_aladdin.cash.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import com.alex_aladdin.cash.CashApp.Companion.PREFS_CURRENT_CURRENCY_INDEX
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class CurrencyManager(private val sharedPreferences: SharedPreferences, private val locale: Locale) {

    companion object {

        private val currenciesList = listOf(
            "\u20BF", // Bitcoin
            "\u0024", // US dollar"
            "\u20AC", // Euro
            "\u20BD", // Russian ruble
            "\u00A3", // British pound
            "\u20B4", // Ukrainian hryvnia
            "\u00A5", // Yen
            "\u058F", "\u060B", "\u09F2", "\u09F3", "\u0AF1", "\u0BF9", "\u0E3F", "\u17DB", "\u5143", "\uFDFC",
            "\u20A1", "\u20A2", "\u20A3", "\u20A4", "\u20A5", "\u20A6", "\u20A7", "\u20A8", "\u20A9", "\u20AA",
            "\u20AB", "\u20AD", "\u20AE", "\u20AF", "\u20B0", "\u20B2", "\u20B3", "\u20B5", "\u20B6", "\u20B8",
            "\u20B9", "\u20BA", "\u2133", "\u20BC", "\u20BE"
        )

    }


    @Volatile private var currentIndex = -1

    private val decimalFormat = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.CEILING
    }


    fun getCurrenciesList() = currenciesList

    fun getCurrencyIndexByLocale(): Int = when (locale.country) {
        // TODO: maintain more countries
        "RU" -> 1
        else -> 0
    }

    fun getCurrentCurrencyIndex(): Int {
        if (currentIndex >= 0) {
            return currentIndex
        }

        val spIndex = sharedPreferences.getInt(PREFS_CURRENT_CURRENCY_INDEX, -1)
        if (spIndex >= 0) {
            currentIndex = spIndex
            return spIndex
        }

        val localeIndex = getCurrencyIndexByLocale()
        currentIndex = localeIndex
        sharedPreferences.edit {
            putInt(PREFS_CURRENT_CURRENCY_INDEX, localeIndex)
        }

        return localeIndex
    }

    fun setCurrentCurrencyIndex(currencyIndex: Int) {
        currentIndex = currencyIndex
        sharedPreferences.edit {
            putInt(PREFS_CURRENT_CURRENCY_INDEX, currencyIndex)
        }
    }

    fun formatMoney(value: Number?, currencyIndex: Int = getCurrentCurrencyIndex()) = if (value != null) {
        "${decimalFormat.format(value)} ${currenciesList[currencyIndex]}"
    } else {
        "0"
    }

}