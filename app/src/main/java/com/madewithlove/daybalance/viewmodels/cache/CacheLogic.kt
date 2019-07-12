package com.madewithlove.daybalance.viewmodels.cache

import android.os.HandlerThread
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.onNextConsumer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*

class CacheLogic(private val dataSource: DataSource) {

    companion object {
        private const val DETECT_DELTA = 2
        private const val LOAD_DELTA = 5
    }


    private val cache = SparseArray<Moment>()
    private val cacheLogicThread = HandlerThread("CacheLogicThread").apply { start() }
    private val cacheLogicScheduler = AndroidSchedulers.from(cacheLogicThread.looper)
    private val dc = DisposableCache()


    /**
     * Request MomentData at given index. Returns Observable that emits first loaded MomentData and all subsequent
     * MomentDatas that could be emitted after clearing cache. It also preloads LOAD_DELTA moments on both sides if and
     * only if there is at least one NOT ACTUAL Moment in DETECT_DELTA radius.
     */
    fun requestMoment(index: Int): Observable<MomentData> = Single
        .fromCallable {
            obtainMoment(index).also { it.actualize() }
        }
        .doOnSuccess {
            var ok = true
            for (i in 1..LOAD_DELTA) {
                val momentLeft = obtainMoment(index - i)
                val momentRight = obtainMoment(index + i)

                if (i <= DETECT_DELTA) {
                    if (!momentLeft.isActual || !momentRight.isActual) {
                        ok = false
                    }
                } else {
                    if (ok) {
                        break
                    }
                }

                momentLeft.actualize()
                momentRight.actualize()
            }
        }
        .flatMapObservable { moment: Moment ->
            moment.data
        }
        .subscribeOn(cacheLogicScheduler)

    /**
     * Stops all current loadings from the repository and marks all moments as NOT ACTUAL.
     */
    fun clear(): Completable = Completable
        .fromCallable {
            dc.drain()
            cache.forEach { _, moment ->
                moment.isActual = false
            }
            Timber.i("All cached data has become not actual")
        }
        .subscribeOn(cacheLogicScheduler)


    private fun obtainMoment(index: Int): Moment =
        cache[index] ?: Moment(index, dataSource, cacheLogicScheduler, dc).also { cache[index] = it }


    private class Moment(
        private val index: Int,
        private val dataSource: DataSource,
        private val cacheLogicScheduler: Scheduler,
        private val dc: DisposableCache,
        val data: BehaviorSubject<MomentData> = BehaviorSubject.create(),
        @Volatile var isActual: Boolean = false
    ) {

        private val date = Date(index * CashApp.millisInDay)

        fun actualize() {
            if (isActual) {
                return
            }

            Timber.i("Start loading data on $date")
            dataSource
                .loadMoment(index)
                .doAfterSuccess {
                    isActual = true
                    Timber.i("Loaded data on $date")
                }
                .subscribeOn(cacheLogicScheduler)
                .subscribe(data.onNextConsumer())
                .cache(dc)
        }
    }
}