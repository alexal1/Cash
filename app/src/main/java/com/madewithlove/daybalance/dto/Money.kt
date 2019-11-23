/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.dto

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.math.BigDecimal.ZERO

data class Money(val amount: BigDecimal) {

    companion object {

        fun by(string: String): Money {
            val amount = try {
                BigDecimal(string)
            } catch (e: NumberFormatException) {
                ZERO
            }

            return Money(amount)
        }

        fun by(long: Long): Money {
            val amount = BigDecimal(long)
            return Money(amount)
        }

    }


    init {
        amount.setScale(2, ROUND_HALF_UP)
    }


    fun isGain(): Boolean = amount.signum() > 0

    fun toUnscaledLong(): Long = amount.unscaledValue().toLong()

}