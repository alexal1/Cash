/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers.enums

import android.content.Context
import androidx.annotation.StringRes
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.enums.Periods.*
import timber.log.Timber
import java.util.*
import java.util.Calendar.*

enum class Periods(@StringRes val shortString: Int, @StringRes val fullString: Int) {

    SINGLE(R.string.period_single, R.string.period_single_full),
    TWENTY_YEARS(R.string.period_twenty_years, R.string.period_twenty_years_full),
    TEN_YEARS(R.string.period_ten_years, R.string.period_ten_years_full),
    THREE_YEARS(R.string.period_three_years, R.string.period_three_years_full),
    ONE_YEAR(R.string.period_one_year, R.string.period_one_year_full),
    THREE_MONTHS(R.string.period_three_months, R.string.period_three_months_full),
    ONE_MONTH(R.string.period_one_month, R.string.period_one_month_full),
    TWO_WEEKS(R.string.period_two_weeks, R.string.period_two_weeks_full),
    ONE_WEEK(R.string.period_one_week, R.string.period_one_week_full),
    THREE_DAYS(R.string.period_three_days, R.string.period_three_days_full),
    ONE_DAY(R.string.period_one_day, R.string.period_one_day_full);

    fun getApproximateString(context: Context): String = if (this == SINGLE) {
        context.getString(shortString)
    } else {
        "~ ${context.getString(shortString)}"
    }


    companion object {

        fun getByName(name: String): Periods = try {
            valueOf(name)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            ONE_DAY
        }

    }

}


fun Periods.getDateIncrement(locale: Locale, date: Date): Date {
    val calendar = GregorianCalendar.getInstance(locale)
    calendar.time = date

    when (this) {
        SINGLE -> calendar.add(DAY_OF_MONTH, 1)
        TWENTY_YEARS -> calendar.add(YEAR, 20)
        TEN_YEARS -> calendar.add(YEAR, 10)
        THREE_YEARS -> calendar.add(YEAR, 3)
        ONE_YEAR -> calendar.add(YEAR, 1)
        THREE_MONTHS -> calendar.add(MONTH, 3)
        ONE_MONTH -> calendar.add(MONTH, 1)
        TWO_WEEKS -> calendar.add(WEEK_OF_YEAR, 2)
        ONE_WEEK -> calendar.add(WEEK_OF_YEAR, 1)
        THREE_DAYS -> calendar.add(DAY_OF_MONTH, 3)
        ONE_DAY -> calendar.add(DAY_OF_MONTH, 1)
    }

    return calendar.time
}