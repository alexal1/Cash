/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.madewithlove.daybalance.di.*
import com.madewithlove.daybalance.helpers.CashRealmMigration
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.helpers.timber.CashDebugTree
import com.madewithlove.daybalance.helpers.timber.CashReleaseTree
import com.madewithlove.daybalance.helpers.timber.KoinLogger
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*
import java.util.Calendar.*

class CashApp : Application(), LifecycleObserver {

    companion object {

        const val CASH_APP_PREFERENCES = "com.madewithlove.daybalance.CASH_APP_PREFERENCES"
        const val PREFS_CURRENT_CURRENCY_INDEX = "current_currency_index"
        const val PREFS_DEFAULT_PICKER_CURRENCY_INDEX = "default_picker_currency_index"
        const val PREFS_CATEGORIES_PREFIX = "category_"
        const val PREFS_DEFAULT_LOSS_CATEGORY = "default_loss_category"
        const val PREFS_DEFAULT_GAIN_CATEGORY = "default_gain_category"
        const val PREFS_AUTO_SWITCH_CURRENCY = "auto_switch_currency"
        const val PREFS_IS_FIRST_LAUNCH = "is_first_launch"
        const val PREFS_SHOW_PUSH_NOTIFICATIONS = "show_push_notifications"
        const val PREFS_TIPS_PREFIX = "tip_"
        const val PREFS_LOGS_ENABLED = "logs_enabled"

        const val millisInDay = 24 * 60 * 60 * 1000L

    }


    private val sharedPreferences: SharedPreferences by inject()
    private val pushManager: PushManager by inject()

    val todayDate: Date by lazy {
        GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+0000"))
            .apply {
                set(HOUR_OF_DAY, 0)
                set(MINUTE, 0)
                set(SECOND, 0)
                set(MILLISECOND, 0)
            }
            .time
    }

    val currentDate by lazy {
        BehaviorSubject.createDefault(todayDate)
    }

    var isInForeground = false; private set


    init {
        RxJavaPlugins.setErrorHandler { throwable ->
            Timber.e(throwable)
        }
    }


    override fun onCreate() {
        super.onCreate()

        val tree = when (BuildConfig.BUILD_TYPE) {
            "debug" -> CashDebugTree()
            else -> CashReleaseTree()
        }

        Timber.plant(tree)


        startKoin {
            logger(KoinLogger())
            androidContext(this@CashApp)
            modules(viewModelsModule, sharedPreferencesModule, helpersModule, repositoryModule, cacheModule)
        }


        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("cash.realm")
            .schemaVersion(0)
            .migration(CashRealmMigration())
            .build()

        Realm.setDefaultConfiguration(config)


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


        if (sharedPreferences.getBoolean(PREFS_IS_FIRST_LAUNCH, true)) {
            sharedPreferences.edit {
                putBoolean(PREFS_IS_FIRST_LAUNCH, false)
            }
            pushManager.schedulePushNotifications()
        }


        val areLogsEnabled = sharedPreferences.getBoolean(PREFS_LOGS_ENABLED, false)
        val isDebugBuild = BuildConfig.BUILD_TYPE == "debug"

        if (!isDebugBuild && areLogsEnabled) {
            Timber.plant(CashDebugTree())
        }


        // Night mode always enabled
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onAppForegrounded() {
        Timber.i("App is in foreground")
        isInForeground = true
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onAppBackgrounded() {
        Timber.i("App is in background")
        isInForeground = false
    }

}