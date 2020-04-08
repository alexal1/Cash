/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.helpers

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import timber.log.Timber

class CashInstallReferrer(
    private val context: Context,
    private val analytics: Analytics
) {

    private val listener = object : InstallReferrerStateListener {

        override fun onInstallReferrerSetupFinished(responseCode: Int) {
            when (responseCode) {
                InstallReferrerClient.InstallReferrerResponse.OK -> {
                    doLogInstallationSource()
                }

                InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                    Timber.e("API not available on the current Play Store app")
                }

                InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                    Timber.e("Connection couldn't be established")
                }
            }
        }

        override fun onInstallReferrerServiceDisconnected() {
            Timber.i("Disconnected")
            referrerClient = null
        }

    }

    private var referrerClient: InstallReferrerClient? = null


    fun logInstallationSource() {
        val referrerClient = InstallReferrerClient.newBuilder(context).build().also {
            referrerClient = it
        }
        referrerClient.startConnection(listener)
    }

    fun dispose() {
        referrerClient?.endConnection()
        referrerClient = null
    }


    private fun doLogInstallationSource() {
        val referrerClient = referrerClient ?: return
        val referrerUrl = referrerClient.installReferrer.installReferrer
        Timber.i("Install Referrer URL: $referrerUrl")
        dispose()
    }

}