package com.alex_aladdin.cash.repository

import android.os.HandlerThread
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.repository.specifications.RealmSpecification
import io.reactivex.Completable
import io.reactivex.Single
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
                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
        }
        .subscribeOn(realmScheduler)

    fun removeTransaction(transactionId: String): Completable = Completable
        .create { emitter ->
            realm().use { realm ->
                realm.executeTransaction {
                    try {
                        realm
                            .where(Transaction::class.java)
                            .equalTo("id", transactionId)
                            .findAll()
                            .deleteAllFromRealm()

                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
        }

    fun removeAllTransactions(): Completable = Completable
        .create { emitter ->
            realm().use { realm ->
                realm.executeTransaction {
                    try {
                        realm
                            .where(Transaction::class.java)
                            .findAll()
                            .deleteAllFromRealm()

                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
        }

    fun query(specification: RealmSpecification): Single<List<Transaction>> = Single
        .create<List<Transaction>> { emitter ->
            realm().use { realm ->
                realm.executeTransaction {
                    try {
                        emitter.onSuccess(realm.copyFromRealm(specification.toRealmResults(realm)))
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
        }
        .subscribeOn(realmScheduler)

    private fun realm() = Realm.getDefaultInstance()

}