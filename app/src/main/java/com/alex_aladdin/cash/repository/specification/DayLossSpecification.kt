package com.alex_aladdin.cash.repository.specification

import com.alex_aladdin.cash.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class DayLossSpecification(private val date: Date) : RealmSpecification {

    override fun toRealmResults(realm: Realm): RealmResults<Transaction> = realm
        .where(Transaction::class.java)
        .equalTo("isGain", false)
        .lessThanOrEqualTo("startTimestamp", date.time)
        .greaterThan("endTimestamp", date.time)
        .sort("addedTimestamp", Sort.DESCENDING)
        .findAll()

}