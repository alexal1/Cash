package com.madewithlove.daybalance.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.helpers.CategoriesManager
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.enums.getDateIncrement
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Account
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
import com.madewithlove.daybalance.viewmodels.enums.Categories
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.Calendar.*

class DebugSettingsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    companion object {

        private const val TRANSACTIONS_COUNT = 100

    }


    private val app = application as CashApp
    private val currentDate = app.currentDate.value!!
    private val pushManager: PushManager by inject()
    private val repository: TransactionsRepository by inject()
    private val cache: CacheLogicAdapter by inject()
    private val categoriesManager: CategoriesManager by inject()
    private val currencyManager: CurrencyManager by inject()
    private val random = Random()


    fun showPush() {
        pushManager.showPushNotification(true)
    }

    fun addTransactionsToCurrentDate(): Completable {
        val completables = Array(TRANSACTIONS_COUNT) {
            addRandomTransactionToDate(currentDate)
        }

        return cache
            .clear()
            .andThen(Completable.mergeArray(*completables))
            .andThen(cache.requestDate(app.currentDate.value!!))
            .take(1)
            .singleOrError()
            .ignoreElement()
    }

    fun addTransactionsToCurrentMonth(): Completable {
        val completables = arrayListOf<Completable>()

        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+0000")).apply {
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
        }

        val currentMonth = calendar.get(MONTH)

        while (calendar.get(MONTH) == currentMonth) {
            repeat(TRANSACTIONS_COUNT) {
                completables.add(addRandomTransactionToDate(calendar.time))
            }
            calendar.add(DAY_OF_MONTH, 1)
        }

        return cache
            .clear()
            .andThen(Completable.mergeArray(*completables.toTypedArray()))
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


    private fun addRandomTransactionToDate(date: Date): Completable {
        val transaction = Transaction().apply {
            amount = random.nextDouble() * 2000 - 1000

            val category: Categories = if (isGain()) {
                random.nextInt(GainCategories.values().count()).let { index ->
                    GainCategories.values()[index]
                }
            } else {
                random.nextInt(LossCategories.values().count()).let { index ->
                    LossCategories.values()[index]
                }
            }

            categoryId = category.id
            period = categoriesManager.getPeriod(category).name
            startTimestamp = date.time

            endTimestamp = categoriesManager
                .getPeriod(category)
                .getDateIncrement(app.currentLocale(), app.currentDate.value!!)
                .time

            addedTimestamp = System.currentTimeMillis()

            account = Account().apply {
                currencyIndex = currencyManager.getCurrentCurrencyIndex()
            }
        }

        return repository.addTransaction(transaction)
    }

}