package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.utils.TextFormatter
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel.CalculatorActionType.*
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class NewTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CashApp

    private val amountSubject = BehaviorSubject.createDefault("0")
    val amountObservable: Observable<String> = amountSubject

    val currentDateObservable: Observable<Date> = app.currentDate
    val calculatorActionConsumer = Consumer(this::handleCalculatorAction)

    private var firstOperand: String? = null
    private var operator: CalculatorActionType? = null


    private fun handleCalculatorAction(action: CalculatorAction) = amountSubject.value!!
        // Empty field if operator was chosen
        .let { amount ->
            if ((action.type == NUMBER || action.type == DOT) && operator != null && firstOperand == null) {
                firstOperand = amount
                ""
            } else {
                amount
            }
        }
        // Key press handling
        .let { amount ->
            when (action.type) {
                NUMBER -> {
                    "$amount${action.payload}"
                }

                DOT -> {
                    val hasDot = amount.contains(".")
                    if (hasDot) {
                        amount
                    } else {
                        "$amount."
                    }
                }

                BACKSPACE -> {
                    amount.dropLast(1)
                }

                PLUS, MINUS, MULTIPLY, DIVIDE -> {
                    if (firstOperand != null && operator != null) {
                        val prevFirstOperand = firstOperand!!
                        val prevOperator = operator!!
                        firstOperand = null
                        operator = action.type
                        performOperation(prevFirstOperand, amount, prevOperator)
                    } else {
                        operator = action.type
                        amount
                    }
                }

                EQUALS -> {
                    if (firstOperand != null && operator != null) {
                        val prevFirstOperand = firstOperand!!
                        val prevOperator = operator!!
                        firstOperand = null
                        operator = null
                        performOperation(prevFirstOperand, amount, prevOperator)
                    } else {
                        firstOperand = null
                        operator = null
                        amount
                    }
                }
            }
        }
        // Null when empty
        .let { newAmount ->
            if (newAmount.isEmpty()) {
                "0"
            } else {
                newAmount
            }
        }
        // Null when single minus
        .let { newAmount ->
            if (newAmount == "-") {
                "0"
            } else {
                newAmount
            }
        }
        // Remove nulls at the start
        .let { newAmount ->
            var amount = newAmount
            while (amount.length > 1 && amount[0] == '0') {
                amount = amount.drop(1)
            }
            amount
        }
        // Add null before dot
        .let { newAmount ->
            if (newAmount[0] == '.') {
                "0$newAmount"
            } else {
                newAmount
            }
        }
        // Set maximum length
        .let { newAmount ->
            newAmount.take(16)
        }
        // Send new amount
        .let { newAmount ->
            amountSubject.onNext(newAmount)
        }

    private fun performOperation(operand1: String, operand2: String, operator: CalculatorActionType) = try {
        when (operator) {
            PLUS -> operand1.toDouble() + operand2.toDouble()
            MINUS -> operand1.toDouble() - operand2.toDouble()
            MULTIPLY -> operand1.toDouble() * operand2.toDouble()
            DIVIDE -> operand1.toDouble() / operand2.toDouble()
            else -> throw IllegalArgumentException()
        }.let { amount ->
            TextFormatter.formatDouble(amount)
        }
    } catch (e: NumberFormatException) {
        "0"
    }


    enum class Type { GAIN, LOSS }

    enum class CalculatorActionType { NUMBER, DOT, BACKSPACE, PLUS, MINUS, MULTIPLY, DIVIDE, EQUALS }

    data class CalculatorAction(val type: CalculatorActionType, val payload: Int? = null)

}