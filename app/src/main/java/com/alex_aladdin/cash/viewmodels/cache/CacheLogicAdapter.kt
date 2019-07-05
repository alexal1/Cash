package com.alex_aladdin.cash.viewmodels.cache

import com.alex_aladdin.cash.CashApp
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*

class CacheLogicAdapter(private val cacheLogic: CacheLogic) {

    fun requestDate(date: Date): Observable<MomentData>
            = cacheLogic.requestMoment((date.time / CashApp.millisInDay).toInt())

    fun clear(): Completable = cacheLogic.clear()
}