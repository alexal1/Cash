package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.CashApp.Companion.PREFS_AUTO_SWITCH_CURRENCY
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.repository.specifications.LastTransactionAnyCurrencySpecification
import com.alex_aladdin.cash.ui.chart.ChartData
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.currentLocale
import com.alex_aladdin.cash.utils.onNextConsumer
import com.alex_aladdin.cash.viewmodels.cache.CacheLogicAdapter
import com.alex_aladdin.cash.viewmodels.cache.MomentData
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val cache: CacheLogicAdapter by inject()
    private val repository: TransactionsRepository by inject()
    private val currencyManager: CurrencyManager by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())
    private val dc = DisposableCache()

    private val weekdaySubject = BehaviorSubject.create<Weekday>()
    val weekdayObservable: Observable<Weekday> = weekdaySubject

    private val dayDataSubject = BehaviorSubject.create<MomentData>()
    val shortTransactionsListObservable: Observable<List<Transaction>> = dayDataSubject.map { it.transactions.take(2) }
    val chartDataObservable: Observable<ChartData> = dayDataSubject.map { it.transactions.toChartData() }
    val realBalanceObservable: Observable<String> = dayDataSubject.map { currencyManager.formatMoney(it.realBalance) }

    private val showMismatchedCurrencyDialogSubject = PublishSubject.create<Int>()
    val showMismatchedCurrencyDialogObservable: Observable<Int> = showMismatchedCurrencyDialogSubject

    private var dataLoadingDisposable: Disposable? = null

    val currentDate = app.currentDate
    val todayDate = app.todayDate


    init {
        currentDate.subscribe { date ->
            weekdaySubject.onNext(date.toWeekday())

            dataLoadingDisposable?.dispose()
            dataLoadingDisposable = cache.requestDate(date).subscribe(dayDataSubject.onNextConsumer())
        }.cache(dc)
    }


    fun checkCurrencyMismatch() {
        repository
            .query(LastTransactionAnyCurrencySpecification())
            .map { it.first() }
            .subscribe { lastTransaction ->
                val transactionCurrencyIndex = lastTransaction.account!!.currencyIndex
                val isCurrencyMismatched = transactionCurrencyIndex != currencyManager.getCurrentCurrencyIndex()

                when (val autoSwitchCurrency = sharedPreferences.getInt(PREFS_AUTO_SWITCH_CURRENCY, 2)) {
                    // yes
                    0 -> if (isCurrencyMismatched) {
                        switchToCurrency(transactionCurrencyIndex)
                    }

                    // no
                    1 -> {
                        // Just do nothing
                    }

                    // ask
                    2 -> if (isCurrencyMismatched) {
                        showMismatchedCurrencyDialogSubject.onNext(transactionCurrencyIndex)
                    }

                    else -> throw IllegalArgumentException("Unexpected autoSwitchCurrency index ($autoSwitchCurrency)")
                }
            }
            .cache(dc)
    }

    fun switchToCurrency(currencyIndex: Int) {
        currencyManager.setCurrentCurrencyIndex(currencyIndex)

        cache
            .clear()
            .andThen(cache.requestDate(app.currentDate.value!!))
            .subscribe()
            .cache(dc)
    }

    private fun Date.toWeekday(): Weekday {
        val name = weekdayFormat.format(this).capitalize()
        val isToday = DateUtils.isToday(time)
        return Weekday(name, isToday)
    }

    private fun List<Transaction>.toChartData() = ChartData(
        gain = GainCategories.values()
            .map { category ->
                category to filter { transaction ->
                    transaction.categoryId == category.id
                }.sumByDouble { transaction ->
                    transaction.getAmountPerDay()
                }.toFloat()
            }
            .filter { it.second > 0f }
            .toMap(),

        loss = LossCategories.values()
            .map { category ->
                category to filter { transaction ->
                    transaction.categoryId == category.id
                }.sumByDouble { transaction ->
                    transaction.getAmountPerDay()
                }.toFloat()
            }
            .filter { it.second > 0f }
            .toMap()
    )

    override fun onCleared() {
        dc.drain()
        dataLoadingDisposable?.dispose()
    }


    data class Weekday(val name: String, val isToday: Boolean)

}