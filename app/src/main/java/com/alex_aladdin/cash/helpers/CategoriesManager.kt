package com.alex_aladdin.cash.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import com.alex_aladdin.cash.CashApp.Companion.PREFS_CATEGORIES_PREFIX
import com.alex_aladdin.cash.CashApp.Companion.PREFS_DEFAULT_GAIN_CATEGORY
import com.alex_aladdin.cash.CashApp.Companion.PREFS_DEFAULT_LOSS_CATEGORY
import com.alex_aladdin.cash.helpers.enums.Periods
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories

class CategoriesManager(private val sharedPreferences: SharedPreferences) {

    private var categoriesToPeriods = HashMap<Categories, Periods>()


    fun getDefaultLossCategory(): LossCategories {
        val spCategory = sharedPreferences.getString(PREFS_DEFAULT_LOSS_CATEGORY, "")
        if (spCategory?.isNotEmpty() == true) {
            return LossCategories.valueOf(spCategory)
        }

        val defaultCategory = LossCategories.CAFES_AND_RESTAURANTS
        sharedPreferences.edit {
            putString(PREFS_DEFAULT_LOSS_CATEGORY, defaultCategory.id)
        }

        return defaultCategory
    }

    fun getDefaultGainCategory(): GainCategories {
        val spCategory = sharedPreferences.getString(PREFS_DEFAULT_GAIN_CATEGORY, "")
        if (spCategory?.isNotEmpty() == true) {
            return GainCategories.valueOf(spCategory)
        }

        val defaultCategory = GainCategories.SALARY
        sharedPreferences.edit {
            putString(PREFS_DEFAULT_GAIN_CATEGORY, defaultCategory.id)
        }

        return defaultCategory
    }

    fun setDefaultCategory(category: Categories) {
        if (category.isGain) {
            sharedPreferences.edit {
                putString(PREFS_DEFAULT_GAIN_CATEGORY, category.id)
            }
        } else {
            sharedPreferences.edit {
                putString(PREFS_DEFAULT_LOSS_CATEGORY, category.id)
            }
        }
    }

    fun getPeriod(category: Categories): Periods {
        val periodFromCache = categoriesToPeriods[category]
        if (periodFromCache != null) {
            return periodFromCache
        }

        val periodFromSharedPrefs = sharedPreferences.getString(PREFS_CATEGORIES_PREFIX + category.id, "")?.takeIf { it.isNotEmpty() }?.let(Periods::valueOf)
        if (periodFromSharedPrefs != null) {
            categoriesToPeriods[category] = periodFromSharedPrefs
            return periodFromSharedPrefs
        }

        val defaultPeriod = getDefaultPeriod(category)
        if (defaultPeriod != null) {
            categoriesToPeriods[category] = defaultPeriod
            sharedPreferences.edit {
                putString(PREFS_CATEGORIES_PREFIX + category.id, defaultPeriod.name)
            }
            return defaultPeriod
        }

        throw Exception("No default period for $category")
    }

    fun setPeriod(category: Categories, period: Periods) {
        categoriesToPeriods[category] = period
        sharedPreferences.edit {
            putString(PREFS_CATEGORIES_PREFIX + category.id, period.name)
        }
    }

    private fun getDefaultPeriod(category: Categories): Periods? = when (category) {
        is LossCategories -> when (category) {
            LossCategories.REAL_ESTATE_PURCHASE -> Periods.TWENTY_YEARS
            LossCategories.FURNITURE_AND_RENOVATION -> Periods.TEN_YEARS
            LossCategories.DEVICES -> Periods.THREE_YEARS
            LossCategories.BANKS_AND_SERVICES -> Periods.ONE_YEAR
            LossCategories.SOME_STUFF -> Periods.ONE_YEAR
            LossCategories.CLOTHES_AND_SHOES -> Periods.ONE_YEAR
            LossCategories.SPORT -> Periods.THREE_MONTHS
            LossCategories.BOOKS_FILMS_GAMES -> Periods.THREE_MONTHS
            LossCategories.ACCOMMODATION -> Periods.ONE_MONTH
            LossCategories.INTERNET_AND_COMMUNICATION -> Periods.ONE_MONTH
            LossCategories.TRAVEL_PASSES -> Periods.ONE_MONTH
            LossCategories.BUSINESS -> Periods.ONE_MONTH
            LossCategories.TRAVELLING -> Periods.TWO_WEEKS
            LossCategories.HEALTH -> Periods.ONE_WEEK
            LossCategories.FOODSTUFF -> Periods.ONE_WEEK
            LossCategories.CAFES_AND_RESTAURANTS -> Periods.ONE_DAY
            LossCategories.FASTFOOD -> Periods.ONE_DAY
            LossCategories.GIFTS -> Periods.ONE_DAY
            LossCategories.PHILANTHROPY -> Periods.ONE_DAY
            LossCategories.TAXI_AND_CARSHARING -> Periods.ONE_DAY
            LossCategories.ENTERTAINMENT -> Periods.ONE_DAY
        }

        is GainCategories -> when (category) {
            GainCategories.SALARY -> Periods.ONE_MONTH
        }

        else -> null
    }

}