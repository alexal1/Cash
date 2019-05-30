package com.alex_aladdin.cash.repository.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Transaction : RealmObject() {

    companion object {

        private const val millisInDay = 24 * 60 * 60 * 1000

    }


    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    var isGain: Boolean = false

    var amount: Double = 0.0

    var categoryId: String = ""

    var startTimestamp: Long = 0

    var endTimestamp: Long = 0

    var addedTimestamp: Long = 0

    var account: Account? = null


    fun getDaysCount(): Int = ((endTimestamp - startTimestamp) / millisInDay).toInt()

    fun getAmountPerDay(): Double = amount / getDaysCount().toDouble()

}