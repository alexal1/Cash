/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.ui.TransactionsList
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HistoryViewModel(
    application: Application,
    repository: TransactionsRepository
) : AndroidViewModel(application) {

    val historyStateObservable: Observable<HistoryState>
    val historyState: HistoryState get() = historyStateSubject.value!!

    private val historyStateSubject = BehaviorSubject.createDefault(getDefaultHistoryState())
    private val dc = DisposableCache()


    init {
        historyStateObservable = historyStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }

        repository.query(HistorySpecification())
            .map(this::toItems)
            .subscribe { items ->
                val newState = if (items.isEmpty()) {
                    historyState.copy(items = items, showLoading = false, showEmpty = true)
                } else {
                    historyState.copy(items = items, showLoading = false, showEmpty = false)
                }

                historyStateSubject.onNext(newState)
            }
            .cache(dc)
    }


    override fun onCleared() {
        dc.drain()
    }


    private fun getDefaultHistoryState() = HistoryState(
        showEmpty = false,
        showLoading = true,
        items = emptyList()
    )

    private fun toItems(transactions: List<Transaction>): List<TransactionsList.Item> {
        if (transactions.isEmpty()) {
            return emptyList()
        }

        val result = ArrayList<TransactionsList.Item>()
        var prevTimestamp: Long? = null

        for (transaction in transactions) {
            val timestamp = if (transaction.isGain()) transaction.addedTimestamp else transaction.startTimestamp

            if (prevTimestamp?.isTheSameDayAs(timestamp) != true) {
                val dateItem = TransactionsList.Item.DateItem(Date(timestamp))
                result.add(dateItem)
                prevTimestamp = timestamp
            }

            val transactionItem = TransactionsList.Item.TransactionItem(transaction)
            result.add(transactionItem)
        }

        return result
    }

    private fun Long.isTheSameDayAs(timestamp: Long): Boolean {
        return this / TimeUnit.DAYS.toMillis(1) == timestamp / TimeUnit.DAYS.toMillis(1)
    }


    data class HistoryState(
        val showEmpty: Boolean,
        val showLoading: Boolean,
        val items: List<TransactionsList.Item>
    )

}