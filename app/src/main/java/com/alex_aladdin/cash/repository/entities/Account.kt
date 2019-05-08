package com.alex_aladdin.cash.repository.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Account : RealmObject() {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    var currencyIndex: Int = 0

}