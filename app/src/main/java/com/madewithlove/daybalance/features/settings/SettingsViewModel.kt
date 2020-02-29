/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.CashApp.Companion.PREFS_LOGS_ENABLED
import com.madewithlove.daybalance.CashApp.Companion.PREFS_SHOW_PUSH_NOTIFICATIONS
import com.madewithlove.daybalance.helpers.ShowcaseManager
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.helpers.timber.CashDebugTree
import timber.log.Timber

class SettingsViewModel(
    application: Application,
    private val sharedPreferences: SharedPreferences,
    private val pushManager: PushManager,
    private val showcaseManager: ShowcaseManager
) : AndroidViewModel(application) {

    fun areNotificationsEnabled(): Boolean {
        val isEnabled = sharedPreferences.getBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, true)

        if (!isEnabled) {
            return false
        }

        val isEnabledInSettings = pushManager.areNotificationsEnabled()

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
    }

    fun tryEnableNotifications(): Boolean {
        if (!pushManager.areNotificationsEnabled()) {
            return false
        }

        sharedPreferences.edit {
            putBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, true)
        }
        pushManager.schedulePushNotifications()

        return true
    }

    fun showPush() {
        pushManager.showPushNotification(true)
    }

    fun areLogsEnabled(): Boolean {
        if (CashApp.isDebugBuild) {
            return true
        }

        return sharedPreferences.getBoolean(PREFS_LOGS_ENABLED, false)
    }

    fun setLogsEnabled(enabled: Boolean): Boolean {
        if (CashApp.isDebugBuild) {
            return false
        }

        sharedPreferences.edit {
            putBoolean(PREFS_LOGS_ENABLED, enabled)
        }

        if (enabled) {
            Timber.plant(CashDebugTree())
        } else {
            val debugTree = Timber.forest().firstOrNull { it is CashDebugTree }
            debugTree?.let(Timber::uproot)
        }

        return true
    }

    fun repeatShowcase() {
        showcaseManager.reset()
    }

}