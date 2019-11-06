/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("DefaultLocale")
class MainViewModel(
    application: Application,
    datesManager: DatesManager
) : AndroidViewModel(application) {

    val mainStateObservable: Observable<MainState>
    val mainState: MainState get() = mainStateSubject.value!!

    private val mainStateSubject = BehaviorSubject.create<MainState>()
    private val weekdayFormat = SimpleDateFormat("EEEE", application.currentLocale())
    private val dc = DisposableCache()


    init {
        mainStateObservable = mainStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }

        Observable
            .combineLatest<Date, Boolean, MainState>(
                datesManager.currentDateObservable,
                datesManager.isTodayObservable,
                BiFunction { currentDate, isToday ->
                    MainState(
                        currentDate = currentDate,
                        isToday = isToday,
                        weekday = weekdayFormat.format(currentDate).capitalize(),
                        circleState = CircleView.CircleState(Money(BigDecimal(1234.56)), 0.8f),
                        largeButtonType = LargeButtonType.HISTORY,
                        isKeyboardOpened = false
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

    fun openKeyboard() {
        val newMainState = mainState.copy(isKeyboardOpened = true)
        mainStateSubject.onNext(newMainState)
    }

    fun notifyKeyboardClosed() {
        val newMainState = mainState.copy(isKeyboardOpened = false)
        mainStateSubject.onNext(newMainState)
    }


    override fun onCleared() {
        dc.drain()
    }


    data class MainState(
        val currentDate: Date,
        val isToday: Boolean,
        val weekday: String,
        val circleState: CircleView.CircleState,
        val largeButtonType: LargeButtonType,
        val isKeyboardOpened: Boolean
    )


    enum class LargeButtonType { HISTORY, KEYBOARD }

}