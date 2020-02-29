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
import com.madewithlove.daybalance.features.settings.SettingsViewModel
import com.madewithlove.daybalance.helpers.*
import com.madewithlove.daybalance.helpers.push.PushManager
import com.madewithlove.daybalance.model.Cache
import com.madewithlove.daybalance.model.CacheDatesMapper
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { BaseViewModel(androidApplication()) }
    viewModel { MainViewModel(androidApplication(), get(), get()) }
    viewModel { (filter: HistorySpecification.Filter) -> HistoryViewModel(androidApplication(), get(), get(), get(), filter) }
    viewModel { (type: CreateViewModel.Type, chosenMonth: Int?) -> CreateViewModel(androidApplication(), get(), get(), get(), get(), type, chosenMonth) }
    viewModel { PlanViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { MoneyboxViewModel(androidApplication(), get(), get(), get()) }
    viewModel { SettingsViewModel(androidApplication(), get(), get(), get()) }
}

val sharedPreferencesModule = module {
    single { androidContext().getSharedPreferences(CASH_APP_PREFERENCES, Context.MODE_PRIVATE) }
}

val helpersModule = module {
    single { DatesManager() }
    single { PushManager(androidContext(), get(), get()) }
    single { Analytics(androidContext()) }
    single { SavingsManager(get()) }
    single { ShowcaseManager(androidContext(), get(), get(), get()) }
}

val repositoryModule = module {
    single { TransactionsRepository() }
}

val modelModule = module {
    single { Cache(get(), get(), get(), CacheDatesMapper()) }
}