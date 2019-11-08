/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.ui.KeypadView
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.TextFormatter
import com.madewithlove.daybalance.utils.cache
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal.ZERO
import java.util.*
import kotlin.collections.ArrayList

class CreateViewModel(
    application: Application,
    private val datesManager: DatesManager,
    private val initialType: Type
) : AndroidViewModel(application) {

    companion object {

        private const val AMOUNT_MAX_LENGTH = 16

    }


    val createStateObservable: Observable<CreateState>
    val createState: CreateState get() = createStateSubject.value!!
    val keypadActionsConsumer = Consumer<KeypadView.Action>(this::handleKeypadAction)
    val commentTextConsumer = Consumer<CharSequence>(this::handleCommentText)

    private val calendar = GregorianCalendar()
    private val createStateSubject = BehaviorSubject.createDefault(getDefaultCreateState())
    private val dc = DisposableCache()


    init {
        createStateObservable = createStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }

        datesManager.currentDateObservable.subscribe { currentDate ->
            val newState = createState.copy(
                lossDate = currentDate,
                gainAvailableMonths = getAvailableMonths(currentDate),
                gainChosenMonth = 1
            )
            createStateSubject.onNext(newState)
        }.cache(dc)
    }


    fun switchType() {
        val newType = if (createState.type == Type.LOSS) Type.GAIN else Type.LOSS
        val newState = createState.copy(type = newType)
        createStateSubject.onNext(newState)
    }

    fun setGainChosenMonth(chosenMonth: Int) {
        val newState = createState.copy(gainChosenMonth = chosenMonth)
        createStateSubject.onNext(newState)
    }


    override fun onCleared() {
        dc.drain()
    }


    private fun handleKeypadAction(action: KeypadView.Action) {
        when (action.type) {
            KeypadView.Type.NUMBER -> {
                if (createState.amountString.count { it.isDigit() } == AMOUNT_MAX_LENGTH) {
                    return
                }

                val newAmountString = createState.amountString + action.payload.toString()
                createStateSubject.onNext(createState.copy(amountString = newAmountString.format()))
            }

            KeypadView.Type.DOT -> {
                if (createState.amountString.contains('.')) {
                    return
                }

                val newAmountString = createState.amountString + "."
                createStateSubject.onNext(createState.copy(amountString = newAmountString))
            }

            KeypadView.Type.BACKSPACE -> {
                val newAmountString = createState.amountString.dropLast(1)
                createStateSubject.onNext(createState.copy(amountString = newAmountString.format()))
            }

            KeypadView.Type.ENTER -> {
                val money = createState.amountString.toMoney()
                val inputValidation = if (money == null) InputValidation.ERROR else InputValidation.OK
                createStateSubject.onNext(createState.copy(inputValidation = inputValidation))
                createStateSubject.onNext(createState.copy(inputValidation = InputValidation.NONE))
            }
        }
    }

    private fun String.format(): String {
        if (this.isEmpty() || this.contains('.')) {
            return this
        }

        val money = Money.by(this.filterNot { it == ' ' })
        return TextFormatter.formatMoney(money, withFixedFraction = false)
    }

    private fun String.toMoney(): Money? {
        var string = this

        if (string.isEmpty()) {
            return null
        }

        if (string.last() == '.') {
            string += '0'
        }

        string = string.filterNot { it == ' ' }

        val money = Money.by(string)
        return if (money.amount > ZERO) {
            money
        } else {
            null
        }
    }

    private fun handleCommentText(comment: CharSequence) {
        val newState = createState.copy(comment = comment.toString().trim())
        createStateSubject.onNext(newState)
    }

    private fun getDefaultCreateState(): CreateState = CreateState(
        type = initialType,
        lossDate = datesManager.currentDate,
        gainAvailableMonths = getAvailableMonths(datesManager.currentDate),
        gainChosenMonth = 1,
        amountString = "",
        comment = "",
        inputValidation = InputValidation.NONE
    )

    private fun getAvailableMonths(date: Date): List<Date> {
        val result = ArrayList<Date>()
        calendar.time = date

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        result.add(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        result.add(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        result.add(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        result.add(calendar.time)

        return result
    }


    data class CreateState(
        val type: Type,
        val lossDate: Date,
        val gainAvailableMonths: List<Date>,
        val gainChosenMonth: Int,
        val amountString: String,
        val comment: String,
        val inputValidation: InputValidation
    )


    enum class Type { GAIN, LOSS }


    enum class InputValidation { OK, ERROR, NONE }

}