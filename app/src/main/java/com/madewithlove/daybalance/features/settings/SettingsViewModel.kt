/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.helpers.push.PushManager

class SettingsViewModel(
    application: Application,
    private val sharedPreferences: SharedPreferences,
    private val pushManager: PushManager
) : AndroidViewModel(application) {

    private var areSettingsChanged = false


    fun getNotificationsEnabled(): Boolean {
        val isEnabled = sharedPreferences.getBoolean(CashApp.PREFS_SHOW_PUSH_NOTIFICATIONS, true)

        if (!isEnabled) {
            return false
        }

        val isEnabledInSettings = pushManager.areNotificationsEnabled()

        if (!isEnabledInSettings) {
            sharedPreferences.edit {
                putBoolean(CashApp.PREFS_SHOW_PUSH_NOTIFICATIONS, false)
            }
            pushManager.cancelPushNotifications()
            return false
        }

        return true
    }

    fun disableNotifications() {
        sharedPreferences.edit {
            putBoolean(CashApp.PREFS_SHOW_PUSH_NOTIFICATIONS, false)
        }
        pushManager.cancelPushNotifications()
        areSettingsChanged = true
    }

    fun tryEnableNotifications(): Boolean {
        if (!pushManager.areNotificationsEnabled()) {
            return false
        }

        sharedPreferences.edit {
            putBoolean(CashApp.PREFS_SHOW_PUSH_NOTIFICATIONS, true)
        }
        pushManager.schedulePushNotifications()
        areSettingsChanged = true

        return true
    }

    fun showPush() {
        pushManager.showPushNotification(true)
    }

}