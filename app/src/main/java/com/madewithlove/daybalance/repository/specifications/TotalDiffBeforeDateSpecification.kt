/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class TotalDiffBeforeDateSpecification(private val before: Date) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .lessThan("actionTimestamp", before.time)
        .or()
        .equalTo("typeName", Transaction.Type.INTO_MONEYBOX.name)
        .sum("value")

}