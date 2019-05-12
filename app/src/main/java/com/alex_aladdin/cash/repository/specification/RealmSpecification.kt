package com.alex_aladdin.cash.repository.specification

import com.alex_aladdin.cash.repository.entities.Transaction
import io.realm.Realm
import io.realm.RealmResults

interface RealmSpecification{

    fun toRealmResults(realm: Realm): RealmResults<Transaction>

}