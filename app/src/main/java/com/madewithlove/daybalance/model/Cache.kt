/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.model

import android.os.HandlerThread
import android.util.SparseArray
import com.madewithlove.daybalance.dto.Balance
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.helpers.SavingsManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.DayLossSpecification
import com.madewithlove.daybalance.repository.specifications.MonthRestSpecification
import com.madewithlove.daybalance.repository.specifications.TotalBalanceSpecification
import com.madewithlove.daybalance.utils.CalendarFactory
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class Cache(
    private val datesManager: DatesManager,
    private val savingsManager: SavingsManager,
    private val repository: TransactionsRepository,
    private val datesMapper: CacheDatesMapper
) {

    val balanceObservable: Observable<Balance>

    private val cacheThread = HandlerThread("CacheThread").apply { start() }
    private val cacheScheduler = AndroidSchedulers.from(cacheThread.looper)
    private val balanceSubject = BehaviorSubject.create<Balance>()
    private val calendar = CalendarFactory.getInstance()
    private val dc = DisposableCache()

    /**
     * Day limits indexed by month indices. Limits are equal for all days in each month.
     */
    private val limits = SparseArray<Money>()

    /**
     * Amounts of spent money for each day. Indexed by day global indices.
     */
    private val losses = SparseArray<Money>()


    init {
        balanceObservable = balanceSubject
            .observeOn(cacheScheduler)
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        datesManager.extendedDateObservable
            .observeOn(cacheScheduler)
            .concatMapSingle(this::obtainBalance)
            .subscribe(balanceSubject.onNextConsumer())
            .cache(dc)
    }


    /**
     * Invalidate cached value for the current date and (optionally) cached values for all given
     * dates.
     */
    fun invalidate(dates: Set<Date> = emptySet()): Completable {
        val currentDate = datesManager.currentDate

        return Single
            .fromCallable {
                checkThread()

                (dates + currentDate)
                    .asSequence()
                    .map(datesMapper::map)
                    .forEach { (monthIndex, dayIndex) ->
                        limits.delete(monthIndex)
                        losses.delete(dayIndex)
                    }

                return@fromCallable datesManager.extendedDate
            }
            .subscribeOn(cacheScheduler)
            .flatMap(this::obtainBalance)
            .observeOn(cacheScheduler)
            .doOnSuccess { balance ->
                checkThread()

                val (monthIndex, dayIndex) = datesMapper.map(currentDate)
                limits.put(monthIndex, balance.dayLimit)
                losses.put(dayIndex, balance.dayLoss)

                balanceSubject.onNext(balance)
            }
            .ignoreElement()
    }

    fun dispose() {
        dc.drain()
    }


    private fun obtainBalance(extendedDate: DatesManager.ExtendedDate): Single<Balance> {
        return Single
            .zip<Maybe<Money>, Money, Balance>(
                obtainDayLimit(extendedDate),
                obtainDayLoss(extendedDate.date),
                BiFunction { maybeDayLimit, dayLoss ->
                    val dayLimit = maybeDayLimit.blockingGet()
                    Balance(dayLimit, dayLoss, null)
                }
            )
            .calculateTotalIfNeeded(byDate = extendedDate.date)
    }

    private fun obtainDayLimit(extendedDate: DatesManager.ExtendedDate): Single<Maybe<Money>> {
        checkThread()

        if (extendedDate.isPast) {
            return Single.just(Maybe.empty())
        }

        val (monthIndex, _) = datesMapper.map(extendedDate.date)
        val savedLimit = limits[monthIndex]
        if (savedLimit != null) {
            return Single.just(Maybe.just(savedLimit))
        }

        return calculateDayLimit()
            .observeOn(cacheScheduler)
            .doOnSuccess { limit ->
                checkThread()
                limits.put(monthIndex, limit)
            }
            .flatMap { limitSingle ->
                Single.just(Maybe.just(limitSingle))
            }
    }

    private fun obtainDayLoss(date: Date): Single<Money> {
        checkThread()

        val (_, dayIndex) = datesMapper.map(date)
        val savedCurrent = losses[dayIndex]
        if (savedCurrent != null) {
            return Single.just(savedCurrent)
        }

        return calculateDayLoss(date)
            .observeOn(cacheScheduler)
            .doOnSuccess { current ->
                checkThread()
                losses.put(dayIndex, current)
            }
    }

    private fun calculateDayLimit(): Single<Money> {
        val todayDate = datesManager.todayDate
        val currentMonthFirstDay = datesManager.getCurrentMonthFirstDay()
        val savingsRatio = savingsManager.getSavingsForMonth(currentMonthFirstDay)

        calendar.time = currentMonthFirstDay
        val daysCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendar.add(Calendar.MONTH, 1)
        val nextMonthFirstDay = calendar.time

        return repository
            .query(MonthRestSpecification(todayDate, currentMonthFirstDay, nextMonthFirstDay))
            .map { value ->
                val restMoney = Money.by(value.toLong())
                val limit = restMoney.amount
                    .multiply(BigDecimal.ONE - savingsRatio.toBigDecimal())
                    .divide(daysCount.toBigDecimal(), RoundingMode.HALF_UP)

                Money.by(limit)
            }
    }

    private fun calculateDayLoss(date: Date): Single<Money> {
        return repository
            .query(DayLossSpecification(date))
            .map { value -> Money.by(-value.toLong()) }
    }

    private fun Single<Balance>.calculateTotalIfNeeded(byDate: Date): Single<Balance> = flatMap { balance ->
        if (balance.dayLimit == null) {
            return@flatMap Single.just(balance)
        }

        return@flatMap if (balance.dayLimit.amount - balance.dayLoss.amount < BigDecimal.ZERO) {
            repository
                .query(TotalBalanceSpecification(byDate))
                .map { totalAmount ->
                    val total = Money.by(totalAmount.toLong())
                    balance.copy(total = total)
                }
        } else {
            Single.just(balance)
        }
    }

    private fun checkThread() {
        val currentThread = Thread.currentThread()
        if (currentThread != cacheThread) {
            throw Exception("Running on the wrong thread: ${currentThread.name}")
        }
    }

}