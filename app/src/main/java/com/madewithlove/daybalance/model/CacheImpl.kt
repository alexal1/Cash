/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.model

import android.os.Handler
import android.os.HandlerThread
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.helpers.PeriodsManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.repository.specifications.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class CacheImpl(
    private val repository: TransactionsRepository,
    private val periodsManager: PeriodsManager,
    private val datesManager: DatesManager
) : Cache {

    private val cacheThread = HandlerThread("CacheThread").apply { start() }
    private val cacheThreadHandler = Handler(cacheThread.looper)
    private val cacheScheduler = AndroidSchedulers.from(cacheThread.looper)
    private val dayLossSpecifications = HashMap<NumberSpecification, Number>()
    private val monthRestSpecifications = HashMap<NumberSpecification, Number>()
    private val monthTotalGainSpecifications = HashMap<NumberSpecification, Number>()


    override fun query(specification: NumberSpecification): Single<Number> {
        val cache: HashMap<NumberSpecification, Number> = when (specification) {
            is DayLossSpecification -> dayLossSpecifications
            is MonthRestSpecification -> monthRestSpecifications
            is MonthTotalGainSpecification -> monthTotalGainSpecifications
            else -> throw IllegalArgumentException("Unexpected specification for Cache: ${specification::class.java.simpleName}")
        }

        return getCachedValue(cache, specification).flatMap { maybeCachedValue ->
            val cachedValue = maybeCachedValue.blockingGet()
            if (cachedValue != null) {
                Single.just(cachedValue)
            } else {
                repository.query(specification).doOnSuccess { value ->
                    synchronized(cache) {
                        cache[specification] = value
                    }
                    Timber.d("Made request and updated cache for $specification")
                }
            }
        }
    }

    /**
     * Invalidate cached value for the current date and (optionally) cached values for all given
     * dates.
     */
    override fun invalidate(dates: Set<Date>) {
        cacheThreadHandler.post {
            (dates + datesManager.currentDate).forEach { date ->
                val dayLossSpecification = DayLossSpecification(date)
                synchronized(dayLossSpecifications) {
                    dayLossSpecifications.remove(dayLossSpecification)
                }

                val (monthFirstDay, nextMonthFirstDay) = periodsManager.getMonthBoundaries(date)
                val monthRestSpecification = MonthRestSpecification(Date(), monthFirstDay, nextMonthFirstDay)
                synchronized(monthRestSpecifications) {
                    monthRestSpecifications.remove(monthRestSpecification)
                }

                val monthTotalGainSpecification = MonthTotalGainSpecification(monthFirstDay)
                synchronized(monthTotalGainSpecifications) {
                    monthTotalGainSpecifications.remove(monthTotalGainSpecification)
                }

                Timber.i("Cache invalidated for $date")
            }
        }
    }

    override fun dispose() {
        cacheThread.quitSafely()
    }

    override fun addTransaction(transaction: Transaction): Completable {
        throw NotImplementedError("addTransaction is not available in Cache")
    }

    override fun removeAllTransactions(transactionIds: List<String>): Completable {
        throw NotImplementedError("removeAllTransactions is not available in Cache")
    }

    override fun query(specification: RealmSpecification): Single<List<Transaction>> {
        throw NotImplementedError("query(specification: RealmSpecification) is not available in Cache")
    }

    private fun getCachedValue(
        cache: HashMap<NumberSpecification, Number>,
        specification: NumberSpecification
    ): Single<Maybe<Number>> {
        return Single
            .fromCallable {
                val cachedValue = synchronized(cache) {
                    cache[specification]
                }

                if (cachedValue != null) {
                    Timber.i("Found cached value for $specification")
                    Maybe.just(cachedValue)
                } else {
                   Maybe.empty()
                }
            }
            .subscribeOn(cacheScheduler)
    }

}