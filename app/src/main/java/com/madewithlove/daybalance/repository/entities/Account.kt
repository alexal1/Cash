/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.repository.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

open class Account : RealmObject(), Serializable {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    var currencyIndex: Int = 0

}