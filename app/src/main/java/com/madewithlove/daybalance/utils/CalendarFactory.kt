/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import java.util.*

object CalendarFactory {

    fun getInstance(day: Int? = null, month: Int? = null, year: Int? = null): Calendar {
        return GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+0000")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            day?.let {
                set(Calendar.DAY_OF_MONTH, it)
            }

            month?.let {
                set(Calendar.MONTH, it)
            }

            year?.let {
                set(Calendar.YEAR, it)
            }
        }
    }

}