/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.entities

import com.madewithlove.daybalance.CashApp
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*
import kotlin.math.abs

open class Transaction : RealmObject(), Serializable {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    var amount: Double = 0.0

    var categoryId: String = ""

    var period: String = ""

    var startTimestamp: Long = 0

    var endTimestamp: Long = 0

    var addedTimestamp: Long = 0

    var account: Account? = null


    fun isGain(): Boolean = amount > 0.0

    fun getDaysCount(): Int = ((endTimestamp - startTimestamp) / CashApp.millisInDay).toInt()

    fun getAmountPerDay(): Double = abs(amount) / getDaysCount().toDouble()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}