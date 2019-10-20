/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.utils

import android.content.Context
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.helpers.CategoriesManager
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.enums.getDateIncrement
import com.madewithlove.daybalance.repository.entities.Account
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.utils.CalendarFactory
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.viewmodels.enums.Categories
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories
import java.util.*

class RandomTransactionsIterator(
    context: Context,
    private val categoriesManager: CategoriesManager,
    private val currencyManager: CurrencyManager,
    private val countPerDay: Int,
    private val mode: Mode
) : Iterator<Transaction> {

    private val app = context.applicationContext as CashApp
    private val random = Random()
    private val calendar = CalendarFactory.getInstance(day = 1)
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentDate = app.currentDate.value!!
    private val transaction = Transaction()

    private var timesThisDay = 0


    override fun hasNext(): Boolean {
        return when (mode) {
            Mode.THIS_DAY -> timesThisDay < countPerDay
            Mode.THIS_MONTH -> calendar.get(Calendar.MONTH) == currentMonth
        }
    }

    override fun next(): Transaction {
        if (!hasNext()) {
            throw Exception("next() invoked while hasNext() returns false!")
        }

        when (mode) {
            Mode.THIS_DAY -> {
                timesThisDay++
                fillWithRandomData(transaction, currentDate)
            }

            Mode.THIS_MONTH -> {
                if (timesThisDay < countPerDay) {
                    timesThisDay++
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    timesThisDay = 1
                }

                fillWithRandomData(transaction, calendar.time)
            }
        }

        return transaction
    }


    private fun fillWithRandomData(transaction: Transaction, date: Date) = transaction.apply {
        id = UUID.randomUUID().toString()

        amount = random.nextDouble() * 200 - 100

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


    enum class Mode { THIS_DAY, THIS_MONTH }

}