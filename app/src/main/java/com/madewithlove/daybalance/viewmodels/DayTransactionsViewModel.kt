/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.DayGainSpecification
import com.madewithlove.daybalance.repository.specifications.DayLossSpecificationOld
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.onNextConsumer
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.ArrayList

class DayTransactionsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val currencyManager: CurrencyManager by inject()
    private val repository: TransactionsRepository by inject()
    private val cache: CacheLogicAdapter by inject()
    private val analytics: Analytics by inject()
    private val currentCurrencyIndex get() = currencyManager.getCurrentCurrencyIndex()
    private val dc = DisposableCache()

    private val dayLossTransactionsSubject = BehaviorSubject.create<List<Transaction>>()
    val dayLossTransactionsItemsObservable: Observable<List<Item>> = dayLossTransactionsSubject.map(this::toItems)

    private val dayGainTransactionsSubject = BehaviorSubject.create<List<Transaction>>()
    val dayGainTransactionsItemsObservable: Observable<List<Item>> = dayGainTransactionsSubject.map(this::toItems)

    private val activityReadyToDrawSubject = PublishSubject.create<Unit>()
    val activityReadyToDrawListener = activityReadyToDrawSubject.onNextConsumer()

    val currentDateObservable: Observable<Date> = app.currentDate


    init {
        obtainData()
    }


    fun deleteTransaction(transactionId: String) {
        analytics.deleteTransaction()

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
            .query(DayLossSpecificationOld(app.currentDate.value!!, currentCurrencyIndex))
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

    private fun toItems(transactions: List<Transaction>): List<Item> {
        if (transactions.isEmpty()) {
            return listOf(Item.NoDataItem())
        }

        val result = ArrayList<Item>()
        var prevTimestamp: Long? = null
        var totalAmount = 0.0

        for (transaction in transactions) {
            if (transaction.startTimestamp != prevTimestamp) {
                val dateItem = Item.DateItem(Date(transaction.startTimestamp))
                result.add(dateItem)
                prevTimestamp = transaction.startTimestamp
            }

            val transactionItem = Item.TransactionItem(transaction)
            result.add(transactionItem)

            totalAmount += transaction.getAmountPerDay()
        }

        val totalItem = Item.TotalItem(totalAmount)
        result.add(totalItem)

        return result
    }

    override fun onCleared() {
        dc.drain()
    }


    sealed class Item(val type: Int) {

        companion object {
            const val TRANSACTION_TYPE = 0
            const val DATE_TYPE = 1
            const val TOTAL_TYPE = 2
            const val NO_DATA_TYPE = 3
        }

        data class TransactionItem(val transaction: Transaction) : Item(TRANSACTION_TYPE)

        data class DateItem(val date: Date) : Item(DATE_TYPE)

        class TotalItem(val total: Double) : Item(TOTAL_TYPE) {
            override fun equals(other: Any?): Boolean {
                return this === other || javaClass == other?.javaClass
            }

            override fun hashCode(): Int = 0
        }

        class NoDataItem : Item(NO_DATA_TYPE) {
            override fun equals(other: Any?): Boolean {
                return this === other || javaClass == other?.javaClass
            }

            override fun hashCode(): Int = 0
        }

    }

}