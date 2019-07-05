package com.alex_aladdin.cash.repository.specifications

import com.alex_aladdin.cash.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class RealBalanceSpecification(private val date: Date, private val currencyIndex: Int) : NumberSpecification {

    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .equalTo("account.currencyIndex", currencyIndex)
        .lessThanOrEqualTo("startTimestamp", date.time)
        .sum("amount")

}