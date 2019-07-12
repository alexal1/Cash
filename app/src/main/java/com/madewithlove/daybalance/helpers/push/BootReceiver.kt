package com.madewithlove.daybalance.helpers.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.madewithlove.daybalance.CashApp.Companion.PREFS_SHOW_PUSH_NOTIFICATIONS
import org.koin.core.KoinComponent
import org.koin.core.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val pushManager: PushManager by inject()
    private val sharedPreferences: SharedPreferences by inject()


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            if (sharedPreferences.getBoolean(PREFS_SHOW_PUSH_NOTIFICATIONS, true)) {
                pushManager.schedulePushNotifications()
            }
        }
    }

}