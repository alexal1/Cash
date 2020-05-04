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

    companion object {

        private const val SOURCE_DEFAULT = "unknown_source"
        private const val SOURCE_NOT_GOOGLE_PLAY = "not_google_play"
        private const val SOURCE_NO_CONNECTION_TO_GOOGLE_PLAY = "no_connection_to_google_play"
        private const val MEDIUM_DEFAULT = "unknown_medium"
        private const val MEDIUM_NOT_GOOGLE_PLAY = "not_google_play"
        private const val MEDIUM_NO_CONNECTION_TO_GOOGLE_PLAY = "no_connection_to_google_play"

    }

    private val sourceRegex = Regex("utm_source=([^\\s&]+)")
    private val mediumRegex = Regex("utm_medium=([^\\s&]+)")

    private val listener = object : InstallReferrerStateListener {

        override fun onInstallReferrerSetupFinished(responseCode: Int) {
            when (responseCode) {
                InstallReferrerClient.InstallReferrerResponse.OK -> {
                    doLogInstallationSource()
                }

                InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                    Timber.e("API not available on the current Play Store app")
                    analytics.installReferrer(SOURCE_NOT_GOOGLE_PLAY, MEDIUM_NOT_GOOGLE_PLAY)
                    dispose()
                }

                InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                    Timber.e("Connection couldn't be established")
                    analytics.installReferrer(SOURCE_NO_CONNECTION_TO_GOOGLE_PLAY, MEDIUM_NO_CONNECTION_TO_GOOGLE_PLAY)
                    dispose()
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
        val source = sourceRegex.find(referrerUrl)?.groups?.toList()?.getOrNull(1)?.value ?: SOURCE_DEFAULT
        val medium = mediumRegex.find(referrerUrl)?.groups?.toList()?.getOrNull(1)?.value ?: MEDIUM_DEFAULT
        analytics.installReferrer(source, medium)

        dispose()
    }

}