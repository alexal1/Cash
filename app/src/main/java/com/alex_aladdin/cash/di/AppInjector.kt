package com.alex_aladdin.cash.di

import android.content.Context
import com.alex_aladdin.cash.CashApp.Companion.CASH_APP_PREFERENCES
import com.alex_aladdin.cash.helpers.CategoriesManager
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.utils.currentLocale
import com.alex_aladdin.cash.viewmodels.DayTransactionsViewModel
import com.alex_aladdin.cash.viewmodels.MainViewModel
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainViewModel(androidApplication()) }
    viewModel { NewTransactionViewModel(androidApplication()) }
    viewModel { DayTransactionsViewModel(androidApplication()) }
}

val sharedPreferencesModule = module {
    single { androidContext().getSharedPreferences(CASH_APP_PREFERENCES, Context.MODE_PRIVATE) }
}

val helpersModule = module {
    single { CategoriesManager(get()) }
    single { CurrencyManager(get(), androidContext().currentLocale()) }
}

val repositoryModule = module {
    single { TransactionsRepository() }
}