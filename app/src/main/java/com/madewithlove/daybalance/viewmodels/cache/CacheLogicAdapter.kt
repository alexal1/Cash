/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels.cache

import com.madewithlove.daybalance.CashApp
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*

class CacheLogicAdapter(private val cacheLogic: CacheLogic) {

    fun requestDate(date: Date): Observable<MomentData>
            = cacheLogic.requestMoment((date.time / CashApp.millisInDay).toInt())

    fun clear(): Completable = cacheLogic.clear()
}