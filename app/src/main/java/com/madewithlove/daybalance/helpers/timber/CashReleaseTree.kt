/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers.timber

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Logs messages to Crashlytics and sends exceptions to Crashlytics.
 */
class CashReleaseTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority < Log.INFO) {
            return
        }

        crashlytics.log(message)

        throwable?.let {
            logExceptionToCrashlytics(throwable, message, priority, tag)
        }
    }

    private fun logExceptionToCrashlytics(throwable: Throwable, message: String, priority: Int, tag: String?) {
        crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority)
        crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)
        tag?.let {
            crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag)
        }
        crashlytics.recordException(throwable)
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}