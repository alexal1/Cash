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
import com.madewithlove.daybalance.helpers.RxErrorHandler
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.helpers.timber.CashDebugTree
import com.madewithlove.daybalance.helpers.timber.CashReleaseTree
import com.madewithlove.daybalance.helpers.timber.KoinLogger
import com.madewithlove.daybalance.model.Cache
import com.madewithlove.daybalance.repository.TransactionsRepository
import io.reactivex.plugins.RxJavaPlugins
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class CashApp : Application(), LifecycleObserver {

    companion object {

        const val CASH_APP_PREFERENCES = "com.madewithlove.daybalance.CASH_APP_PREFERENCES"
        const val PREFS_IS_FIRST_LAUNCH = "is_first_launch"
        const val PREFS_SHOW_PUSH_NOTIFICATIONS = "show_push_notifications"
        const val PREFS_LOGS_ENABLED = "logs_enabled"
        const val PREFS_SAVINGS_PREFIX = "savings_for_"
        const val PREFS_SHOWCASE_STEP = "showcase_step"

        val initializationTime = System.currentTimeMillis()
        val isDebugBuild get() = BuildConfig.BUILD_TYPE == "debug"

    }


    private val sharedPreferences: SharedPreferences by inject()
    private val pushManager: PushManager by inject()
    private val transactionsRepository: TransactionsRepository by inject()
    private val cache: Cache by inject()
    private val errorHandler: RxErrorHandler by inject()

    var isInForeground = false; private set


    override fun onCreate() {
        super.onCreate()

        val tree = if (isDebugBuild) {
            CashDebugTree()
        } else {
            CashReleaseTree()
        }

        Timber.plant(tree)


        startKoin {
            logger(KoinLogger())
            androidContext(this@CashApp)
            modules(
                viewModelsModule,
                sharedPreferencesModule,
                helpersModule,
                repositoryModule,
                modelModule
            )
        }


        RxJavaPlugins.setErrorHandler(errorHandler)


        Realm.init(this)
        val configBuilder = RealmConfiguration.Builder()
            .name("cash.realm")
            .schemaVersion(0)

        if (isDebugBuild) {
            configBuilder.deleteRealmIfMigrationNeeded()
        } else {
            configBuilder.migration(CashRealmMigration())
        }

        Realm.setDefaultConfiguration(configBuilder.build())


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


        if (sharedPreferences.getBoolean(PREFS_IS_FIRST_LAUNCH, true)) {
            sharedPreferences.edit {
                putBoolean(PREFS_IS_FIRST_LAUNCH, false)
            }
            pushManager.schedulePushNotifications()
        }


        val areLogsEnabled = sharedPreferences.getBoolean(PREFS_LOGS_ENABLED, false)

        if (!isDebugBuild && areLogsEnabled) {
            Timber.plant(CashDebugTree())
            Timber.i("Not a debug build, but logs are enabled in debug settings")
        }
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
        transactionsRepository.dispose()
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onAppDestroyed() {
        Timber.i("App destroyed")
        pushManager.dispose()
        cache.dispose()
    }

}