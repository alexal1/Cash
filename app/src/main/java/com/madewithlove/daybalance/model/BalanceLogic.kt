/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.model

import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.dto.Balance
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.helpers.PeriodsManager
import com.madewithlove.daybalance.helpers.SavingsManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.DayLossSpecification
import com.madewithlove.daybalance.repository.specifications.MonthRestSpecification
import com.madewithlove.daybalance.repository.specifications.MonthTotalGainSpecification
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class BalanceLogic(
    private val repository: TransactionsRepository,
    private val cache: Cache,
    private val savingsManager: SavingsManager,
    private val datesManager: DatesManager,
    private val periodsManager: PeriodsManager
) {

    val balanceObservable: Observable<Balance>

    private val balanceSubject = BehaviorSubject.create<Balance>()
    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val dc = DisposableCache()


    init {
        balanceObservable = balanceSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        datesManager.extendedDateObservable
            .map { it.date }
            .concatMapSingle(this::getBalance)
            .subscribe(balanceSubject.onNextConsumer())
            .cache(dc)
    }


    /**
     * Invalidate cache and recalculate Balance.
     */
    fun invalidate(dates: Set<Date> = emptySet()): Completable {
        cache.invalidate(dates)

        return Single
            .just(datesManager.currentDate)
            .flatMap(this::getBalance)
            .doOnSuccess(balanceSubject.onNextConsumer())
            .ignoreElement()
    }

    fun dispose() {
        dc.drain()
    }


    private fun getBalance(day: Date): Single<Balance> {
        val (monthFirstDay, nextMonthFirstDay) = periodsManager.getMonthBoundaries(day)
        val today = datesManager.todayDate

        return Single.zip(
            getDayLossMoney(day),
            getDayLimitMoney(day),
            getRestMoney(monthFirstDay, nextMonthFirstDay, today),
            Function3 { dayLossMoney, maybeDayLimitMoney, restMoney ->
                val dayLimitMoney = maybeDayLimitMoney.blockingGet()
                val totalMoney = if (dayLimitMoney != null && (dayLimitMoney.amount - dayLossMoney.amount).signum() < 0) {
                    restMoney
                } else {
                    null
                }

                Balance(dayLimitMoney, dayLossMoney, totalMoney)
            }
        )
    }

    private fun getDayLimitMoney(day: Date): Single<Maybe<Money>> {
        validateDates(day)

        val today = datesManager.todayDate
        if (day < today) {
            return Single.just(Maybe.empty())
        }

        val (monthFirstDay, nextMonthFirstDay) = periodsManager.getMonthBoundaries(day)
        val startDay = maxOf(monthFirstDay, today)
        val remainingMonthDaysCount = ((nextMonthFirstDay.time - startDay.time) / millisInDay).toInt()

        return getOverspentDaysFromRange(startDay, day).flatMap { overspentDays ->
            Single.zip<Money, Money, Maybe<Money>>(
                getRestMoney(monthFirstDay, nextMonthFirstDay, today),
                getDaysTotalLossMoney(overspentDays),
                BiFunction { rest, overspentDaysLoss ->
                    val remainingNonOverspentDaysCount = (remainingMonthDaysCount - overspentDays.size).toBigDecimal()
                    val dayLimitAmount = (rest.amount - overspentDaysLoss.amount).divide(remainingNonOverspentDaysCount, RoundingMode.HALF_UP)
                    val dayLimitMoney = Money.by(dayLimitAmount)
                    Maybe.just(dayLimitMoney)
                }
            )
        }
    }

    private fun getOverspentDaysFromRange(fromDate: Date, toDate: Date): Single<Array<Date>> {
        validateDates(fromDate, toDate)
        if (fromDate == toDate) {
            return Single.just(emptyArray())
        }

        val daysIterator = DaysIterator(fromDate, toDate)
        val overspentChecks = ArrayList<OverspentCheck>(daysIterator.count)

        while (daysIterator.hasNext()) {
            val date = daysIterator.next()
            val overspentCheck = OverspentCheck(date, isOverspent(date))
            overspentChecks.add(overspentCheck)
        }

        return Single.zip(overspentChecks.map { it.isOverspent }) { array ->
            array
                .mapIndexed { index, isOverspent ->
                    if (isOverspent as Boolean) {
                        overspentChecks[index].date
                    } else {
                        null
                    }
                }
                .filterNotNull()
                .toTypedArray()
        }
    }

    private data class OverspentCheck(val date: Date, val isOverspent: Single<Boolean>)

    private fun isOverspent(day: Date): Single<Boolean> {
        validateDates(day)

        val today = datesManager.todayDate
        if (day < today) {
            throw IllegalArgumentException("Overspent cannot be checked for past days")
        }

        val (monthFirstDay, nextMonthFirstDay) = periodsManager.getMonthBoundaries(day)
        val remainingMonthDaysCount = ((nextMonthFirstDay.time - maxOf(monthFirstDay, today).time) / millisInDay).toBigDecimal()

        return Single.zip(
            getRestMoney(monthFirstDay, nextMonthFirstDay, today),
            getDayLossMoney(day),
            BiFunction { rest, dayLoss ->
                val restPerDay = rest.amount.divide(remainingMonthDaysCount, RoundingMode.HALF_UP)
                val overspent = restPerDay - dayLoss.amount
                overspent.signum() < 0
            }
        )
    }

    private fun getRestMoney(monthFirstDay: Date, nextMonthFirstDay: Date, today: Date): Single<Money> {
        validateDates(monthFirstDay, nextMonthFirstDay, today)

        return Single.zip(
            repository.query(MonthRestSpecification(today, monthFirstDay, nextMonthFirstDay)),
            repository.query(MonthTotalGainSpecification(monthFirstDay)),
            BiFunction { rest, totalGain ->
                val restAmount = Money.by(rest.toLong()).amount
                val totalGainAmount = Money.by(totalGain.toLong()).amount
                val savingsRatio = savingsManager.getSavingsForMonth(monthFirstDay).toBigDecimal()
                val desiredSavings = totalGainAmount.multiply(savingsRatio)
                val realRestAmount = (restAmount - desiredSavings).abs()
                Money.by(realRestAmount)
            }
        )
    }

    private fun getDaysTotalLossMoney(days: Array<Date>): Single<Money> {
        if (days.isEmpty()) {
            return Single.just(Money.by(BigDecimal.ZERO))
        }
        validateDates(*days)

        return Single.zip(days.map { getDayLossMoney(it) }) { array ->
            val totalLossAmount = array
                    .asSequence()
                    .map { (it as Money).amount }
                    .fold(BigDecimal.ZERO) { accumulator, lossAmount ->
                        accumulator + lossAmount
                    }

                Money.by(totalLossAmount)
        }
    }

    private fun getDayLossMoney(day: Date): Single<Money> {
        validateDates(day)

        return repository
            .query(DayLossSpecification(day))
            .map { value -> Money.by(-value.toLong()) }
    }

    private fun validateDates(vararg dates: Date) {
        if (!CashApp.isDebugBuild) {
            return
        }

        dates.forEach { date ->
            require(date.time % millisInDay == 0L) {
                "Only dates in GMT+00:00 with hours, minutes, seconds, milliseconds set to zero are accepted here"
            }
        }
    }

}