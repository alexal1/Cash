/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import android.text.format.DateUtils
import com.madewithlove.daybalance.utils.CalendarFactory
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class DatesManager {

    /**
     * Time is emitted as GMT+00:00 date with hours, minutes, seconds, milliseconds set to zero.
     */
    val currentDateObservable: Observable<Date>
    val currentDate: Date get() = currentDateSubject.value!!

    /**
     * Whether current date is today in default time zone.
     */
    val isTodayObservable: Observable<Boolean>
    val isToday: Boolean get() = DateUtils.isToday(currentDate.time)

    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val now get() = Date(System.currentTimeMillis() / millisInDay * millisInDay)
    private val currentDateSubject = BehaviorSubject.createDefault(now)
    private val midnightSubject = PublishSubject.create<Unit>()
    private val calendar = CalendarFactory.getInstance()


    init {
        currentDateObservable = currentDateSubject.distinctUntilChanged()

        isTodayObservable = Observable
            .merge(
                currentDateSubject,
                midnightSubject
            )
            .map { isToday }
            .distinctUntilChanged()

        Timer("DatesManagerThread").schedule(
            object : TimerTask() {
                override fun run() {
                    midnightSubject.onNext(Unit)
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

        currentDateSubject.onNext(date)
    }

    fun getCurrentMonthFirstDay(): Date {
        calendar.time = currentDate
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }

    fun getThisMonthFirstDay(): Date {
        calendar.time = now
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }

}