/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.ui.KeypadView
import com.madewithlove.daybalance.utils.TextFormatter
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.math.BigDecimal

class CreateViewModel(application: Application) : AndroidViewModel(application) {

    companion object {

        private const val AMOUNT_MAX_LENGTH = 16

    }


    val createStateObservable: Observable<CreateState>
    val createState: CreateState get() = createStateSubject.value!!
    val keypadActionsConsumer = Consumer<KeypadView.Action>(this::handleKeypadAction)
    val commentTextConsumer = Consumer<CharSequence>(this::handleCommentText)

    private val createStateSubject = BehaviorSubject.create<CreateState>()


    init {
        createStateObservable = createStateSubject
            .distinctUntilChanged()
            .doOnNext { Timber.i(it.toString()) }

        // Default state
        createStateSubject.onNext(
            CreateState(
                amountString = "",
                comment = ""
            )
        )
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

            }
        }
    }

    private fun String.format(): String {
        if (this.isEmpty() || this.contains('.')) {
            return this
        }

        val money = Money(BigDecimal(this.filterNot { it == ' ' }))
        return TextFormatter.formatMoney(money, withFixedFraction = false)
    }

    private fun handleCommentText(comment: CharSequence) {
        val newState = createState.copy(comment = comment.toString().trim())
        createStateSubject.onNext(newState)
    }


    data class CreateState(
        val amountString: String,
        val comment: String
    )


    enum class Type { GAIN, LOSS }

}