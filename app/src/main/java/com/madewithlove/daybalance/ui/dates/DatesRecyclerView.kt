/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.dates

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.MotionEvent.*
import androidx.core.util.rangeTo
import androidx.recyclerview.widget.RecyclerView
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.utils.ZeroInterpolator
import com.madewithlove.daybalance.utils.onNextConsumer
import com.madewithlove.daybalance.utils.screenSize
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.dip
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

class DatesRecyclerView(context: Context) : RecyclerView(context), KoinComponent {

    companion object {

        private const val SCROLL_ANIMATION_DEBOUNCE = 400L
        private const val TOUCH_RADIUS_DP = 16

    }

    private val analytics: Analytics by inject()
    private val dateSubject = PublishSubject.create<Date>()
    private val centerItemClickSubject = PublishSubject.create<Unit>()
    private val datesLayoutManager = DatesLayoutManager(context)
    private val datesSnapHelper = DatesSnapHelper()
    private val prevGuidelineX = context.screenSize().x * 0.25f
    private val nextGuidelineX = context.screenSize().x * 0.75f
    private val offset = context.screenSize().x / 4
    private val touchRadius = context.dip(TOUCH_RADIUS_DP).toFloat()
    private val touchRange = 0f.rangeTo(touchRadius)

    private var prevDownPoint: PointF? = null
    private var nextDownPoint: PointF? = null
    private var swipeDownPoint: PointF? = null
    private var lastSwipeTime = 0L

    val dateObservable: Observable<Date> = dateSubject.distinctUntilChanged().skip(1)
    val centerItemClickObservable: Observable<Unit> = centerItemClickSubject
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
        datesLayoutManager.scrollToPositionWithOffset(newPos, offset)
        datesSnapHelper.lastPos = newPos

        dateSubject.onNext(date)
    }

    fun swipeNext() {
        analytics.dateSwipeNext(isByButton = true)
        val position = datesLayoutManager.findLastVisibleItemPosition()
        swipeToPos(position)
    }

    fun swipePrev() {
        analytics.dateSwipePrev(isByButton = true)
        val position = datesLayoutManager.findFirstVisibleItemPosition()
        swipeToPos(position)
    }


    private fun swipeToPos(position: Int) {
        val view = datesLayoutManager.findViewByPosition(position)!!
        val snapDistance = datesSnapHelper.calculateDistanceToFinalSnap(datesLayoutManager, view)!!
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            if (System.currentTimeMillis() - lastSwipeTime < SCROLL_ANIMATION_DEBOUNCE) {
                smoothScrollBy(snapDistance[0], snapDistance[1],
                    ZeroInterpolator()
                )
                requestLayout()
            } else {
                smoothScrollBy(snapDistance[0], snapDistance[1])
            }
        }

        datesSnapHelper.lastPos = position
        lastSwipeTime = System.currentTimeMillis()
    }

    private fun MotionEvent.distanceTo(point: PointF?): Float {
        point ?: return -1f
        return sqrt((x - point.x).pow(2) + (y - point.y).pow(2))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            ACTION_DOWN -> {
                datesSnapHelper.isSwipeEnabled = false
                swipeDownPoint = PointF(ev.x, ev.y)

                if (ev.x < prevGuidelineX) {
                    prevDownPoint = PointF(ev.x, ev.y)
                    goPrevSubject.onNext(ACTION_DOWN)
                    return super.dispatchTouchEvent(ev)
                }

                if (ev.x > nextGuidelineX) {
                    nextDownPoint = PointF(ev.x, ev.y)
                    goNextSubject.onNext(ACTION_DOWN)
                    return super.dispatchTouchEvent(ev)
                }
            }

            ACTION_MOVE -> {
                if (!datesSnapHelper.isSwipeEnabled && ev.distanceTo(swipeDownPoint) > touchRadius) {
                    datesSnapHelper.isSwipeEnabled = true
                }

                if (ev.distanceTo(prevDownPoint) > touchRadius) {
                    prevDownPoint = null
                    goPrevSubject.onNext(ACTION_CANCEL)
                    return super.dispatchTouchEvent(ev)
                }

                if (ev.distanceTo(nextDownPoint) > touchRadius) {
                    nextDownPoint = null
                    goNextSubject.onNext(ACTION_CANCEL)
                    return super.dispatchTouchEvent(ev)
                }
            }

            ACTION_UP -> {
                if (touchRange.contains(ev.distanceTo(prevDownPoint))) {
                    prevDownPoint = null
                    goPrevSubject.onNext(ACTION_UP)
                    return super.dispatchTouchEvent(ev)
                }

                if (touchRange.contains(ev.distanceTo(nextDownPoint))) {
                    nextDownPoint = null
                    goNextSubject.onNext(ACTION_UP)
                    return super.dispatchTouchEvent(ev)
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

}