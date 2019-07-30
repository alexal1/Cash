/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels.cache

import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.DaySpecification
import com.madewithlove.daybalance.repository.specifications.RealBalanceSpecification
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