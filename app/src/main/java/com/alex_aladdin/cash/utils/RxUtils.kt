package com.alex_aladdin.cash.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.Subject

fun <T> Observable<T>.subscribeOnUi(c: (T) -> Unit) = observeOn(AndroidSchedulers.mainThread()).subscribe(c)

fun <T> Observable<T>.subscribeOnUi(c: (T) -> Unit, ec: (Throwable) -> Unit) = observeOn(AndroidSchedulers.mainThread()).subscribe(c, ec)

fun <T> Observable<T>.subscribeOnUi() = observeOn(AndroidSchedulers.mainThread()).subscribe()

fun <T> Observable<T>.subscribeOnUi(c: Consumer<in T>) = observeOn(AndroidSchedulers.mainThread()).subscribe(c)

fun <T> Observable<T>.subscribeOnUi(c: Consumer<in T>, ec: Consumer<in Throwable>) = observeOn(AndroidSchedulers.mainThread()).subscribe(c, ec)


fun <T> Subject<T>.onNextConsumer() = Consumer<T> { onNext(it) }