/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.model

import com.madewithlove.daybalance.utils.CalendarFactory
import java.util.*
import java.util.concurrent.TimeUnit

class DaysIterator(
    startDate: Date,
    private val endDate: Date
) : Iterator<Date> {

    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val calendar = CalendarFactory.getInstance()

    val count = ((endDate.time - startDate.time) / millisInDay).toInt()


    init {
        require(startDate.time % millisInDay == 0L && endDate.time % millisInDay == 0L) {
            "Only dates in GMT+00:00 with hours, minutes, seconds, milliseconds set to zero are accepted here"
        }

        calendar.time = startDate
    }


    override fun hasNext(): Boolean {
        return calendar.time < endDate
    }

    override fun next(): Date {
        val next = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return next
    }

}