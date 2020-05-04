/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import android.app.Activity
import android.content.Context
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class Analytics(context: Context) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)


    fun setCurrentScreen(activity: Activity, screenName: String) {
        Timber.d("setCurrentScreen: $screenName")
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
    }

    fun dateSwipeNext(isByButton: Boolean) {
        Timber.d("dateSwipeNext, isByButton: $isByButton")
        val bundle = bundleOf("direction" to "next")
        firebaseAnalytics.logEvent("date_swipe", bundle)
    }

    fun dateSwipePrev(isByButton: Boolean) {
        Timber.d("dateSwipePrev, isByButton: $isByButton")
        val bundle = bundleOf("direction" to "prev")
        firebaseAnalytics.logEvent("date_swipe", bundle)
    }

    fun openCalendar(isDatePicked: Boolean) {
        Timber.d("openCalendar, isDatePicked: $isDatePicked")
        val bundle = bundleOf("is_date_picked" to isDatePicked)
        firebaseAnalytics.logEvent("open_calendar", bundle)
    }

    fun createTransaction(isWithComment: Boolean) {
        Timber.d("createTransaction, isWithComment: $isWithComment")
        val bundle = bundleOf("is_with_comment" to isWithComment)
        firebaseAnalytics.logEvent("create_transaction", bundle)
    }

    fun deleteTransactions(count: Int) {
        Timber.d("deleteTransactions, count: $count")
        val bundle = bundleOf("count" to count)
        firebaseAnalytics.logEvent("delete_transaction", bundle)
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
    }

}