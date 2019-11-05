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
import java.math.BigDecimal

class CreateViewModel(application: Application) : AndroidViewModel(application) {

    val createStateObservable: Observable<CreateState>
    val createState: CreateState get() = createStateSubject.value!!
    val keypadActionsConsumer = Consumer<KeypadView.Action>(this::handleKeypadAction)

    private val createStateSubject = BehaviorSubject.create<CreateState>()


    init {
        createStateObservable = createStateSubject.distinctUntilChanged()

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
                val newAmountString = createState.amountString + action.payload.toString()
                createStateSubject.onNext(createState.copy(amountString = newAmountString.format()))
            }

            KeypadView.Type.DOT -> {
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


    data class CreateState(
        val amountString: String,
        val comment: String
    )


    enum class Type { GAIN, LOSS }

}