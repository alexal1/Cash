/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.dates

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.madewithlove.daybalance.utils.onNextConsumer
import com.madewithlove.daybalance.utils.screenSize
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class DatesRecyclerView(context: Context) : RecyclerView(context) {

    private val dateSubject = PublishSubject.create<Date>()
    private val centerItemClickSubject = PublishSubject.create<Unit>()
    private val datesLayoutManager = DatesLayoutManager(context)
    private val datesSnapHelper = DatesSnapHelper()

    val dateObservable: Observable<Date> = dateSubject.distinctUntilChanged().skip(1)
    val centerItemClickObservable: Observable<Unit> = centerItemClickSubject.throttleFirst(1, TimeUnit.SECONDS)


    fun init(locale: Locale) {
        layoutManager = datesLayoutManager
        datesSnapHelper.attachToRecyclerView(this)
        setHasFixedSize(true)

        val datesAdapter = DatesAdapter(locale, dateSubject.onNextConsumer()) { clickPos ->
            if (clickPos == datesLayoutManager.findFirstCompletelyVisibleItemPosition()) {
                centerItemClickSubject.onNext(Unit)
            }
        }
        adapter = datesAdapter
    }


    fun setDate(date: Date) = post {
        val newPos = (date.time / TimeUnit.DAYS.toMillis(1)).toInt()
        val offset = context.screenSize().x / 4
        datesLayoutManager.scrollToPositionWithOffset(newPos, offset)
        datesSnapHelper.lastPos = newPos

        dateSubject.onNext(date)
    }
}