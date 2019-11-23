/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class MonthTotalGainSpecification(private val monthFirstDay: Date) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .equalTo("actionTimestamp", monthFirstDay.time)
        .equalTo("typeName", Transaction.Type.MONTH.name)
        .greaterThan("value", 0)
        .sum("value")

}