/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.madewithlove.daybalance.CashApp.Companion.PREFS_ANALYTICS_CREATE_COUNT
import com.madewithlove.daybalance.CashApp.Companion.PREFS_ANALYTICS_DELETE_COUNT
import com.madewithlove.daybalance.CashApp.Companion.PREFS_ANALYTICS_SESSIONS_COUNT
import com.madewithlove.daybalance.CashApp.Companion.PREFS_ANALYTICS_TOTAL_TIME
import timber.log.Timber

class Analytics(
    context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val facebookAnalytics = AppEventsLogger.newLogger(context)

    private var startSessionTime: Long? = null


    fun setCurrentScreen(activity: Activity, screenName: String) {
        Timber.d("setCurrentScreen: $screenName")
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
    }

    fun dateSwipeNext(isByButton: Boolean) {
        Timber.d("dateSwipeNext, isByButton: $isByButton")
        val bundle = bundleOf("direction" to "next", "is_by_button" to isByButton)
        firebaseAnalytics.logEvent("date_swipe", bundle)
    }

    fun dateSwipePrev(isByButton: Boolean) {
        Timber.d("dateSwipePrev, isByButton: $isByButton")
        val bundle = bundleOf("direction" to "prev", "is_by_button" to isByButton)
        firebaseAnalytics.logEvent("date_swipe", bundle)
    }

    fun openCalendar(isDatePicked: Boolean) {
        Timber.d("openCalendar, isDatePicked: $isDatePicked")
        val bundle = bundleOf("is_date_picked" to isDatePicked)
        firebaseAnalytics.logEvent("open_calendar", bundle)
    }

    fun createTransaction(isWithComment: Boolean) {
        val bundle = bundleOf("is_with_comment" to isWithComment)
        firebaseAnalytics.logEvent("create_transaction", bundle)

        val createdTransactions = incrementSharedPreferencesCounter(PREFS_ANALYTICS_CREATE_COUNT)
        firebaseAnalytics.setUserProperty("transactions_created", createdTransactions.toString())

        Timber.d("createTransaction, isWithComment: $isWithComment, createdTransactions: $createdTransactions")

        if (createdTransactions == 1L) {
            Timber.d("First transaction!")
            firebaseAnalytics.logEvent("first_transaction", null)
        }
    }

    fun deleteTransactions(count: Int) {
        val bundle = bundleOf("count" to count)
        firebaseAnalytics.logEvent("delete_transaction", bundle)

        val deletedTransactions = incrementSharedPreferencesCounter(PREFS_ANALYTICS_DELETE_COUNT, count.toLong())
        firebaseAnalytics.setUserProperty("transactions_deleted", deletedTransactions.toString())

        Timber.d("deleteTransactions, count: $count, deletedTransactions: $deletedTransactions")
    }

    fun clickOnPush() {
        Timber.d("clickOnPush")
        firebaseAnalytics.logEvent("click_on_push", null)
    }

    fun splashScreenTime(millis: Long) {
        Timber.d("splashScreenTime: $millis")
        val bundle = bundleOf("millis" to millis)
        firebaseAnalytics.logEvent("splash_screen_time", bundle)
    }

    fun installReferrer(source: String, medium: String) {
        Timber.d("installReferrer, source: $source, medium: $medium")
        val bundle = bundleOf("source" to source, "medium" to medium)
        firebaseAnalytics.logEvent("install_referrer", bundle)
        firebaseAnalytics.setUserProperty("referrer_source", source)
        firebaseAnalytics.setUserProperty("referrer_medium", medium)
    }

    fun switchPushNotifications(newValue: Boolean) {
        Timber.d("switchPushNotifications, newValue: $newValue")
        val bundle = bundleOf("enabled" to newValue)
        firebaseAnalytics.logEvent("switch_push_notifications", bundle)
        firebaseAnalytics.setUserProperty("settings_push", newValue.toYN())
    }

    fun completeShowcase() {
        Timber.d("completeShowcase")
        firebaseAnalytics.logEvent("showcase_completed", null)
        firebaseAnalytics.setUserProperty("showcase_completed", "Y")
        facebookAnalytics.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL)
    }

    fun rateButtonClick() {
        Timber.d("rateButtonClick")
        firebaseAnalytics.logEvent("rate_button_click", null)
    }

    fun setInitialProperties(locale: String, startVersion: String) {
        Timber.d("setStaticProperties, locale: $locale, startVersion: $startVersion")
        firebaseAnalytics.setUserProperty("locale", locale)
        firebaseAnalytics.setUserProperty("start_version", startVersion)
        firebaseAnalytics.setUserProperty("settings_push", "Y")
        firebaseAnalytics.setUserProperty("transactions_created", "0")
        firebaseAnalytics.setUserProperty("transactions_deleted", "0")
        firebaseAnalytics.setUserProperty("showcase_completed", "N")
    }

    fun startSession() {
        startSessionTime = System.currentTimeMillis()
        val sessionsCount = incrementSharedPreferencesCounter(PREFS_ANALYTICS_SESSIONS_COUNT)
        firebaseAnalytics.setUserProperty("sessions_count", sessionsCount.toString())
        Timber.d("startSession, sessionsCount: $sessionsCount")
    }

    fun finishSession() {
        val sessionLength = startSessionTime?.let { System.currentTimeMillis() - it } ?: 0L
        val totalLength = incrementSharedPreferencesCounter(PREFS_ANALYTICS_TOTAL_TIME, sessionLength)
        firebaseAnalytics.setUserProperty("time_spent", totalLength.toString())
        Timber.d("finishSession, totalLength: $totalLength")
    }


    private fun Boolean.toYN(): String {
        return if (this) "Y" else "N"
    }

    private fun incrementSharedPreferencesCounter(key: String, add: Long = 1): Long {
        var value = sharedPreferences.getLong(key, 0)
        value += add
        sharedPreferences.edit {
            putLong(key, value)
        }
        return value
    }

}