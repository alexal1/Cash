/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults

class LastTransactionAnyCurrencySpecification : RealmSpecification {

    override fun toRealmResults(realm: Realm): RealmResults<Transaction> {
        val maxTimestamp = realm
            .where(Transaction::class.java)
            .max("addedTimestamp")
            ?.toLong()
            ?: return getEmptyRealmResults(realm)

        return realm
            .where(Transaction::class.java)
            .equalTo("addedTimestamp", maxTimestamp)
            .findAll()
    }

    private fun getEmptyRealmResults(realm: Realm) = realm
        .where(Transaction::class.java)
        .alwaysFalse()
        .findAll()

}