/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository

import android.os.Handler
import android.os.HandlerThread
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.NumberSpecification
import com.madewithlove.daybalance.repository.specifications.RealmSpecification
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import timber.log.Timber

class TransactionsRepository {

    init {
        Timber.i("Starting RealmThread...")
    }

    private val realmThread = HandlerThread("RealmThread").apply { start() }
    private val realmScheduler = AndroidSchedulers.from(realmThread.looper)

    init {
        Handler(realmThread.looper).post {
            Timber.i("RealmThread has been started successfully")
        }
    }


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
                        val result = realm.copyFromRealm(specification.toRealmResults(realm))
                        emitter.onSuccess(result)
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
        }
        .subscribeOn(realmScheduler)

    fun query(specification: NumberSpecification): Single<Number> = Single
        .create<Number> { emitter ->
            realm().use { realm ->
                realm.executeTransaction {
                    try {
                        val result = specification.toNumber(realm)
                        emitter.onSuccess(result)
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
        }
        .subscribeOn(realmScheduler)

    private fun realm() = Realm.getDefaultInstance()

}