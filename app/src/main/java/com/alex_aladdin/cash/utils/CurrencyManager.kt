package com.alex_aladdin.cash.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.*

class CurrencyManager(private val sharedPreferences: SharedPreferences) {

    companion object {

        private const val DEFAULT_CURRENCY_INDEX = "currency_index"

        private val currenciesList = listOf(
            "\u0024", "\u20BD", "\u20AC", "\u00A3", "\u00A5", "\u058F", "\u060B", "\u09F2", "\u09F3", "\u0AF1",
            "\u0BF9", "\u0E3F", "\u17DB", "\u5143", "\uFDFC", "\u20A1", "\u20A2", "\u20A3", "\u20A4", "\u20A5",
            "\u20A6", "\u20A7", "\u20A8", "\u20A9", "\u20AA", "\u20AB", "\u20AD", "\u20AE", "\u20AF", "\u20B0",
            "\u20B2", "\u20B3", "\u20B4", "\u20B5", "\u20B6", "\u20B8", "\u20B9", "\u20BA", "\u2133", "\u20BC",
            "\u20BE", "\u20BF"
        )

    }


    fun getCurrenciesList() = currenciesList

    fun getDefaultCurrencyIndex(locale: Locale): Int {
        val spIndex = sharedPreferences.getInt(DEFAULT_CURRENCY_INDEX, -1)
        if (spIndex > 0) {
            return spIndex
        }

        val localeIndex = when (locale.country) {
            // TODO: maintain more countries
            "RU" -> 1
            else -> 0
        }

        sharedPreferences.edit {
            putInt(DEFAULT_CURRENCY_INDEX, localeIndex)
        }

        return localeIndex
    }

}