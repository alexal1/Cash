/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.dates

import android.content.Context
import android.view.MotionEvent
import android.view.MotionEvent.*
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
    private val prevGuidelineX = context.screenSize().x * 0.25f
    private val nextGuidelineX = context.screenSize().x * 0.75f

    private var prevPressed = false
    private var nextPressed = false

    val dateObservable: Observable<Date> = dateSubject.distinctUntilChanged().skip(1)
    val centerItemClickObservable: Observable<Unit> = centerItemClickSubject.throttleFirst(1, TimeUnit.SECONDS)
    val goPrevSubject = PublishSubject.create<Int>()
    val goNextSubject = PublishSubject.create<Int>()


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

    fun swipeNext() {
        val position = datesLayoutManager.findLastVisibleItemPosition()
        swipeToPos(position)
    }

    fun swipePrev() {
        val position = datesLayoutManager.findFirstVisibleItemPosition()
        swipeToPos(position)
    }


    private fun swipeToPos(position: Int) {
        val view = datesLayoutManager.findViewByPosition(position)!!
        val snapDistance = datesSnapHelper.calculateDistanceToFinalSnap(datesLayoutManager, view)!!
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            smoothScrollBy(snapDistance[0], snapDistance[1])
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            ACTION_DOWN -> {
                if (ev.x < prevGuidelineX) {
                    prevPressed = true
                    goPrevSubject.onNext(ACTION_DOWN)
                }

                if (ev.x > nextGuidelineX) {
                    nextPressed = true
                    goNextSubject.onNext(ACTION_DOWN)
                }
            }

            ACTION_MOVE -> {
                if (prevPressed && ev.x > prevGuidelineX) {
                    prevPressed = false
                    goPrevSubject.onNext(ACTION_CANCEL)
                }

                if (nextPressed && ev.x < nextGuidelineX) {
                    nextPressed = false
                    goNextSubject.onNext(ACTION_CANCEL)
                }
            }

            ACTION_UP -> {
                if (prevPressed && ev.x < prevGuidelineX) {
                    prevPressed = false
                    goPrevSubject.onNext(ACTION_UP)
                }

                if (nextPressed && ev.x > nextGuidelineX) {
                    nextPressed = false
                    goNextSubject.onNext(ACTION_UP)
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

}