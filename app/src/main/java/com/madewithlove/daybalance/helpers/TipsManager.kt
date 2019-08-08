/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.repository.entities.Transaction
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TipsManager(context: Context, private val sharedPreferences: SharedPreferences) {

    private val app = context.applicationContext as CashApp
    private val currentTimestamp get() = app.currentDate.value!!.time

    private val resetSubject = PublishSubject.create<Unit>()
    val resetObservable: Observable<Unit> = resetSubject


    fun getTip(transactions: List<Transaction>): Maybe<Tip> {
        val tip: Tip = when {
            transactions.isEmpty() -> Tip.Empty()

            transactions.size == 1
                    && !transactions[0].isGain()
                    && transactions[0].getDaysCount() == 1
                    && transactions[0].startTimestamp == currentTimestamp
            -> Tip.SingleLossOnThisDayOneDayLong(transactions[0].getAmountPerDay())

            transactions.size == 1
                    && transactions[0].isGain()
                    && transactions[0].getDaysCount() == 1
                    && transactions[0].startTimestamp == currentTimestamp
            -> Tip.SingleGainOnThisDayOneDayLong(transactions[0].getAmountPerDay())

            transactions.size == 1
                    && !transactions[0].isGain()
                    && transactions[0].getDaysCount() > 1
                    && transactions[0].startTimestamp == currentTimestamp
            -> Tip.SingleLossOnThisDayManyDaysLong(transactions[0])

            transactions.size == 1
                    && transactions[0].isGain()
                    && transactions[0].getDaysCount() > 1
                    && transactions[0].startTimestamp == currentTimestamp
            -> Tip.SingleGainOnThisDayManyDaysLong(transactions[0])

            transactions.all { !it.isGain() }
                    && transactions.all { it.startTimestamp != currentTimestamp }
            -> Tip.LossesFromOtherDaysOnly(transactions.sumByDouble { it.getAmountPerDay() })

            transactions.all { it.isGain() }
                    && transactions.all { it.startTimestamp != currentTimestamp }
            -> Tip.GainsFromOtherDaysOnly(transactions.sumByDouble { it.getAmountPerDay() })

            transactions.any { !it.isGain() }
                    && transactions.any { it.isGain() }
                    && (transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() } - transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() } >= 0)
            -> Tip.LossesAndGainsPositiveResult(
                lossesSum = transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() },
                gainsSum = transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() },
                totalSum = transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() } - transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() }
            )

            transactions.any { !it.isGain() }
                    && transactions.any { it.isGain() }
                    && (transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() } - transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() } < 0)
            -> Tip.LossesAndGainsNegativeResult(
                lossesSum = transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() },
                gainsSum = transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() },
                totalSum = transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() } - transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() }
            )

            else -> null
        } ?: return Maybe.empty()

        return if (checkTipCanBeShown(tip)) {
            Maybe.just(tip)
        } else {
            Maybe.empty()
        }
    }

    fun closeTip(tip: Tip) {
        sharedPreferences.edit {
            putBoolean(CashApp.PREFS_TIPS_PREFIX + tip.name, true)
        }
    }

    fun reset() {
        sharedPreferences.edit {
            TipsNames.values().map { it.name.toLowerCase() }.forEach { name ->
                putBoolean(CashApp.PREFS_TIPS_PREFIX + name, false)
            }
        }

        resetSubject.onNext(Unit)
    }

    private fun checkTipCanBeShown(tip: Tip): Boolean {
        val wasClosed = sharedPreferences.getBoolean(CashApp.PREFS_TIPS_PREFIX + tip.name, false)
        return !wasClosed
    }


    sealed class Tip {

        abstract val name: String


        class Empty : Tip() {
            override val name: String = TipsNames.EMPTY.name.toLowerCase()
        }

        class SingleLossOnThisDayOneDayLong(val amount: Double) : Tip() {
            override val name: String = TipsNames.SINGLE_LOSS_ON_THIS_DAY_ONE_DAY_LONG.name.toLowerCase()
        }

        class SingleGainOnThisDayOneDayLong(val amount: Double) : Tip() {
            override val name: String = TipsNames.SINGLE_GAIN_ON_THIS_DAY_ONE_DAY_LONG.name.toLowerCase()
        }

        class SingleLossOnThisDayManyDaysLong(val transaction: Transaction) : Tip() {
            override val name: String = TipsNames.SINGLE_LOSS_ON_THIS_DAY_MANY_DAYS_LONG.name.toLowerCase()
        }

        class SingleGainOnThisDayManyDaysLong(val transaction: Transaction) : Tip() {
            override val name: String = TipsNames.SINGLE_GAIN_ON_THIS_DAY_MANY_DAYS_LONG.name.toLowerCase()
        }

        class LossesFromOtherDaysOnly(val sum: Double) : Tip() {
            override val name: String = TipsNames.LOSSES_FROM_OTHER_DAYS_ONLY.name.toLowerCase()
        }

        class GainsFromOtherDaysOnly(val sum: Double) : Tip() {
            override val name: String = TipsNames.GAINS_FROM_OTHER_DAYS_ONLY.name.toLowerCase()
        }

        class LossesAndGainsPositiveResult(val lossesSum: Double, val gainsSum: Double, val totalSum: Double) : Tip() {
            override val name: String = TipsNames.LOSSES_AND_GAINS_POSITIVE_RESULT.name.toLowerCase()
        }

        class LossesAndGainsNegativeResult(val lossesSum: Double, val gainsSum: Double, val totalSum: Double) : Tip() {
            override val name: String = TipsNames.LOSSES_AND_GAINS_NEGATIVE_RESULT.name.toLowerCase()
        }

    }

    enum class TipsNames {

        EMPTY,
        SINGLE_LOSS_ON_THIS_DAY_ONE_DAY_LONG,
        SINGLE_GAIN_ON_THIS_DAY_ONE_DAY_LONG,
        SINGLE_LOSS_ON_THIS_DAY_MANY_DAYS_LONG,
        SINGLE_GAIN_ON_THIS_DAY_MANY_DAYS_LONG,
        LOSSES_FROM_OTHER_DAYS_ONLY,
        GAINS_FROM_OTHER_DAYS_ONLY,
        LOSSES_AND_GAINS_POSITIVE_RESULT,
        LOSSES_AND_GAINS_NEGATIVE_RESULT

    }

}