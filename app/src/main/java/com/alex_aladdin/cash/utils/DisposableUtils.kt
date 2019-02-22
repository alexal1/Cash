package com.alex_aladdin.cash.utils

import io.reactivex.disposables.Disposable

fun Disposable.cache(cache: DisposableCache): Disposable {
    cache.registerDisposable(this)
    return this
}