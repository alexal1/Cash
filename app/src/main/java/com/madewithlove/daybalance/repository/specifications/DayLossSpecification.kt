/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

data class DayLossSpecification(val date: Date) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .equalTo("actionTimestamp", date.time)
        .equalTo("typeName", Transaction.Type.INSTANT.name)
        .lessThan("value", 0)
        .sum("value")

}