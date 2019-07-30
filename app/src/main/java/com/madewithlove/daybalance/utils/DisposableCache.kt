/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicReference

class DisposableCache {

    private val cache = AtomicReference(listOf<Disposable>())

    fun registerDisposable(newItem: Disposable) {
        do {
            val current = cache.get()
        } while (!cache.compareAndSet(current, current + newItem))
    }

    fun drain() {
        val list = cache.getAndSet(listOf())
        for (disposable in list) {
            disposable.dispose()
        }
    }

}