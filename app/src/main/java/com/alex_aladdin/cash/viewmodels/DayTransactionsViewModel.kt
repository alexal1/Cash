package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.repository.specifications.DayGainSpecification
import com.alex_aladdin.cash.repository.specifications.DayLossSpecification
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.onNextConsumer
import com.alex_aladdin.cash.viewmodels.cache.CacheLogicAdapter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class DayTransactionsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val currencyManager: CurrencyManager by inject()
    private val repository: TransactionsRepository by inject()
    private val cache: CacheLogicAdapter by inject()
    private val currentCurrencyIndex get() = currencyManager.getCurrentCurrencyIndex()
    private val dc = DisposableCache()

    private val dayLossTransactionsSubject = BehaviorSubject.create<List<Transaction>>()
    val dayLossTransactionsObservable: Observable<List<Transaction>> = dayLossTransactionsSubject
    val dayLossTotalObservable: Observable<Double> = dayLossTransactionsSubject.map {
        it.sumByDouble { transaction -> transaction.getAmountPerDay() }
    }

    private val dayGainTransactionsSubject = BehaviorSubject.create<List<Transaction>>()
    val dayGainTransactionsObservable: Observable<List<Transaction>> = dayGainTransactionsSubject
    val dayGainTotalObservable: Observable<Double> = dayGainTransactionsSubject.map {
        it.sumByDouble { transaction -> transaction.getAmountPerDay() }
    }

    private val activityReadyToDrawSubject = PublishSubject.create<Unit>()
    val activityReadyToDrawListener = activityReadyToDrawSubject.onNextConsumer()

    val currentDateObservable: Observable<Date> = app.currentDate


    init {
        obtainData()
    }


    fun deleteTransaction(transactionId: String) {
        Completable
            .mergeArray(
                cache.clear(),
                repository.removeTransaction(transactionId),
                activityReadyToDrawSubject.take(1).singleOrError().ignoreElement()
            )
            .andThen(cache.requestDate(app.currentDate.value!!))
            .subscribe {
                obtainData()
            }
            .cache(dc)
    }

    private fun obtainData() {
        repository
            .query(DayLossSpecification(app.currentDate.value!!, currentCurrencyIndex))
            .subscribe { transactions ->
                dayLossTransactionsSubject.onNext(transactions)
            }
            .cache(dc)

        repository
            .query(DayGainSpecification(app.currentDate.value!!, currentCurrencyIndex))
            .subscribe { transactions ->
                dayGainTransactionsSubject.onNext(transactions)
            }
            .cache(dc)
    }

    override fun onCleared() {
        dc.drain()
    }

}