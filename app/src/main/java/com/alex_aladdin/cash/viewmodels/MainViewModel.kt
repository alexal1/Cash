package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.text.format.DateUtils
import com.alex_aladdin.cash.utils.currentLocale
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())

    private val weekdaySubject = BehaviorSubject.createDefault(Date().toWeekday())
    val weekdayObservable: Observable<Weekday> = weekdaySubject

    val dateConsumer = Consumer<Date> { date -> weekdaySubject.onNext(date.toWeekday())}


    private fun Date.toWeekday(): Weekday {
        val name = weekdayFormat.format(this).capitalize()
        val isToday = DateUtils.isToday(time)
        return Weekday(name, isToday)
    }


    data class Weekday(val name: String, val isToday: Boolean)

}