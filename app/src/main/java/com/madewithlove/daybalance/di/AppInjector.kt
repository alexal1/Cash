/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.di

import android.content.Context
import com.madewithlove.daybalance.BaseViewModel
import com.madewithlove.daybalance.CashApp.Companion.CASH_APP_PREFERENCES
import com.madewithlove.daybalance.features.create.CreateViewModel
import com.madewithlove.daybalance.features.history.HistoryViewModel
import com.madewithlove.daybalance.features.main.MainViewModel
import com.madewithlove.daybalance.features.moneybox.MoneyboxViewModel
import com.madewithlove.daybalance.features.plan.PlanViewModel
import com.madewithlove.daybalance.helpers.*
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.model.Cache
import com.madewithlove.daybalance.model.CacheDatesMapper
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.repository.utils.RandomTransactionsIterator
import com.madewithlove.daybalance.repository.utils.RandomTransactionsIteratorFactory
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
    viewModel { BaseViewModel(androidApplication()) }
    viewModel { MainViewModel(androidApplication(), get(), get()) }
    viewModel { (filter: HistorySpecification.Filter) -> HistoryViewModel(androidApplication(), get(), get(), filter) }
    viewModel { (type: CreateViewModel.Type, chosenMonth: Int?) -> CreateViewModel(androidApplication(), get(), get(), get(), type, chosenMonth) }
    viewModel { PlanViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { MoneyboxViewModel(androidApplication(), get(), get(), get()) }
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
    single { DatesManager() }
    single { CategoriesManager(androidContext(), get()) }
    single { CurrencyManager(get(), androidContext().currentLocale()) }
    single { PushManager(androidContext()) }
    single { TipsManager(androidContext(), get()) }
    single { Analytics(androidContext()) }
    single { SavingsManager(get()) }
}

val repositoryModule = module {
    single { TransactionsRepository() }
    single { (count: Int, mode: RandomTransactionsIterator.Mode) -> RandomTransactionsIteratorFactory(androidContext(), get(), get(), count, mode) }
}

val cacheModule = module {
    single {
        val dataSource = DataSource(get(), get())
        val cacheLogic = CacheLogic(dataSource)
        CacheLogicAdapter(cacheLogic)
    }
}

val modelModule = module {
    single { Cache(get(), get(), get(), CacheDatesMapper()) }
}