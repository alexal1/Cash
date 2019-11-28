/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.io.Serializable
import java.util.*

class HistorySpecification(private val filter: Filter) : RealmSpecification {

    override fun toRealmResults(realm: Realm): RealmResults<Transaction> {
        var realmResults = realm.where(Transaction::class.java)

        when (filter) {
            is MonthTotalGainFilter -> {
                realmResults = realmResults
                    .equalTo("actionTimestamp", filter.monthFirstDay.time)
                    .equalTo("typeName", Transaction.Type.MONTH.name)
                    .greaterThan("value", 0)
            }

            is MonthMandatoryLossFilter -> {
                realmResults = realmResults
                    .equalTo("actionTimestamp", filter.monthFirstDay.time)
                    .equalTo("typeName", Transaction.Type.MONTH.name)
                    .lessThan("value", 0)
            }
        }

        return realmResults
            .sort("displayTimestamp", Sort.DESCENDING, "addedTimestamp", Sort.DESCENDING)
            .findAll()
    }


    interface Filter : Serializable

    object Empty : Filter

    data class MonthTotalGainFilter(val monthFirstDay: Date) : Filter

    data class MonthMandatoryLossFilter(val monthFirstDay: Date) : Filter

}