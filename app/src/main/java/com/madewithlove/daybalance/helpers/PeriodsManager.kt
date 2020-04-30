/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.helpers

import com.madewithlove.daybalance.utils.CalendarFactory
import java.util.*

class PeriodsManager {

    private val calendar = CalendarFactory.getInstance()


    /**
     * Returns the first day (inclusive) and the last day (exclusive) of containing month.
     */
    fun getMonthBoundaries(day: Date): Pair<Date, Date> {
        synchronized(calendar) {
            calendar.time = day
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val monthFirstDay = calendar.time

            calendar.add(Calendar.MONTH, 1)
            val nextMonthFirstDay = calendar.time

            return monthFirstDay to nextMonthFirstDay
        }
    }

}