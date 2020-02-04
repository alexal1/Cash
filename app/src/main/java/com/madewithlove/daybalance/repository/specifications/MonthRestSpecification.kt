package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class MonthRestSpecification(
    private val todayDate: Date,
    private val currentMonthFirstDay: Date,
    private val nextMonthFirstDay: Date
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

}