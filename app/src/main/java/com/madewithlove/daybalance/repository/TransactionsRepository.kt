/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.repository

import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.NumberSpecification
import com.madewithlove.daybalance.repository.specifications.RealmSpecification
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface TransactionsRepository {

    val realmChangedObservable: Observable<Unit>


    fun addTransaction(transaction: Transaction): Completable

    fun removeAllTransactions(transactionIds: List<String> = emptyList()): Completable

    fun query(specification: RealmSpecification): Single<List<Transaction>>

    fun query(specification: NumberSpecification): Single<Number>

    fun dispose()

}