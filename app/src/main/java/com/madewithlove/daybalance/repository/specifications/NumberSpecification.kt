package com.madewithlove.daybalance.repository.specifications

import io.realm.Realm

interface NumberSpecification {

    fun toNumber(realm: Realm): Number

}