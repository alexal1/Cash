/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.viewmodels.cache.CacheLogicAdapter
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class SplashViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val app = application as CashApp
    private val cache: CacheLogicAdapter by inject()

    val finishCompletable: Completable = cache.requestDate(app.currentDate.value!!)
        .doOnSubscribe {
            Timber.i("Requesting data from cache...")
        }
        .doOnNext {
            Timber.i("Cache data obtained successfully")
        }
        .doOnError { throwable ->
            Timber.e(throwable, "Error when requesting data from cache")
        }
        .take(1)
        .singleOrError()
        .ignoreElement()

}