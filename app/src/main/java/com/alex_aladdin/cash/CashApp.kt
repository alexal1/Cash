package com.alex_aladdin.cash

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.alex_aladdin.cash.di.*
import com.alex_aladdin.cash.helpers.push.PushManager
import com.alex_aladdin.cash.helpers.timber.CashDebugTree
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*
import java.util.Calendar.*

class CashApp : Application(), LifecycleObserver {

    companion object {

        const val CASH_APP_PREFERENCES = "com.alex_aladdin.cash.CASH_APP_PREFERENCES"
        const val PREFS_CURRENT_CURRENCY_INDEX = "current_currency_index"
        const val PREFS_DEFAULT_PICKER_CURRENCY_INDEX = "default_picker_currency_index"
        const val PREFS_CATEGORIES_PREFIX = "category_"
        const val PREFS_DEFAULT_LOSS_CATEGORY = "default_loss_category"
        const val PREFS_DEFAULT_GAIN_CATEGORY = "default_gain_category"
        const val PREFS_AUTO_SWITCH_CURRENCY = "auto_switch_currency"
        const val PREFS_IS_FIRST_LAUNCH = "is_first_launch"
        const val PREFS_SHOW_PUSH_NOTIFICATIONS = "show_push_notifications"

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

        startKoin {
            androidLogger()
            androidContext(this@CashApp)
            modules(viewModelsModule, sharedPreferencesModule, helpersModule, repositoryModule, cacheModule)
        }


        val tree = when(BuildConfig.BUILD_TYPE) {
            "debug" -> CashDebugTree()
            "release" -> TODO()
            else -> throw IllegalArgumentException("Unexpected build type: ${BuildConfig.BUILD_TYPE}")
        }

        Timber.plant(tree)


        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("cash.realm")
            .deleteRealmIfMigrationNeeded() // TODO: remove before production!
            .build()

        Realm.setDefaultConfiguration(config)


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


        if (sharedPreferences.getBoolean(PREFS_IS_FIRST_LAUNCH, true)) {
            sharedPreferences.edit {
                putBoolean(PREFS_IS_FIRST_LAUNCH, false)
            }
            pushManager.schedulePushNotifications()
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
    }

}