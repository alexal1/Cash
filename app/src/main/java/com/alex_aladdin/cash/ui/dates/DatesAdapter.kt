package com.alex_aladdin.cash.ui.dates

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.screenSize
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH

class DatesAdapter(locale: Locale) : RecyclerView.Adapter<DatesAdapter.DateViewHolder>() {

    companion object {

        private const val textDateId = 1

    }


    private val calendar = GregorianCalendar.getInstance(locale)
    private val todayDate = Date(System.currentTimeMillis())
    private val todayPos = Int.MAX_VALUE / 2
    private val dateFormatter = SimpleDateFormat("d MMM", locale)

    private val dateSubject = BehaviorSubject.createDefault(Date())
    val dateObservable: Observable<Date> = dateSubject.distinctUntilChanged()

    private var datesScrollListener: DatesScrollListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = DateUI().createView(AnkoContext.create(parent.context, parent))
        return DateViewHolder(view)
    }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dateByPos(position)
        holder.textDate.text = dateFormatter.format(date)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        val offset = recyclerView.context.screenSize().x / 4
        layoutManager.scrollToPositionWithOffset(todayPos, offset)

        datesScrollListener = DatesScrollListener {
            val position = layoutManager.findFirstCompletelyVisibleItemPosition()
            val date = dateByPos(position)
            dateSubject.onNext(date)
        }
        recyclerView.addOnScrollListener(datesScrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        datesScrollListener?.let(recyclerView::removeOnScrollListener)
    }

    private fun dateByPos(position: Int): Date {
        calendar.time = todayDate
        calendar.add(DAY_OF_MONTH, position - todayPos)
        return calendar.time
    }


    private class DatesScrollListener(private val onScrolled: () -> Unit) : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                onScrolled()
            }
        }

    }

    private class DateUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = textDateId
                layoutParams = ViewGroup.LayoutParams(context.screenSize().x / 2, dimen(R.dimen.date_height))
                gravity = CENTER
                textSize = 44f
                textColorResource = R.color.white
                includeFontPadding = false
            }
        }

    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textDate: TextView = itemView.findViewById(textDateId)

    }

}