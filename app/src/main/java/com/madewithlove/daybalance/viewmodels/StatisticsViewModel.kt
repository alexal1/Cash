/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.KoinComponent
import org.koin.core.inject

class StatisticsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    val intervalSubject = BehaviorSubject.create<Interval>()

    private val sharedPreferences: SharedPreferences by inject()
    private val dc = DisposableCache()


    init {
        val lastInterval: Interval = sharedPreferences
            .getString(CashApp.PREFS_STATISTICS_INTERVAL, Interval.THIS_MONTH.name)
            .let { Interval.valueOf(it!!) }

        intervalSubject.onNext(lastInterval)

        intervalSubject
            .distinctUntilChanged()
            .subscribe { interval ->
                sharedPreferences.edit {
                    putString(CashApp.PREFS_STATISTICS_INTERVAL, interval.name)
                }
            }.cache(dc)
    }


    override fun onCleared() {
        dc.drain()
        super.onCleared()
    }


    enum class Interval { THIS_MONTH, THIS_YEAR, ALL_TIME }

}