/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class DayLossSpecificationOld(private val date: Date, private val currencyIndex: Int) : RealmSpecification {

    override fun toRealmResults(realm: Realm): RealmResults<Transaction> = realm
        .where(Transaction::class.java)
        .equalTo("account.currencyIndex", currencyIndex)
        .lessThan("amount", 0.0)
        .lessThanOrEqualTo("startTimestamp", date.time)
        .greaterThan("endTimestamp", date.time)
        .sort("startTimestamp", Sort.DESCENDING, "addedTimestamp", Sort.DESCENDING)
        .findAll()

}