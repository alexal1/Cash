package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.viewmodels.cache.CacheLogicAdapter
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.KoinComponent
import org.koin.core.inject

class SettingsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val currencyManager: CurrencyManager by inject()
    private val cache: CacheLogicAdapter by inject()
    private val dc = DisposableCache()

    private val currencyIndexSubject = BehaviorSubject.createDefault(currencyManager.getCurrentCurrencyIndex())
    val currencyObservable: Observable<String> = currencyIndexSubject.map { currencyIndex ->
        currencyManager.getCurrenciesList()[currencyIndex]
    }

    var areSettingsChanged = false; private set


    fun notifyCurrencyWasChanged() {
        areSettingsChanged = true
        currencyIndexSubject.onNext(currencyManager.getCurrentCurrencyIndex())

        cache
            .clear()
            .andThen(cache.requestDate(app.currentDate.value!!))
            .subscribe()
            .cache(dc)
    }

    override fun onCleared() {
        dc.drain()
    }

}