/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.dto

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP

data class Money(val amount: BigDecimal) {

    companion object {

        fun by(string: String): Money {
            val amount = try {
                BigDecimal(string)
            } catch (e: NumberFormatException) {
                BigDecimal.ZERO
            }

            return Money(amount)
        }

    }


    init {
        amount.setScale(2, ROUND_HALF_UP)
    }

}