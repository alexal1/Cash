/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import io.reactivex.disposables.Disposable

fun Disposable.cache(cache: DisposableCache): Disposable {
    cache.registerDisposable(this)
    return this
}