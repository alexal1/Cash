package com.alex_aladdin.cash.helpers.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.KoinComponent
import org.koin.core.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val pushManager: PushManager by inject()


    override fun onReceive(context: Context, intent: Intent) {
        pushManager.showPushNotification()
    }

}