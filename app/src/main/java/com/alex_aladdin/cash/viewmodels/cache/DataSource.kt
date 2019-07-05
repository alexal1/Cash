package com.alex_aladdin.cash.viewmodels.cache

import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.specifications.DaySpecification
import com.alex_aladdin.cash.repository.specifications.RealBalanceSpecification
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*

class DataSource(private val repository: TransactionsRepository, private val currencyManager: CurrencyManager) {

    fun loadMoment(index: Int): Single<MomentData> {
        val date = Date(index * CashApp.millisInDay)
        val currencyIndex = currencyManager.getCurrentCurrencyIndex()

        val transactionsSingle = repository.query(DaySpecification(date, currencyIndex))
        val realBalanceSingle = repository.query(RealBalanceSpecification(date, currencyIndex))

        return Single.zip(
            transactionsSingle,
            realBalanceSingle,
            BiFunction { transactions, realBalance -> MomentData(transactions, realBalance.toDouble()) }
        )
    }
}