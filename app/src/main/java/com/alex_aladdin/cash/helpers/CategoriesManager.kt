package com.alex_aladdin.cash.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import com.alex_aladdin.cash.CashApp.Companion.PREFS_CATEGORIES_PREFIX
import com.alex_aladdin.cash.CashApp.Companion.PREFS_DEFAULT_GAIN_CATEGORY
import com.alex_aladdin.cash.CashApp.Companion.PREFS_DEFAULT_LOSS_CATEGORY
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.enums.Periods
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories

class CategoriesManager(context: Context, private val sharedPreferences: SharedPreferences) {

    companion object {

        private const val COLOR_LIGHT_MIN = 0.1f
        private const val COLOR_LIGHT_MAX = 0.6f

    }


    val categoriesColors: Map<Categories, Int>

    private val defaultLossColorHsl = FloatArray(3).apply {
        val defaultColor = ContextCompat.getColor(context, R.color.red)
        ColorUtils.colorToHSL(defaultColor, this)
    }

    private val defaultGainColorHsl = FloatArray(3).apply {
        val defaultColor = ContextCompat.getColor(context, R.color.green)
        ColorUtils.colorToHSL(defaultColor, this)
    }

    private var categoriesToPeriods = HashMap<Categories, Periods>()


    init {
        val categoriesColorsMutable = HashMap<Categories, Int>()

        val lossCategoriesCount = LossCategories.values().size.toFloat()
        LossCategories.values().forEachIndexed { index, lossCategory ->
            val light = COLOR_LIGHT_MIN + (COLOR_LIGHT_MAX - COLOR_LIGHT_MIN) * (index + 1).toFloat() / lossCategoriesCount
            defaultLossColorHsl[2] = light
            categoriesColorsMutable[lossCategory] = ColorUtils.HSLToColor(defaultLossColorHsl)
        }

        val gainCategoriesCount = GainCategories.values().size.toFloat()
        GainCategories.values().forEachIndexed { index, lossCategory ->
            val light = COLOR_LIGHT_MIN + (COLOR_LIGHT_MAX - COLOR_LIGHT_MIN) * (index + 1).toFloat() / gainCategoriesCount
            defaultGainColorHsl[2] = light
            categoriesColorsMutable[lossCategory] = ColorUtils.HSLToColor(defaultGainColorHsl)
        }

        categoriesColors = categoriesColorsMutable
    }


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
            LossCategories.CAR_PURCHASE -> Periods.THREE_YEARS
            LossCategories.JEWELRY_AND_ACCESSORIES -> Periods.THREE_YEARS
            LossCategories.DEVICES -> Periods.THREE_YEARS
            LossCategories.HOUSEHOLD_STUFF -> Periods.THREE_YEARS
            LossCategories.TAXES -> Periods.ONE_YEAR
            LossCategories.BANKING_SERVICE -> Periods.ONE_YEAR
            LossCategories.CLOTHES_AND_SHOES -> Periods.ONE_YEAR
            LossCategories.SOFTWARE -> Periods.ONE_YEAR
            LossCategories.YOU_LENT_SOME_MONEY -> Periods.ONE_YEAR
            LossCategories.YOU_PAID_A_LOAN_BACK -> Periods.ONE_YEAR
            LossCategories.EDUCATION -> Periods.ONE_YEAR
            LossCategories.EXPENDABLE_MATERIALS -> Periods.THREE_MONTHS
            LossCategories.TECHNICAL_SERVICE -> Periods.THREE_MONTHS
            LossCategories.BOOKS_FILMS_GAMES -> Periods.THREE_MONTHS
            LossCategories.SPORT -> Periods.ONE_MONTH
            LossCategories.ACCOMMODATION -> Periods.ONE_MONTH
            LossCategories.HOUSEKEEPING_SERVICE -> Periods.ONE_MONTH
            LossCategories.INTERNET_AND_COMMUNICATION -> Periods.ONE_MONTH
            LossCategories.PUBLIC_TRANSPORT_PASSES -> Periods.ONE_MONTH
            LossCategories.BUSINESS -> Periods.ONE_MONTH
            LossCategories.CREDIT_INTEREST -> Periods.ONE_MONTH
            LossCategories.TRIPS -> Periods.TWO_WEEKS
            LossCategories.CAR_RENTAL -> Periods.TWO_WEEKS
            LossCategories.HEALTH_AND_BODY_CARE -> Periods.ONE_WEEK
            LossCategories.FOODSTUFF -> Periods.ONE_WEEK
            LossCategories.CAFES_AND_RESTAURANTS -> Periods.ONE_DAY
            LossCategories.FASTFOOD -> Periods.ONE_DAY
            LossCategories.GIFTS -> Periods.ONE_DAY
            LossCategories.PHILANTHROPY -> Periods.ONE_DAY
            LossCategories.TRAVEL_TICKETS -> Periods.ONE_DAY
            LossCategories.TAXI_AND_CARSHARING -> Periods.ONE_DAY
            LossCategories.MUSEUMS_AND_EXHIBITIONS -> Periods.ONE_DAY
            LossCategories.ENTERTAINMENT -> Periods.ONE_DAY
            LossCategories.OTHER -> Periods.ONE_DAY
        }

        is GainCategories -> when (category) {
            GainCategories.INHERITANCE -> Periods.TWENTY_YEARS
            GainCategories.LOTTERY -> Periods.TWENTY_YEARS
            GainCategories.GIFT -> Periods.TEN_YEARS
            GainCategories.SALE_OF_A_PROPERTY -> Periods.ONE_YEAR
            GainCategories.YOU_BORROWED_SOME_MONEY -> Periods.ONE_YEAR
            GainCategories.YOUR_LOAN_WAS_PAID_BACK -> Periods.ONE_YEAR
            GainCategories.REWARD -> Periods.THREE_MONTHS
            GainCategories.PASSIVE_INCOME -> Periods.THREE_MONTHS
            GainCategories.SCHOLARSHIP_AND_GRANTS -> Periods.THREE_MONTHS
            GainCategories.ALIMONY -> Periods.ONE_MONTH
            GainCategories.DIVIDENDS -> Periods.ONE_MONTH
            GainCategories.BUSINESS -> Periods.ONE_MONTH
            GainCategories.FINANCIAL_AID -> Periods.ONE_MONTH
            GainCategories.SALARY -> Periods.ONE_MONTH
            GainCategories.RENTING_OUT_A_PROPERTY -> Periods.ONE_MONTH
            GainCategories.CASHBACK -> Periods.ONE_MONTH
            GainCategories.DEPOSIT_INTEREST -> Periods.ONE_MONTH
            GainCategories.TUTORING -> Periods.ONE_WEEK
            GainCategories.FREELANCE -> Periods.ONE_WEEK
            GainCategories.PART_TIME_JOB -> Periods.ONE_DAY
            GainCategories.OTHER -> Periods.ONE_DAY
        }

        else -> null
    }

}