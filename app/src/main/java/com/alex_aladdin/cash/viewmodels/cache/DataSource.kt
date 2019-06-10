package com.alex_aladdin.cash.viewmodels.cache

import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.repository.specifications.DaySpecification
import io.reactivex.Single
import java.util.*

class DataSource(private val repository: TransactionsRepository, private val currencyManager: CurrencyManager) {

    fun loadMoment(index: Int): Single<List<Transaction>> {
        val date = Date(index * CashApp.millisInDay)
        val currencyIndex = currencyManager.getCurrentCurrencyIndex()
        return repository.query(DaySpecification(date, currencyIndex))
    }
}