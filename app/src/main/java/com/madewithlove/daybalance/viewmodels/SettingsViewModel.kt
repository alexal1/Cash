/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.CashApp.Companion.PREFS_AUTO_SWITCH_CURRENCY
import com.madewithlove.daybalance.CashApp.Companion.PREFS_SHOW_PUSH_NOTIFICATIONS
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
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