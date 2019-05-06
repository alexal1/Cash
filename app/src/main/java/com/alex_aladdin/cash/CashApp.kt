package com.alex_aladdin.cash

import android.app.Application
import android.content.Context
import android.util.Log.e
import com.alex_aladdin.cash.helpers.CategoriesManager
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.utils.currentLocale
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.Calendar.*

class CashApp : Application() {

    companion object {

        private const val PREFERENCES_NAME = "CashAppPreferences"
        private const val TAG = "CashApp"

    }


    val currencyManager by lazy {
        CurrencyManager(sharedPreferences)
    }

    val categoriesManager by lazy {
        CategoriesManager(sharedPreferences)
    }

    val todayDate: Date by lazy {
        GregorianCalendar.getInstance(currentLocale())
            .apply {
                set(HOUR, 0)
                set(MINUTE, 0)
                set(SECOND, 0)
                set(MILLISECOND, 0)
            }
            .time
    }

    val currentDate by lazy {
        BehaviorSubject.createDefault(todayDate)
    }

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }


    init {
        RxJavaPlugins.setErrorHandler { throwable ->
            e(TAG, "Uncaught exception: ", throwable)
        }
    }

}