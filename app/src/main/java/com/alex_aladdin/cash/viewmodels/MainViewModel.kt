package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.ui.chart.ChartData
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.currentLocale
import com.alex_aladdin.cash.utils.onNextConsumer
import com.alex_aladdin.cash.viewmodels.cache.CacheLogicAdapter
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val cache: CacheLogicAdapter by inject()
    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())
    private val dc = DisposableCache()

    private val weekdaySubject = BehaviorSubject.create<Weekday>()
    val weekdayObservable: Observable<Weekday> = weekdaySubject

    private val dayTransactionsSubject = BehaviorSubject.create<List<Transaction>>()
    val shortTransactionsListObservable: Observable<List<Transaction>> = dayTransactionsSubject.map { it.take(2) }
    val chartDataObservable: Observable<ChartData> = Observable.merge(
        Observable.just(ChartData()),
        dayTransactionsSubject.map { transactions -> transactions.toChartData() }
    )

    private var dataLoadingDisposable: Disposable? = null

    val dateConsumer = app.currentDate.onNextConsumer()
    val todayDate = app.todayDate


    init {
        app.currentDate.subscribe { date ->
            weekdaySubject.onNext(date.toWeekday())

            dataLoadingDisposable?.dispose()
            dataLoadingDisposable = cache.requestDate(date).subscribe(dayTransactionsSubject.onNextConsumer())
        }.cache(dc)
    }


    private fun Date.toWeekday(): Weekday {
        val name = weekdayFormat.format(this).capitalize()
        val isToday = DateUtils.isToday(time)
        return Weekday(name, isToday)
    }

    private fun List<Transaction>.toChartData() = ChartData(
        gain = GainCategories.values().map { category ->
            category to filter { transaction ->
                transaction.categoryId == category.id
            }.sumByDouble { transaction ->
                transaction.amount
            }.toFloat()
        }.toMap(),

        loss = LossCategories.values().map { category ->
            category to filter { transaction ->
                transaction.categoryId == category.id
            }.sumByDouble { transaction ->
                transaction.amount
            }.toFloat()
        }.toMap()
    )

    override fun onCleared() {
        dc.drain()
        dataLoadingDisposable?.dispose()
    }


    data class Weekday(val name: String, val isToday: Boolean)

}