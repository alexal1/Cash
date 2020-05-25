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
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.realm.Realm

class TransactionsRepositoryImpl : TransactionsRepository {

    private val realmChangedSubject = PublishSubject.create<Unit>()
    private val realmThread = HandlerThread("RealmThread").apply { start() }
    private val realmScheduler = AndroidSchedulers.from(realmThread.looper)
    private var realmInstance: Realm? = null

    override val realmChangedObservable: Observable<Unit> = realmChangedSubject


    override fun addTransaction(transaction: Transaction): Completable = Completable
        .create { emitter ->
            getRealm().executeTransaction { realm ->
                try {
                    realm.insert(transaction)
                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
        .subscribeOn(realmScheduler)
        .doOnComplete {
            realmChangedSubject.onNext(Unit)
        }

    fun addAllTransactions(iterator: Iterator<Transaction>): Completable = Completable
        .create { emitter ->
            getRealm().executeTransaction { realm ->
                try {
                    while (iterator.hasNext()) {
                        val transaction = iterator.next()
                        realm.insert(transaction)
                    }

                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
        .subscribeOn(realmScheduler)
        .doOnComplete {
            realmChangedSubject.onNext(Unit)
        }

    fun removeTransaction(transactionId: String): Completable = Completable
        .create { emitter ->
            getRealm().executeTransaction { realm ->
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
        .subscribeOn(realmScheduler)
        .doOnComplete {
            realmChangedSubject.onNext(Unit)
        }

    override fun removeAllTransactions(transactionIds: List<String>): Completable = Completable
        .create { emitter ->
            getRealm().executeTransaction { realm ->
                try {
                    var realmQuery = realm.where(Transaction::class.java)
                    transactionIds.forEachIndexed { i, transactionId ->
                        realmQuery = realmQuery.equalTo("id", transactionId)

                        if (i < transactionIds.size - 1) {
                            realmQuery = realmQuery.or()
                        }
                    }

                    realmQuery.findAll().deleteAllFromRealm()

                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
        .subscribeOn(realmScheduler)
        .doOnComplete {
            realmChangedSubject.onNext(Unit)
        }

    override fun query(specification: RealmSpecification): Single<List<Transaction>> = Single
        .create<List<Transaction>> { emitter ->
            getRealm().executeTransaction { realm ->
                try {
                    val result = realm.copyFromRealm(specification.toRealmResults(realm))
                    emitter.onSuccess(result)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
        .subscribeOn(realmScheduler)

    override fun query(specification: NumberSpecification): Single<Number> = Single
        .create<Number> { emitter ->
            getRealm().executeTransaction { realm ->
                try {
                    val result = specification.toNumber(realm)
                    emitter.onSuccess(result)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
        .subscribeOn(realmScheduler)

    override fun dispose() {
        Handler(realmThread.looper).post {
            realmInstance?.close()
            realmInstance = null
        }
    }


    private fun getRealm(): Realm {
        if (realmInstance == null) {
            realmInstance = Realm.getDefaultInstance()
        }

        return realmInstance!!
    }

}