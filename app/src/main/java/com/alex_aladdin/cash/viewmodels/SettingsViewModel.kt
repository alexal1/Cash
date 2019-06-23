package com.alex_aladdin.cash.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.CashApp.Companion.PREFS_AUTO_SWITCH_CURRENCY
import com.alex_aladdin.cash.CashApp.Companion.PREFS_SHOW_PUSH_NOTIFICATIONS
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.helpers.push.PushManager
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
    private val sharedPreferences: SharedPreferences by inject()
    private val pushManager: PushManager by inject()
    private val dc = DisposableCache()

    private val currencyIndexSubject = BehaviorSubject.createDefault(currencyManager.getCurrentCurrencyIndex())
    val currencyObservable: Observable<String> = currencyIndexSubject.map { currencyIndex ->
        currencyManager.getCurrenciesList()[currencyIndex]
    }

    private val autoSwitchCurrencySubject = BehaviorSubject.createDefault(autoSwitchCurrency)
    val autoSwitchCurrencyObservable: Observable<String> = autoSwitchCurrencySubject.map { autoSwitchCurrency ->
        when (autoSwitchCurrency) {
            0 -> app.getString(R.string.yes)
            1 -> app.getString(R.string.no)
            2 -> app.getString(R.string.ask)
            else -> throw IllegalArgumentException("Unexpected autoSwitchCurrency index ($autoSwitchCurrency)")
        }
    }

    var autoSwitchCurrency: Int
        get() = sharedPreferences.getInt(PREFS_AUTO_SWITCH_CURRENCY, 2)
        set(value) = sharedPreferences.edit {
            putInt(PREFS_AUTO_SWITCH_CURRENCY, value)
            autoSwitchCurrencySubject.onNext(value)
            areSettingsChanged = true
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

    fun getNotificationsEnabled(): Boolean {
        val isEnabled = sharedPreferences.getBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, true)

        if (!isEnabled) {
            return false
        }

        val isEnabledInSettings = pushManager.checkIfNotificationsEnabled()

        if (!isEnabledInSettings) {
            sharedPreferences.edit {
                putBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, false)
            }
            pushManager.cancelPushNotifications()
            return false
        }

        return true
    }

    fun disableNotifications() {
        sharedPreferences.edit {
            putBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, false)
        }
        pushManager.cancelPushNotifications()
        areSettingsChanged = true
    }

    fun tryEnableNotifications(): Boolean {
        if (!pushManager.checkIfNotificationsEnabled()) {
            return false
        }

        sharedPreferences.edit {
            putBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, true)
        }
        pushManager.schedulePushNotifications()
        areSettingsChanged = true

        return true
    }

    override fun onCleared() {
        dc.drain()
    }

}