/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Balance
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.features.plan.PlanViewModel
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.model.Cache
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow

@SuppressLint("DefaultLocale")
class MainViewModel(
    application: Application,
    datesManager: DatesManager,
    cache: Cache
) : AndroidViewModel(application) {

    val mainStateObservable: Observable<MainState>
    val mainState: MainState get() = mainStateSubject.value!!
    val showCalendarObservable: Observable<Unit>

    private val mainStateSubject = BehaviorSubject.createDefault(getDefaultMainState())
    private val showCalendarSubject = PublishSubject.create<Unit>()
    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())
    private val dc = DisposableCache()


    init {
        mainStateObservable = mainStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        showCalendarObservable = showCalendarSubject.throttleFirst(1, TimeUnit.SECONDS)

        cache.balanceObservable
            .map(this::getCircleState)
            .map {
                mainState.copy(circleState = it)
            }
            .subscribe(mainStateSubject.onNextConsumer())
            .cache(dc)

        datesManager.extendedDateObservable
            .map { extendedDate ->
                mainState.copy(
                    currentDate = extendedDate.date,
                    isToday = extendedDate.isToday,
                    weekday = weekdayFormat.format(extendedDate.date).capitalize()
                )
            }
            .subscribe(mainStateSubject.onNextConsumer())
            .cache(dc)
    }


    fun notifyCreateOpened() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.KEYBOARD)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyCreateClosed() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.HISTORY, isKeyboardOpened = false)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyPlanOpened(section: PlanViewModel.Section) {
        val largeButtonType = when (section) {
            PlanViewModel.Section.GAIN -> LargeButtonType.PLAN_GAIN
            PlanViewModel.Section.LOSS -> LargeButtonType.PLAN_LOSS
            PlanViewModel.Section.MONEYBOX -> LargeButtonType.PLAN_MONEYBOX
        }
        val newMainState = mainState.copy(largeButtonType = largeButtonType)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyPlanClosed() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.HISTORY)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyMoneyboxOpened() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.MONEYBOX)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyMoneyboxClosed() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.HISTORY)
        mainStateSubject.onNext(newMainState)
    }

    fun notifySettingsOpened() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.SETTINGS)
        mainStateSubject.onNext(newMainState)
    }

    fun notifySettingsClosed() {
        val newMainState = mainState.copy(largeButtonType = LargeButtonType.HISTORY)
        mainStateSubject.onNext(newMainState)
    }

    fun openKeyboard() {
        val newMainState = mainState.copy(isKeyboardOpened = true)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyKeyboardClosed() {
        val newMainState = mainState.copy(isKeyboardOpened = false)
        mainStateSubject.onNext(newMainState)
    }

    fun showCalendar() {
        showCalendarSubject.onNext(Unit)
    }


    override fun onCleared() {
        dc.drain()
    }


    private fun getCircleState(balance: Balance): CircleView.CircleState {
        val amount: Money
        val progress: Float
        val isPast: Boolean = balance.dayLimit == null

        balance.apply {
            if (isPast) {
                progress = 0f
                amount = dayLoss
            } else {
                when {
                    // Normal
                    total == null
                            && dayLimit != null
                            && dayLimit.amount.signum() > 0 -> {
                        amount = Money.by(dayLimit.amount - dayLoss.amount)
                        progress = 1f - (dayLoss.amount.toFloat() / dayLimit.amount.toFloat())
                    }

                    // Zero limit & zero loss
                    total == null
                            && dayLimit != null
                            && dayLimit.amount.signum() == 0 -> {
                        amount = dayLoss
                        progress = 0f
                    }

                    // Overrun
                    total != null
                            && total.amount > dayLoss.amount
                            && dayLimit != null -> {
                        amount = Money.by(dayLimit.amount - dayLoss.amount)
                        progress = (dayLoss.amount.toDouble() / total.amount.toDouble() - 1.0).pow(4.0).toFloat() - 1f
                    }

                    // No money
                    total != null
                            && dayLimit != null -> {
                        amount = Money.by(dayLimit.amount - dayLoss.amount)
                        progress = -1f
                    }

                    else -> throw IllegalStateException("Unexpected balance: $this")
                }
            }
        }

        return CircleView.CircleState(amount, progress, isPast)
    }

    private fun getDefaultMainState(): MainState = MainState(
        currentDate = Date(),
        isToday = false,
        weekday = "",
        circleState = CircleView.CircleState(),
        largeButtonType = LargeButtonType.HISTORY,
        isKeyboardOpened = false
    )


    data class MainState(
        val currentDate: Date,
        val isToday: Boolean,
        val weekday: String,
        val circleState: CircleView.CircleState,
        val largeButtonType: LargeButtonType,
        val isKeyboardOpened: Boolean
    )


    enum class LargeButtonType { HISTORY, KEYBOARD, PLAN_GAIN, PLAN_LOSS, PLAN_MONEYBOX, MONEYBOX, SETTINGS }

}