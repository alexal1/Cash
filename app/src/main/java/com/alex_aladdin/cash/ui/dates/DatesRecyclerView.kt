package com.alex_aladdin.cash.ui.dates

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.utils.screenSize
import io.reactivex.Observable
import java.util.*

class DatesRecyclerView(context: Context) : RecyclerView(context) {

    private val datesLayoutManager = DatesLayoutManager(context)

    private lateinit var todayDate: Date


    fun init(todayDate: Date, locale: Locale): Observable<Date> {
        this.todayDate = todayDate

        layoutManager = datesLayoutManager
        DatesSnapHelper().attachToRecyclerView(this)
        setHasFixedSize(true)

        val datesAdapter = DatesAdapter(locale, todayDate)
        adapter = datesAdapter

        return datesAdapter.dateObservable
    }


    fun setDate(date: Date) = post {
        val daysCountDiff = ((date.time - todayDate.time) / CashApp.millisInDay).toInt()
        val todayPos = Int.MAX_VALUE / 2
        val newPos = todayPos + daysCountDiff
        val offset = context.screenSize().x / 4
        datesLayoutManager.scrollToPositionWithOffset(newPos, offset)
    }
}