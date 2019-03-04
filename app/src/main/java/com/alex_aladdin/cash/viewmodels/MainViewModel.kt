package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.ui.chart.ChartData
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.currentLocale
import com.alex_aladdin.cash.utils.onNextConsumer
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CashApp
    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())
    private val dc = DisposableCache()

    private val weekdaySubject = BehaviorSubject.create<Weekday>()
    val weekdayObservable: Observable<Weekday> = weekdaySubject

    private val chartDataSubject = BehaviorSubject.createDefault(ChartData())
    val chartDataObservable: Observable<ChartData> = chartDataSubject

    val dateConsumer = app.currentDate.onNextConsumer()
    val todayDate = app.todayDate


    init {
        app.currentDate.subscribe { date ->
            weekdaySubject.onNext(date.toWeekday())

            // TODO: remove
            val chartData = if (DateUtils.isToday(date.time)) {
                ChartData(
                    gain = mapOf(GainCategories.SALARY to 1000f),
                    loss = mapOf(LossCategories.CAFES_AND_RESTAURANTS to 500f, LossCategories.FOODSTUFF to 250f)
                )
            } else {
                ChartData(
                    gain = mapOf(GainCategories.SALARY to 1000f),
                    loss = mapOf(LossCategories.CAFES_AND_RESTAURANTS to 250f, LossCategories.FOODSTUFF to 500f)
                )
            }
            chartDataSubject.onNext(chartData)
        }.cache(dc)
    }


    private fun Date.toWeekday(): Weekday {
        val name = weekdayFormat.format(this).capitalize()
        val isToday = DateUtils.isToday(time)
        return Weekday(name, isToday)
    }

    override fun onCleared() {
        dc.drain()
    }


    data class Weekday(val name: String, val isToday: Boolean)

}