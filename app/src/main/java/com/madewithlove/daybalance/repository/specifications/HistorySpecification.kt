/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class HistorySpecification : RealmSpecification {

    override fun toRealmResults(realm: Realm): RealmResults<Transaction> = realm
        .where(Transaction::class.java)
        .sort("startTimestamp", Sort.DESCENDING, "addedTimestamp", Sort.DESCENDING)
        .findAll()

}