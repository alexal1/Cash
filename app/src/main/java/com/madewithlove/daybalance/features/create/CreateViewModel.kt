/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.model.BalanceLogic
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.ui.KeypadView
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.TextFormatter
import com.madewithlove.daybalance.utils.cache
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal.ZERO
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class CreateViewModel(
    application: Application,
    private val datesManager: DatesManager,
    private val balanceLogic: BalanceLogic,
    private val repository: TransactionsRepository,
    private val analytics: Analytics,
    private val initialType: Type,
    private val initialChosenMonth: Int?
) : AndroidViewModel(application) {

    companion object {

        private const val MAX_DIGITS_COUNT_BEFORE_DOT = 16
        private const val DIGITS_COUNT_AFTER_DOT = 2

    }


    val createStateObservable: Observable<CreateState>
    val createState: CreateState get() = createStateSubject.value!!
    val keypadActionsConsumer = Consumer<KeypadView.Action>(this::handleKeypadAction)
    val commentTextConsumer = Consumer<CharSequence>(this::handleCommentText)

    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val calendar = GregorianCalendar()
    private val createStateSubject = BehaviorSubject.createDefault(getDefaultCreateState())
    private val dc = DisposableCache()


    init {
        createStateObservable = createStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }
            .replay(1)
            .autoConnect()

        datesManager.currentDateObservable.subscribe { currentDate ->
            val newState = createState.copy(
                lossDate = currentDate,
                intoMoneyboxDate = currentDate,
                gainAvailableMonths = getAvailableMonths(currentDate),
                gainChosenMonth = if (initialType == Type.GAIN && initialChosenMonth != null) initialChosenMonth else 1,
                mandatoryLossAvailableMonths = getAvailableMonths(currentDate),
                mandatoryLossChosenMonth = if (initialType == Type.MANDATORY_LOSS && initialChosenMonth != null) initialChosenMonth else 1
            )
            createStateSubject.onNext(newState)
        }.cache(dc)
    }


    fun switchType() {
        val newType = when (initialType) {
            Type.MANDATORY_LOSS -> {
                if (createState.type == Type.MANDATORY_LOSS) Type.GAIN else Type.MANDATORY_LOSS
            }

            Type.LOSS, Type.GAIN -> {
                if (createState.type == Type.LOSS) Type.GAIN else Type.LOSS
            }

            else -> {
                createState.type
            }
        }

        val newState = createState.copy(type = newType)
        createStateSubject.onNext(newState)
    }

    fun setMandatoryLossChosenMonth(chosenMonth: Int) {
        val newState = createState.copy(mandatoryLossChosenMonth = chosenMonth)
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
                val hasDot = createState.amountString.contains('.')
                val maxDigitsCount = if (hasDot) {
                    MAX_DIGITS_COUNT_BEFORE_DOT + DIGITS_COUNT_AFTER_DOT
                } else {
                    MAX_DIGITS_COUNT_BEFORE_DOT
                }

                if (createState.amountString.count { it.isDigit() } == maxDigitsCount) {
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
                if (createState.inputValidation == InputValidation.OK) {
                    return
                }

                val money = createState.amountString.toMoney()
                if (money == null) {
                    createStateSubject.onNext(createState.copy(inputValidation = InputValidation.ERROR))
                    createStateSubject.onNext(createState.copy(inputValidation = InputValidation.NONE))
                } else {
                    val transaction = createTransaction(money)
                    saveTransaction(transaction).subscribe {
                        val newState = createState.copy(inputValidation = InputValidation.OK)
                        createStateSubject.onNext(newState)

                        analytics.createTransaction(isWithComment = transaction.comment.isNotEmpty())
                    }.cache(dc)
                }
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
        intoMoneyboxDate = datesManager.currentDate,
        gainAvailableMonths = getAvailableMonths(datesManager.currentDate),
        gainChosenMonth = 1,
        mandatoryLossAvailableMonths = getAvailableMonths(datesManager.currentDate),
        mandatoryLossChosenMonth = 1,
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

    private fun createTransaction(money: Money): Transaction = Transaction().apply {
        addedTimestamp = System.currentTimeMillis()
        comment = createState.comment

        when (createState.type) {
            Type.LOSS -> {
                value = -money.toUnscaledLong()
                actionTimestamp = datesManager.currentDate.time
                displayTimestamp = actionTimestamp
                setType(Transaction.Type.INSTANT)
            }

            Type.MANDATORY_LOSS -> {
                value = -money.toUnscaledLong()
                actionTimestamp = createState.mandatoryLossAvailableMonths[createState.mandatoryLossChosenMonth].time
                displayTimestamp = addedTimestamp / millisInDay * millisInDay
                setType(Transaction.Type.MONTH)
            }

            Type.GAIN -> {
                value = money.toUnscaledLong()
                actionTimestamp = createState.gainAvailableMonths[createState.gainChosenMonth].time
                displayTimestamp = addedTimestamp / millisInDay * millisInDay
                setType(Transaction.Type.MONTH)
            }

            Type.INTO_MONEYBOX -> {
                value = money.toUnscaledLong()
                actionTimestamp = datesManager.currentDate.time
                displayTimestamp = actionTimestamp
                setType(Transaction.Type.INTO_MONEYBOX)
            }
        }
    }

    private fun saveTransaction(transaction: Transaction): Completable {
        return repository
            .addTransaction(transaction)
            .andThen(balanceLogic.invalidate())
    }


    data class CreateState(
        val type: Type,
        val lossDate: Date,
        val intoMoneyboxDate: Date,
        val gainAvailableMonths: List<Date>,
        val gainChosenMonth: Int,
        val mandatoryLossAvailableMonths: List<Date>,
        val mandatoryLossChosenMonth: Int,
        val amountString: String,
        val comment: String,
        val inputValidation: InputValidation
    )


    enum class Type { GAIN, LOSS, MANDATORY_LOSS, INTO_MONEYBOX }


    enum class InputValidation { OK, ERROR, NONE }

}