/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.plan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.helpers.SavingsManager
import com.madewithlove.daybalance.model.BalanceLogic
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.repository.specifications.MonthMandatoryLossSpecification
import com.madewithlove.daybalance.repository.specifications.MonthTotalGainSpecification
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*

class PlanViewModel(
    application: Application,
    private val datesManager: DatesManager,
    private val repository: TransactionsRepository,
    private val savingsManager: SavingsManager,
    private val balanceLogic: BalanceLogic
) : AndroidViewModel(application) {

    val planStateObservable: Observable<PlanState>
    val planState: PlanState get() = planStateSubject.value!!
    val amountClickSubject = PublishSubject.create<Unit>()

    private val planStateSubject = BehaviorSubject.createDefault(getDefaultPlanState())
    private val dc = DisposableCache()


    init {
        planStateObservable = planStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        datesManager.currentDateObservable
            .skip(1)
            .map {
                planState.copy(currentDate = it)
            }
            .subscribe(planStateSubject.onNextConsumer())
            .cache(dc)

        repository.realmChangedObservable.subscribe {
            requestData()
        }.cache(dc)
    }


    fun setSection(section: Section) {
        val newHistoryFilter = when (section) {
            Section.GAIN -> HistorySpecification.MonthTotalGainFilter(datesManager.getCurrentMonthFirstDay())
            Section.LOSS -> HistorySpecification.MonthMandatoryLossFilter(datesManager.getThisMonthFirstDay())
            else -> HistorySpecification.Empty
        }
        val newState = planState.copy(currentSection = section, historyFilter = newHistoryFilter)
        planStateSubject.onNext(newState)
    }

    fun requestData() {
        val loadingState = planState.copy(gain = null, loss = null, savingsRatio = null)
        planStateSubject.onNext(loadingState)

        val currentMonthFirstDay = datesManager.getCurrentMonthFirstDay()

        Single
            .zip(
                repository.query(MonthTotalGainSpecification(currentMonthFirstDay)),
                repository.query(MonthMandatoryLossSpecification(currentMonthFirstDay)),
                Single.just(savingsManager.getSavingsForMonth(currentMonthFirstDay)),
                Function3<Number, Number, Float, Unit> { gainValue, lossValue, savingsRatioValue ->
                    val gain = Money.by(gainValue.toLong())
                    val loss = Money.by(lossValue.toLong())
                    val newState = planState.copy(gain = gain, loss = loss, savingsRatio = savingsRatioValue)
                    planStateSubject.onNext(newState)
                }
            )
            .subscribe()
            .cache(dc)
    }

    fun setSavingsRatio(ratio: Float) {
        val currentMonthFirstDay = datesManager.getCurrentMonthFirstDay()
        savingsManager.setSavingsForMonth(currentMonthFirstDay, ratio)

        val newState = planState.copy(savingsRatio = ratio)
        planStateSubject.onNext(newState)

        balanceLogic.invalidate().subscribe().cache(dc)
    }


    override fun onCleared() {
        dc.drain()
    }


    private fun getDefaultPlanState(): PlanState = PlanState(
        currentDate = datesManager.currentDate,
        currentSection = Section.GAIN,
        gain = null,
        loss = null,
        savingsRatio = null,
        historyFilter = HistorySpecification.MonthTotalGainFilter(datesManager.getCurrentMonthFirstDay())
    )


    data class PlanState(
        val currentDate: Date,
        val currentSection: Section,
        val gain: Money?,
        val loss: Money?,
        val savingsRatio: Float?,
        val historyFilter: HistorySpecification.Filter
    ) {

        val isLoading: Boolean get() = gain == null || loss == null || savingsRatio == null

    }


    enum class Section { GAIN, LOSS, MONEYBOX }

}