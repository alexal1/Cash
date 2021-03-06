/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class MonthDiffSpecification(
    private val thisMonthFirstDay: Date,
    private val nextMonthFirstDay: Date
) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .greaterThanOrEqualTo("actionTimestamp", thisMonthFirstDay.time)
        .lessThan("actionTimestamp", nextMonthFirstDay.time)
        .not()
        .equalTo("typeName", Transaction.Type.INTO_MONEYBOX.name)
        .sum("value")

}