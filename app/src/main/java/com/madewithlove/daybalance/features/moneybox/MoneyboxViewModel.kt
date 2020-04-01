/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.moneybox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.helpers.SavingsManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.MonthDiffSpecification
import com.madewithlove.daybalance.repository.specifications.MonthTotalGainSpecification
import com.madewithlove.daybalance.repository.specifications.TotalDiffBeforeDateSpecification
import com.madewithlove.daybalance.utils.CalendarFactory
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class MoneyboxViewModel(
    application: Application,
    private val datesManager: DatesManager,
    private val savingsManager: SavingsManager,
    private val repository: TransactionsRepository
) : AndroidViewModel(application) {

    val moneyboxStateObservable: Observable<MoneyboxState>
    val moneyboxState: MoneyboxState get() = moneyboxStateSubject.value!!

    private val ctx = application.applicationContext
    private val calendar = CalendarFactory.getInstance()
    private val moneyboxStateSubject = BehaviorSubject.createDefault(getDefaultMoneyboxState())
    private val dc = DisposableCache()


    init {
        moneyboxStateObservable = moneyboxStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        datesManager.isTodayObservable
            .skip(1)
            .subscribe {
                val newState = moneyboxState.copy(
                    monthFirstDay = datesManager.getThisMonthFirstDay(),
                    savingsRatio = savingsManager.getSavingsForMonth(datesManager.getThisMonthFirstDay())
                )
                moneyboxStateSubject.onNext(newState)
            }
            .cache(dc)
    }


    fun requestData() {
        val loadingState = moneyboxState.copy(totalMoney = null, monthMoney = null)
        moneyboxStateSubject.onNext(loadingState)

        val thisMonthFirstDay = datesManager.getThisMonthFirstDay()

        calendar.time = thisMonthFirstDay
        calendar.add(Calendar.MONTH, 1)
        val nextMonthFirstDay = calendar.time

        Single
            .zip<Money, Money, Money, Unit>(
                repository
                    .query(TotalDiffBeforeDateSpecification(thisMonthFirstDay))
                    .map { Money.by(it.toLong()) },

                repository
                    .query(MonthTotalGainSpecification(thisMonthFirstDay))
                    .map { Money.by(it.toLong()) },

                repository
                    .query(MonthDiffSpecification(thisMonthFirstDay, nextMonthFirstDay))
                    .map { Money.by(it.toLong()) },

                Function3 { totalMoney, monthGainMoney, monthDiffMoney ->
                    val savingsRatio = BigDecimal(savingsManager.getSavingsForMonth(thisMonthFirstDay).toDouble())

                    val monthMoneyAmount = minOf(
                        monthGainMoney.amount.multiply(savingsRatio),
                        monthDiffMoney.amount
                    )
                    val previousMoneyAmount = totalMoney.amount - monthMoneyAmount

                    val monthMoney = Money.by(monthMoneyAmount)
                    val previousMoney = Money.by(previousMoneyAmount)

                    val newState = moneyboxState.copy(
                        totalMoney = totalMoney,
                        previousMoney = previousMoney,
                        monthMoney = monthMoney
                    )
                    moneyboxStateSubject.onNext(newState)
                }
            )
            .subscribe()
            .cache(dc)
    }

    fun getDaysCountSinceInstall(): Int {
        val installTime = ctx.packageManager.getPackageInfo(ctx.packageName, 0).firstInstallTime
        val timeSinceInstall = System.currentTimeMillis() - installTime
        return (timeSinceInstall / TimeUnit.DAYS.toMillis(1) + 1).toInt()
    }

    override fun onCleared() {
        dc.drain()
    }


    private fun getDefaultMoneyboxState(): MoneyboxState = MoneyboxState(
        monthFirstDay = datesManager.getThisMonthFirstDay(),
        savingsRatio = savingsManager.getSavingsForMonth(datesManager.getThisMonthFirstDay()),
        totalMoney = null,
        previousMoney = null,
        monthMoney = null
    )


    data class MoneyboxState(
        val monthFirstDay: Date,
        val savingsRatio: Float,
        val totalMoney: Money?,
        val previousMoney: Money?,
        val monthMoney: Money?
    ) {

        val isLoading: Boolean get() = totalMoney == null || monthMoney == null

    }

}