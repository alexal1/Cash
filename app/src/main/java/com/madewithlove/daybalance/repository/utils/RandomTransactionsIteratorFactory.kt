/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.utils

import android.content.Context
import com.madewithlove.daybalance.helpers.CategoriesManager
import com.madewithlove.daybalance.helpers.CurrencyManager

class RandomTransactionsIteratorFactory(
    private val context: Context,
    private val categoriesManager: CategoriesManager,
    private val currencyManager: CurrencyManager,
    private val countPerDay: Int,
    private val mode: RandomTransactionsIterator.Mode
) {

    fun getInstance(): RandomTransactionsIterator =
        RandomTransactionsIterator(context, categoriesManager, currencyManager, countPerDay, mode)

}