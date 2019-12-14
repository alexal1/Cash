/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.features.plan.PlanViewModel
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
class MainViewModel(
    application: Application,
    datesManager: DatesManager
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

        showCalendarObservable = showCalendarSubject.throttleFirst(1, TimeUnit.SECONDS)

        Observable
            .combineLatest<Date, Boolean, MainState>(
                datesManager.currentDateObservable,
                datesManager.isTodayObservable,
                BiFunction { currentDate, isToday ->
                    mainState.copy(
                        currentDate = currentDate,
                        isToday = isToday,
                        weekday = weekdayFormat.format(currentDate).capitalize()
                    )
                }
            )
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
            PlanViewModel.Section.MONEYBOX -> LargeButtonType.PLAN_MODEYBOX
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


    private fun getDefaultMainState(): MainState = MainState(
        currentDate = Date(),
        isToday = false,
        weekday = "",
        circleState = CircleView.CircleState(Money.by(BigDecimal(1234.56)), 0.8f),
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


    enum class LargeButtonType { HISTORY, KEYBOARD, PLAN_GAIN, PLAN_LOSS, PLAN_MODEYBOX, MONEYBOX }

}