/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.di

import android.content.Context
import com.madewithlove.daybalance.CashApp.Companion.CASH_APP_PREFERENCES
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.CategoriesManager
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.TipsManager
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.viewmodels.*
import com.madewithlove.daybalance.viewmodels.cache.CacheLogic
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
import com.madewithlove.daybalance.viewmodels.cache.DataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainViewModel(androidApplication()) }
    viewModel { NewTransactionViewModel(androidApplication()) }
    viewModel { DayTransactionsViewModel(androidApplication()) }
    viewModel { SettingsViewModel(androidApplication()) }
    viewModel { DebugSettingsViewModel(androidApplication()) }
    viewModel { SplashViewModel(androidApplication()) }
}

val sharedPreferencesModule = module {
    single { androidContext().getSharedPreferences(CASH_APP_PREFERENCES, Context.MODE_PRIVATE) }
}

val helpersModule = module {
    single { CategoriesManager(androidContext(), get()) }
    single { CurrencyManager(get(), androidContext().currentLocale()) }
    single { PushManager(androidContext()) }
    single { TipsManager(androidContext(), get()) }
    single { Analytics(androidContext()) }
}

val repositoryModule = module {
    single { TransactionsRepository() }
}

val cacheModule = module {
    single {
        val dataSource = DataSource(get(), get())
        val cacheLogic = CacheLogic(dataSource)
        CacheLogicAdapter(cacheLogic)
    }
}