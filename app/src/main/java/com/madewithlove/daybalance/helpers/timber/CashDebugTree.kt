/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers.timber

import timber.log.Timber

/**
 * Just logs messages to logcat.
 */
class CashDebugTree : Timber.DebugTree() {

    companion object {
        private const val PREFIX = "Cash"
        private const val MAX_TAG_LENGTH = 23
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val tagWithPrefix = "$PREFIX$tag".take(MAX_TAG_LENGTH)
        super.log(priority, tagWithPrefix, message, t)
    }

}