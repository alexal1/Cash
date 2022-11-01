/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.dates

import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.CalendarFactory
import com.madewithlove.daybalance.utils.screenSize
import io.reactivex.functions.Consumer
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH

class DatesAdapter(
    locale: Locale,
    private val dateConsumer: Consumer<Date>,
    private val onItemClick: (clickPos: Int) -> Unit
) : RecyclerView.Adapter<DatesAdapter.DateViewHolder>() {

    private val calendar = CalendarFactory.getInstance()
    private val dateFormatter = SimpleDateFormat("d MMM", locale)

    private var datesScrollListener: DatesScrollListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = DateUI().createView(AnkoContext.create(parent.context, parent))
        return DateViewHolder(view)
    }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dateByPos(position)
        holder.textDate.text = dateFormatter.format(date)
        holder.textDate.setOnClickListener { onItemClick(position) }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        datesScrollListener = DatesScrollListener {
            val position = layoutManager.findFirstCompletelyVisibleItemPosition()
            val date = dateByPos(position)
            dateConsumer.accept(date)
        }.also {
            recyclerView.addOnScrollListener(it)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        datesScrollListener?.let(recyclerView::removeOnScrollListener)
    }

    private fun dateByPos(position: Int): Date {
        calendar.timeInMillis = 0
        calendar.add(DAY_OF_MONTH, position)
        return calendar.time
    }


    private class DatesScrollListener(private val onScrolled: () -> Unit) : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                onScrolled()
            }
        }

    }

    private class DateUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.text_date
                layoutParams = ViewGroup.LayoutParams(context.screenSize().x / 2, dimen(R.dimen.date_height))
                gravity = CENTER
                textSize = 44f
                textColorResource = R.color.white
                includeFontPadding = false
            }
        }

    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textDate: TextView = itemView.findViewById(R.id.text_date)

    }

}