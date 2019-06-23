package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.helpers.push.PushManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class DebugSettingsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val pushManager: PushManager by inject()


    fun showPush() {
        pushManager.showPushNotification(true)
    }

}