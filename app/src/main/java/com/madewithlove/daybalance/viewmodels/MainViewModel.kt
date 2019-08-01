/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.CashApp.Companion.PREFS_AUTO_SWITCH_CURRENCY
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.TipsManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.LastTransactionAnyCurrencySpecification
import com.madewithlove.daybalance.ui.chart.ChartData
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.utils.onNextConsumer
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
import com.madewithlove.daybalance.viewmodels.cache.MomentData
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories
import io.reactivex.Maybe
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
    private val tipsManager: TipsManager by inject()
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

    private val tipsDataSubject = BehaviorSubject.create<Maybe<TipsManager.Tip>>()
    val tipsDataObservable: Observable<Maybe<TipsManager.Tip>> = tipsDataSubject

    private var dataLoadingDisposable: Disposable? = null

    val currentDate = app.currentDate
    val todayDate = app.todayDate


    init {
        currentDate.subscribe { date ->
            weekdaySubject.onNext(date.toWeekday())

            dataLoadingDisposable?.dispose()
            dataLoadingDisposable = cache.requestDate(date).subscribe(dayDataSubject.onNextConsumer())
        }.cache(dc)

        dayDataSubject
            .map { tipsManager.getTip(it.transactions) }
            .subscribe(tipsDataSubject.onNextConsumer())
            .cache(dc)

        tipsManager.resetObservable.subscribe {
            val lastDayData = dayDataSubject.value ?: return@subscribe
            val lastTip = tipsManager.getTip(lastDayData.transactions)
            tipsDataSubject.onNext(lastTip)
        }.cache(dc)
    }


    fun checkCurrencyMismatch() {
        repository
            .query(LastTransactionAnyCurrencySpecification())
            .filter { it.isNotEmpty() }
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

    fun closeTip(tip: TipsManager.Tip) {
        tipsDataSubject.onNext(Maybe.empty())
        tipsManager.closeTip(tip)
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
                    transaction.isGain() && transaction.categoryId == category.id
                }.sumByDouble { transaction ->
                    transaction.getAmountPerDay()
                }.toFloat()
            }
            .filter { it.second > 0f }
            .toMap(),

        loss = LossCategories.values()
            .map { category ->
                category to filter { transaction ->
                    !transaction.isGain() && transaction.categoryId == category.id
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