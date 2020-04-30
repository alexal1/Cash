/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

data class MonthRestSpecification(
    private val todayDate: Date,
    val currentMonthFirstDay: Date,
    val nextMonthFirstDay: Date
) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        // Month gain
        .equalTo("actionTimestamp", currentMonthFirstDay.time)
        .equalTo("typeName", Transaction.Type.MONTH.name)
        .greaterThan("value", 0)
        .or()
        // Month mandatory loss
        .equalTo("actionTimestamp", currentMonthFirstDay.time)
        .equalTo("typeName", Transaction.Type.MONTH.name)
        .lessThan("value", 0)
        .or()
        // Month loss
        .greaterThanOrEqualTo("actionTimestamp", currentMonthFirstDay.time)
        .lessThan("actionTimestamp", nextMonthFirstDay.time)
        .lessThan("actionTimestamp", todayDate.time)
        .equalTo("typeName", Transaction.Type.INSTANT.name)
        .lessThan("value", 0)
        // Now sum it all
        .sum("value")

    // Intentionally considering only currentMonthFirstDay and nextMonthFirstDay for Cache
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MonthRestSpecification

        if (currentMonthFirstDay != other.currentMonthFirstDay) return false
        if (nextMonthFirstDay != other.nextMonthFirstDay) return false

        return true
    }

    // Intentionally considering only currentMonthFirstDay and nextMonthFirstDay for Cache
    override fun hashCode(): Int {
        var result = currentMonthFirstDay.hashCode()
        result = 31 * result + nextMonthFirstDay.hashCode()
        return result
    }

}