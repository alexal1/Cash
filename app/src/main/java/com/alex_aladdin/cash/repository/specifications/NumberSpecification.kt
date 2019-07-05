package com.alex_aladdin.cash.repository.specifications

import io.realm.Realm

interface NumberSpecification {

    fun toNumber(realm: Realm): Number

}