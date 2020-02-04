/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.model

import com.madewithlove.daybalance.utils.CalendarFactory
import java.util.*
import java.util.concurrent.TimeUnit

class CacheDatesMapper {

    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val calendar = CalendarFactory.getInstance()


    /**
     * Map Date into two indices: index of month (first) and global index of day (second).
     */
    fun map(date: Date): Pair<Int, Int> {
        require(date.time % millisInDay == 0L) {
            "Only dates in GMT+00:00 with hours, minutes, seconds, milliseconds set to zero are accepted here"
        }

        val dayIndex = (date.time / millisInDay).toInt()

        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val monthIndex = (year - 1970) * 12 + month

        return monthIndex to dayIndex
    }

}