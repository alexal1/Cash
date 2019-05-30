package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.repository.specification.DayGainSpecification
import com.alex_aladdin.cash.repository.specification.DayLossSpecification
import io.reactivex.Observable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class DayTransactionsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val currencyManager: CurrencyManager by inject()
    private val repository: TransactionsRepository by inject()
    private val currentCurrencyIndex get() = currencyManager.getCurrentCurrencyIndex()

    val dayLossTransactions: Single<List<Transaction>> = repository
        .query(DayLossSpecification(app.currentDate.value!!, currentCurrencyIndex))
        .toObservable()
        .replay()
        .autoConnect()
        .singleOrError()

    val dayLossTotal: Single<Double> = dayLossTransactions
        .map { it.sumByDouble { transaction -> transaction.getAmountPerDay() } }

    val dayGainTransactions: Single<List<Transaction>> = repository
        .query(DayGainSpecification(app.currentDate.value!!, currentCurrencyIndex))
        .toObservable()
        .replay()
        .autoConnect()
        .singleOrError()

    val dayGainTotal: Single<Double> = dayGainTransactions
        .map { it.sumByDouble { transaction -> transaction.getAmountPerDay() } }

    val currentDateObservable: Observable<Date> = app.currentDate

}