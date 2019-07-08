package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.viewmodels.cache.CacheLogicAdapter
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject

class SplashViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val cache: CacheLogicAdapter by inject()

    val finishCompletable: Completable = cache.requestDate(app.currentDate.value!!)
        .take(1)
        .singleOrError()
        .ignoreElement()

}