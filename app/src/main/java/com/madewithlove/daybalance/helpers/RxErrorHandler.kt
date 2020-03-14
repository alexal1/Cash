/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.helpers

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class RxErrorHandler : Consumer<Throwable> {

    private val errorMessageSubject = PublishSubject.create<String>()
    val errorMessageObservable: Observable<String> = errorMessageSubject


    override fun accept(throwable: Throwable) {
        Timber.e(throwable.cause)
        errorMessageSubject.onNext(throwable.cause?.message ?: "Unknown exception")
    }

}