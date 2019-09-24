/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.specifications

import com.madewithlove.daybalance.repository.entities.Transaction
import io.realm.Realm
import java.util.*

class MonthDiffSpecification(private val date: Date, private val currencyIndex: Int) : NumberSpecification {

    private val monthStart: Long = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+0000")).run {
        time = date
        set(Calendar.DAY_OF_MONTH, 1)
        time.time
    }


    override fun toNumber(realm: Realm): Number = realm
        .where(Transaction::class.java)
        .equalTo("account.currencyIndex", currencyIndex)
        .greaterThanOrEqualTo("startTimestamp", monthStart)
        .lessThanOrEqualTo("startTimestamp", date.time)
        .sum("amount")

}