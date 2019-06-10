package com.alex_aladdin.cash.repository.specifications

import com.alex_aladdin.cash.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class DaySpecification(private val date: Date, private val currencyIndex: Int) : RealmSpecification {

    override fun toRealmResults(realm: Realm): RealmResults<Transaction> = realm
        .where(Transaction::class.java)
        .equalTo("account.currencyIndex", currencyIndex)
        .lessThanOrEqualTo("startTimestamp", date.time)
        .greaterThan("endTimestamp", date.time)
        .sort("addedTimestamp", Sort.DESCENDING)
        .findAll()

}