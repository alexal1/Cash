package com.alex_aladdin.cash.repository

import android.os.HandlerThread
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.repository.specification.TransactionsSpecification
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm

class TransactionsRepository {

    private val realmThread = HandlerThread("RealmThread").apply { start() }
    private val realmScheduler = AndroidSchedulers.from(realmThread.looper)


    fun addTransaction(transaction: Transaction): Completable = Completable
        .create { emitter ->
            realm().use { realm ->
                realm.executeTransaction {
                    try {
                        realm.copyToRealm(transaction)
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
                emitter.onComplete()
            }
        }
        .subscribeOn(realmScheduler)

    fun removeTransaction(transaction: Transaction) {
        // TODO
    }

    fun query(specification: TransactionsSpecification): List<Transaction> = TODO()

    private fun realm() = Realm.getDefaultInstance()

}