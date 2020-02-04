/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import com.madewithlove.daybalance.utils.CalendarFactory
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit

class DatesManager {

    val extendedDateObservable: Observable<ExtendedDate>
    val currentDateObservable: Observable<Date>
    val isTodayObservable: Observable<Boolean>
    val extendedDate: ExtendedDate get() = extendedDateSubject.value!!
    val currentDate: Date get() = extendedDate.date
    val todayDate: Date get() = now

    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val now get() = Date(System.currentTimeMillis() / millisInDay * millisInDay)
    private val extendedNow get() = ExtendedDate(now, 0)
    private val extendedDateSubject = BehaviorSubject.createDefault(extendedNow)
    private val calendar = CalendarFactory.getInstance()


    init {
        extendedDateObservable = extendedDateSubject.distinctUntilChanged()
        currentDateObservable = extendedDateSubject.map { it.date }.distinctUntilChanged()
        isTodayObservable = extendedDateSubject.map { it.isToday }.distinctUntilChanged()

        Timer("DatesManagerThread").schedule(
            object : TimerTask() {
                override fun run() {
                    extendedDateSubject.onNext(extendedNow)
                }
            },
            Date(now.time - TimeZone.getDefault().rawOffset + millisInDay),
            millisInDay
        )
    }


    fun updateCurrentDate(date: Date) {
        require(date.time % millisInDay == 0L) {
            "Only dates in GMT+00:00 with hours, minutes, seconds, milliseconds set to zero are accepted here"
        }

        val extendedDate = ExtendedDate(date, getTodayRelation(date))
        extendedDateSubject.onNext(extendedDate)
    }

    fun getCurrentMonthFirstDay(): Date {
        calendar.time = extendedDate.date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }

    fun getThisMonthFirstDay(): Date {
        calendar.time = now
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }


    private fun getTodayRelation(date: Date): Byte {
        val timeZoneOffset = TimeZone.getDefault().rawOffset
        val todayStart = now.time - timeZoneOffset
        val todayEnd = todayStart + millisInDay

        return when {
            date.time < todayStart -> -1
            date.time >= todayEnd -> 1
            else -> 0
        }
    }


    /**
     * Class that represents complete info about date.
     *
     * @property date GMT+00:00 date with hours, minutes, seconds, milliseconds set to zero.
     */
    data class ExtendedDate(val date: Date, private val todayRelation: Byte) {

        val isPast: Boolean = todayRelation < 0
        val isFuture: Boolean = todayRelation > 0
        val isToday: Boolean = !isPast && !isFuture

    }

}