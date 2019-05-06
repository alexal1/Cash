package com.alex_aladdin.cash.helpers.enums

import androidx.annotation.StringRes
import com.alex_aladdin.cash.R

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
    ONE_DAY(R.string.period_one_day, R.string.period_one_day_full)

}