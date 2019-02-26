package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.text.format.DateUtils
import com.alex_aladdin.cash.ui.chart.ChartData
import com.alex_aladdin.cash.utils.currentLocale
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())

    private val weekdaySubject = BehaviorSubject.createDefault(Date().toWeekday())
    val weekdayObservable: Observable<Weekday> = weekdaySubject

    private val chartDataSubject = BehaviorSubject.createDefault(ChartData())
    val chartDataObservable: Observable<ChartData> = chartDataSubject

    val dateConsumer = Consumer<Date> { date -> weekdaySubject.onNext(date.toWeekday())}


    private fun Date.toWeekday(): Weekday {
        val name = weekdayFormat.format(this).capitalize()
        val isToday = DateUtils.isToday(time)
        return Weekday(name, isToday)
    }


    init {
        // TODO: remove

        val testChartData = ChartData(
            gain = mapOf(GainCategories.SALARY to 1000f),
            loss = mapOf(LossCategories.CAFES_AND_RESTAURANTS to 500f, LossCategories.FOODSTUFF to 250f)
        )

        chartDataSubject.onNext(testChartData)
    }


    data class Weekday(val name: String, val isToday: Boolean)

}