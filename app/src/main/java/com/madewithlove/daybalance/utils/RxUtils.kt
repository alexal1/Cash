/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.functions.Consumer
import io.reactivex.subjects.Subject

@CheckReturnValue
fun <T> Observable<T>.subscribeOnUi(c: (T) -> Unit) = observeOn(AndroidSchedulers.mainThread()).subscribe(c)

@CheckReturnValue
fun <T> Observable<T>.subscribeOnUi(c: (T) -> Unit, ec: (Throwable) -> Unit) = observeOn(AndroidSchedulers.mainThread()).subscribe(c, ec)

@CheckReturnValue
fun <T> Observable<T>.subscribeOnUi() = observeOn(AndroidSchedulers.mainThread()).subscribe()

@CheckReturnValue
fun <T> Observable<T>.subscribeOnUi(c: Consumer<in T>) = observeOn(AndroidSchedulers.mainThread()).subscribe(c)

@CheckReturnValue
fun <T> Observable<T>.subscribeOnUi(c: Consumer<in T>, ec: Consumer<in Throwable>) = observeOn(AndroidSchedulers.mainThread()).subscribe(c, ec)

@CheckReturnValue
fun <T> Single<T>.subscribeOnUi(c: (T) -> Unit) = observeOn(AndroidSchedulers.mainThread()).subscribe(c)

@CheckReturnValue
fun Completable.subscribeOnUi(c: () -> Unit) = observeOn(AndroidSchedulers.mainThread()).subscribe(c)


fun <T> Subject<T>.onNextConsumer() = Consumer<T> { onNext(it) }