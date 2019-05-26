package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.CashApp.Companion.PREFS_DEFAULT_PICKER_CURRENCY_INDEX
import com.alex_aladdin.cash.helpers.CategoriesManager
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.helpers.enums.Periods
import com.alex_aladdin.cash.helpers.enums.getDateIncrement
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.entities.Account
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.TextFormatter
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.currentLocale
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel.CalculatorActionType.*
import com.alex_aladdin.cash.viewmodels.enums.Categories
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class NewTransactionViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val repository: TransactionsRepository by inject()
    private val currencyManager: CurrencyManager by inject()
    private val categoriesManager: CategoriesManager by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private val defaultCurrencyIndex = getDefaultCurrencyIndex()
    private val amountPattern = Regex("\\d+(\\.\\d+)?")
    private val dc = DisposableCache()

    private val amountSubject = BehaviorSubject.createDefault("0")
    val amountObservable: Observable<String> = amountSubject

    private val periodSubject = PublishSubject.create<Periods>()
    val periodObservable: Observable<Periods> = periodSubject

    private val isDoneSubject = PublishSubject.create<Boolean>()
    val isDoneObservable: Observable<Boolean> = isDoneSubject

    val currentDateObservable: Observable<Date> = app.currentDate
    val calculatorActionConsumer = Consumer(this::handleCalculatorAction)
    val categoryPickConsumer = Consumer(this::handleCategoryPick)
    val currenciesList = currencyManager.getCurrenciesList()

    var currencyIndex = defaultCurrencyIndex
        set(value) {
            field = value
            setDefaultCurrencyIndex(value)
        }

    lateinit var type: Type; private set
    lateinit var currentCategory: Categories; private set

    private var firstOperand: String? = null
    private var operator: CalculatorActionType? = null


    fun setTransactionType(type: Type) {
        this.type = type
    }

    fun getDefaultCategory(): Categories = if (type == Type.LOSS) {
        categoriesManager.getDefaultLossCategory()
    } else {
        categoriesManager.getDefaultGainCategory()
    }

    fun getAvailablePeriods(): List<Periods> = if (type == Type.LOSS) {
        Periods.values()
            .filterNot { it == Periods.SINGLE }
            .reversed()
    } else {
        Periods.values()
            .filterNot { listOf(Periods.TWENTY_YEARS, Periods.TEN_YEARS, Periods.THREE_YEARS).contains(it) }
            .reversed()
    }

    fun getCurrentPeriodIndex(): Int = getAvailablePeriods().indexOf(categoriesManager.getPeriod(currentCategory))

    fun setPeriodIndex(index: Int) {
        val period = getAvailablePeriods()[index]
        categoriesManager.setPeriod(currentCategory, period)
        periodSubject.onNext(period)
    }

    fun done() {
        val isInputValid = amountSubject.value!!.isValidAmount()

        if (isInputValid) {
            val transaction = getCurrentTransaction()
            repository.addTransaction(transaction).subscribe {
                isDoneSubject.onNext(true)
            }.cache(dc)
        } else {
            isDoneSubject.onNext(false)
        }
    }

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

    private fun handleCategoryPick(category: Categories) {
        currentCategory = category
        categoriesManager.setDefaultCategory(category)

        val period = categoriesManager.getPeriod(category)
        periodSubject.onNext(period)
    }

    private fun String.isValidAmount() = this.matches(amountPattern) && this.toDouble() > 0

    private fun getCurrentTransaction() = Transaction().apply {
        isGain = type == Type.GAIN
        amount = amountSubject.value!!.toDouble()
        categoryId = currentCategory.id
        startTimestamp = app.currentDate.value!!.time

        endTimestamp = categoriesManager
            .getPeriod(currentCategory)
            .getDateIncrement(app.currentLocale(), app.currentDate.value!!)
            .time

        addedTimestamp = System.currentTimeMillis()

        account = Account().apply {
            currencyIndex = this@NewTransactionViewModel.currencyIndex
        }
    }

    private fun getDefaultCurrencyIndex(): Int {
        val spIndex = sharedPreferences.getInt(PREFS_DEFAULT_PICKER_CURRENCY_INDEX, -1)
        if (spIndex >= 0) {
            return spIndex
        }

        val localeIndex = currencyManager.getCurrencyIndexByLocale()

        sharedPreferences.edit {
            putInt(PREFS_DEFAULT_PICKER_CURRENCY_INDEX, localeIndex)
        }

        return localeIndex
    }

    private fun setDefaultCurrencyIndex(index: Int) = sharedPreferences.edit {
        putInt(PREFS_DEFAULT_PICKER_CURRENCY_INDEX, index)
    }

    override fun onCleared() {
        dc.drain()
        super.onCleared()
    }


    enum class Type { GAIN, LOSS }

    enum class CalculatorActionType { NUMBER, DOT, BACKSPACE, PLUS, MINUS, MULTIPLY, DIVIDE, EQUALS }

    data class CalculatorAction(val type: CalculatorActionType, val payload: Int? = null)

}