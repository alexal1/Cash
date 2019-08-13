/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import io.realm.DynamicRealm
import io.realm.RealmMigration
import timber.log.Timber

class CashRealmMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Timber.e("Realm migration missing from v$oldVersion to v$newVersion")
    }

}