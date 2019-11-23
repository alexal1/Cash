/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*

class SavingsManager(private val sharedPreferences: SharedPreferences) {

    companion object {

        private const val PREFS_SAVINGS_PREFIX = "savings_for_"
        private const val DEFAULT_SAVINGS_RATIO = 0.15f

    }


    private val monthIdFormatter = SimpleDateFormat("MM_YYYY", Locale.US)


    fun getSavingsForMonth(monthFirstDay: Date): Float {
        return sharedPreferences.getFloat(monthFirstDay.toPrefsKey(), DEFAULT_SAVINGS_RATIO)
    }

    fun setSavingsForMonth(monthFirstDay: Date, savingsRatio: Float) {
        sharedPreferences.edit {
            putFloat(monthFirstDay.toPrefsKey(), savingsRatio)
        }
    }


    private fun Date.toPrefsKey(): String {
        return "$PREFS_SAVINGS_PREFIX${monthIdFormatter.format(this)}"
    }

}