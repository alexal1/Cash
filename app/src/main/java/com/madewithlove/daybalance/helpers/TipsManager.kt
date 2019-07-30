package com.madewithlove.daybalance.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.repository.entities.Transaction
import io.reactivex.Maybe

class TipsManager(context: Context, private val sharedPreferences: SharedPreferences) {

    private val app = context.applicationContext as CashApp
    private val currentTimestamp get() = app.currentDate.value!!.time


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
            -> Tip.SingleLossOnThisDayManyDaysLong(transactions[0].getAmountPerDay())

            transactions.size == 1
                    && transactions[0].isGain()
                    && transactions[0].getDaysCount() > 1
                    && transactions[0].startTimestamp == currentTimestamp
            -> Tip.SingleGainOnThisDayManyDaysLong(transactions[0].getAmountPerDay())

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

    private fun checkTipCanBeShown(tip: Tip): Boolean {
        val wasClosed = sharedPreferences.getBoolean(CashApp.PREFS_TIPS_PREFIX + tip.name, false)
        return !wasClosed
    }


    sealed class Tip {

        abstract val name: String


        class Empty : Tip() {
            override val name: String = "empty"
        }

        class SingleLossOnThisDayOneDayLong(val amount: Double) : Tip() {
            override val name: String = "single_loss_on_this_day_one_day_long"
        }

        class SingleGainOnThisDayOneDayLong(val amount: Double) : Tip() {
            override val name: String = "single_gain_on_this_day_one_day_long"
        }

        class SingleLossOnThisDayManyDaysLong(val amount: Double) : Tip() {
            override val name: String = "single_loss_on_this_day_many_days_long"
        }

        class SingleGainOnThisDayManyDaysLong(val amount: Double) : Tip() {
            override val name: String = "single_gain_on_this_day_many_days_long"
        }

    }

}