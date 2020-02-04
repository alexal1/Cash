package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class TotalBalanceSpecification(private val date: Date) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .lessThan("actionTimestamp", date.time)
        .sum("value")

}