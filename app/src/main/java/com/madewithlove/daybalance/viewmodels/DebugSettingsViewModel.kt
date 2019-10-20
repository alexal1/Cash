/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.CashApp.Companion.PREFS_LOGS_ENABLED
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.utils.RandomTransactionsIterator
import com.madewithlove.daybalance.repository.utils.RandomTransactionsIteratorFactory
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class DebugSettingsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    companion object {

        private const val TRANSACTIONS_COUNT = 100

    }


    private val app = application as CashApp
    private val pushManager: PushManager by inject()
    private val repository: TransactionsRepository by inject()
    private val cache: CacheLogicAdapter by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private val randomDayTransactionsIteratorFactory: RandomTransactionsIteratorFactory by inject { parametersOf(TRANSACTIONS_COUNT, RandomTransactionsIterator.Mode.THIS_DAY) }
    private val randomMonthTransactionsIteratorFactory: RandomTransactionsIteratorFactory by inject { parametersOf(TRANSACTIONS_COUNT, RandomTransactionsIterator.Mode.THIS_MONTH) }


    fun showPush() {
        pushManager.showPushNotification(true)
    }

    fun addTransactionsToCurrentDate(): Completable {
        val randomDayTransactionsIterator = randomDayTransactionsIteratorFactory.getInstance()

        return cache
            .clear()
            .andThen(repository.addAllTransactions(randomDayTransactionsIterator))
            .andThen(cache.requestDate(app.currentDate.value!!))
            .take(1)
            .singleOrError()
            .ignoreElement()
    }

    fun addTransactionsToCurrentMonth(): Completable {
        val randomMonthTransactionsIterator = randomMonthTransactionsIteratorFactory.getInstance()

        return cache
            .clear()
            .andThen(repository.addAllTransactions(randomMonthTransactionsIterator))
            .andThen(cache.requestDate(app.currentDate.value!!))
            .take(1)
            .singleOrError()
            .ignoreElement()
    }

    fun wipe(): Completable = cache
        .clear()
        .andThen(repository.removeAllTransactions())
        .andThen(cache.requestDate(app.currentDate.value!!))
        .take(1)
        .singleOrError()
        .ignoreElement()

    fun areLogsEnabled(): Boolean = sharedPreferences.getBoolean(PREFS_LOGS_ENABLED, false)

    fun setLogsEnabled(enabled: Boolean) = sharedPreferences.edit {
        putBoolean(PREFS_LOGS_ENABLED, enabled)
    }

}