package com.madewithlove.daybalance.helpers.timber

import android.util.Log
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import timber.log.Timber

class KoinLogger : Logger() {

    override fun log(level: Level, msg: MESSAGE) {
        val priority = when (level) {
            Level.DEBUG -> Log.DEBUG
            Level.INFO -> Log.INFO
            Level.ERROR -> Log.ERROR
        }

        Timber.log(priority, msg)
    }

}