/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers.timber

import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Logs messages to Crashlytics and sends exceptions to Crashlytics.
 */
class CashReleaseTree : Timber.Tree() {

    companion object {

        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"


        fun logExceptionToCrashlytics(throwable: Throwable, message: String, priority: Int, tag: String?) {
            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority)
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag)
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message)
            Crashlytics.logException(throwable)
        }

    }


    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        Crashlytics.log(message)

        throwable?.let {
            logExceptionToCrashlytics(throwable, message, priority, tag)
        }
    }

}