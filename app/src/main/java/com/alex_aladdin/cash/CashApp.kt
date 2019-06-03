package com.alex_aladdin.cash

import android.app.Application
import com.alex_aladdin.cash.di.helpersModule
import com.alex_aladdin.cash.di.repositoryModule
import com.alex_aladdin.cash.di.sharedPreferencesModule
import com.alex_aladdin.cash.di.viewModelsModule
import com.alex_aladdin.cash.helpers.timber.CashDebugTree
import com.alex_aladdin.cash.utils.currentLocale
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*
import java.util.Calendar.*

class CashApp : Application() {

    companion object {

        const val CASH_APP_PREFERENCES = "com.alex_aladdin.cash.CASH_APP_PREFERENCES"
        const val PREFS_CURRENT_CURRENCY_INDEX = "current_currency_index"
        const val PREFS_DEFAULT_PICKER_CURRENCY_INDEX = "default_picker_currency_index"
        const val PREFS_CATEGORIES_PREFIX = "category_"
        const val PREFS_DEFAULT_LOSS_CATEGORY = "default_loss_category"
        const val PREFS_DEFAULT_GAIN_CATEGORY = "default_gain_category"

    }


    val todayDate: Date by lazy {
        GregorianCalendar.getInstance(currentLocale())
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
            modules(viewModelsModule, sharedPreferencesModule, helpersModule, repositoryModule)
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
    }

}