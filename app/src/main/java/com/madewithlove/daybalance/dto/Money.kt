/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.dto

import java.math.BigDecimal
import java.math.BigDecimal.*

class Money private constructor(val amount: BigDecimal) {

    companion object {

        private val hundred = BigDecimal(100)


        fun by(string: String): Money {
            val amount = try {
                BigDecimal(string).setScale(2, ROUND_HALF_UP)
            } catch (e: NumberFormatException) {
                ZERO
            }

            return Money(amount)
        }

        fun by(long: Long): Money {
            val amount = BigDecimal(long).divide(hundred).setScale(2, ROUND_HALF_UP)
            return Money(amount)
        }

        fun by(bigDecimal: BigDecimal): Money {
            val amount = bigDecimal.setScale(2, ROUND_HALF_UP)
            return Money(amount)
        }

    }


    fun isGain(): Boolean = amount.signum() > 0

    fun toUnscaledLong(): Long = amount.multiply(hundred).toLong()

}