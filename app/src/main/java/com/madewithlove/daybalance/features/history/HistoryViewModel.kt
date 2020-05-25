/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.model.BalanceLogic
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.ui.TransactionsList
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class HistoryViewModel(
    application: Application,
    private val repository: TransactionsRepository,
    private val balanceLogic: BalanceLogic,
    private val analytics: Analytics
) : AndroidViewModel(application) {

    val historyStateObservable: Observable<HistoryState>
    val historyState: HistoryState get() = historyStateSubject.value!!
    val checkConsumer: Consumer<TransactionsList.Item.TransactionItem> = Consumer(this::handleCheck)
    val uncheckConsumer: Consumer<TransactionsList.Item.TransactionItem> = Consumer(this::handleUncheck)
    val openHistorySubject = PublishSubject.create<Unit>()

    private val historyStateSubject = BehaviorSubject.createDefault(getDefaultHistoryState())
    private val dc = DisposableCache()


    init {
        historyStateObservable = historyStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        repository.realmChangedObservable
            .mergeWith(Observable.just(Unit))
            .flatMapSingle {
                val specification = HistorySpecification(historyState.filter)
                repository.query(specification)
            }
            .map(this::toItems)
            .subscribe { items ->
                val newState = historyState.copy(items = items)
                historyStateSubject.onNext(newState)
            }
            .cache(dc)
    }


    fun setFilter(filter: HistorySpecification.Filter) {
        if (filter == historyState.filter) {
            return
        }

        repository.query(HistorySpecification(filter))
            .map(this::toItems)
            .subscribe { items ->
                val newState = historyState.copy(items = items, filter = filter)
                historyStateSubject.onNext(newState)
            }
            .cache(dc)
    }

    fun deleteCheckedItems() {
        val affectedDates = historyState.checkedTransactions
            .map { Date(it.actionTimestamp) }
            .toSet()

        repository
            .removeAllTransactions(historyState.checkedTransactions.map { it.id })
            .andThen(balanceLogic.invalidate(affectedDates))
            .subscribe {
                val transactionsCount = historyState.checkedTransactions.size
                val newState = historyState.copy(
                    items = historyState.items.removeChecked(),
                    checkedTransactions = emptySet()
                )
                historyStateSubject.onNext(newState)

                analytics.deleteTransactions(count = transactionsCount)
            }
            .cache(dc)
    }

    fun dismissDeleteMode() {
        if (!historyState.deleteModeOn) {
            return
        }

        val newState = historyState.copy(
            items = historyState.items.map { item ->
                if (item is TransactionsList.Item.TransactionItem) {
                    item.isChecked = false
                }

                item
            },
            checkedTransactions = emptySet()
        )

        historyStateSubject.onNext(newState)
    }


    override fun onCleared() {
        dc.drain()
    }


    private fun getDefaultHistoryState() = HistoryState(
        items = emptyList(),
        checkedTransactions = emptySet(),
        filter = HistorySpecification.Empty
    )

    private fun toItems(transactions: List<Transaction>): List<TransactionsList.Item> {
        if (transactions.isEmpty()) {
            return emptyList()
        }

        val result = LinkedList<TransactionsList.Item>()
        var prevTimestamp: Long? = null

        for (transaction in transactions) {
            val timestamp = transaction.displayTimestamp

            if (prevTimestamp?.isTheSameDayAs(timestamp) != true) {
                val dateItem = TransactionsList.Item.DateItem(Date(timestamp))
                result.add(dateItem)
                prevTimestamp = timestamp
            }

            val transactionItem = historyState.items
                .find {
                    it is TransactionsList.Item.TransactionItem && it.transaction == transaction
                }
                ?: TransactionsList.Item.TransactionItem(transaction)
            result.add(transactionItem)
        }

        return result
    }

    private fun List<TransactionsList.Item>.removeChecked(): List<TransactionsList.Item> {
        val result = LinkedList<TransactionsList.Item>()
        val itemsOnSingleDate = LinkedList<TransactionsList.Item>()

        forEach { item ->
            when (item) {
                is TransactionsList.Item.DateItem -> {
                    if (itemsOnSingleDate.size > 1) {
                        result.addAll(itemsOnSingleDate)
                    }

                    itemsOnSingleDate.clear()
                    itemsOnSingleDate.add(item)
                }

                is TransactionsList.Item.TransactionItem -> {
                    if (!item.isChecked) {
                        itemsOnSingleDate.add(item)
                    }
                }
            }
        }

        if (itemsOnSingleDate.size > 1) {
            result.addAll(itemsOnSingleDate)
        }

        return result
    }

    private fun Long.isTheSameDayAs(timestamp: Long): Boolean {
        return this / TimeUnit.DAYS.toMillis(1) == timestamp / TimeUnit.DAYS.toMillis(1)
    }

    private fun handleCheck(transactionItem: TransactionsList.Item.TransactionItem) {
        transactionItem.isChecked = true

        val newState = historyState.copy(checkedTransactions = historyState.checkedTransactions + transactionItem.transaction)
        historyStateSubject.onNext(newState)
    }

    private fun handleUncheck(transactionItem: TransactionsList.Item.TransactionItem) {
        transactionItem.isChecked = false

        val newState = historyState.copy(checkedTransactions = historyState.checkedTransactions - transactionItem.transaction)
        historyStateSubject.onNext(newState)
    }


    data class HistoryState(
        val items: List<TransactionsList.Item>,
        val checkedTransactions: Set<Transaction>,
        val filter: HistorySpecification.Filter
    ) {
        val showEmpty: Boolean get() = items.isEmpty()
        val deleteModeOn: Boolean get() = checkedTransactions.isNotEmpty()
    }

}