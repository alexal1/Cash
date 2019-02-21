package com.alex_aladdin.cash.ui.dates

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.screenSize
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dimen
import org.jetbrains.anko.textView
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH

class DatesAdapter(locale: Locale) : RecyclerView.Adapter<DatesAdapter.DatesViewHolder>() {

    companion object {

        private const val textDateId = 1

    }


    private val calendar = GregorianCalendar.getInstance(locale)
    private val todayDate = Date(System.currentTimeMillis())
    private val todayPos = Int.MAX_VALUE / 2
    private val dateFormatter = SimpleDateFormat("d MMM", locale)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatesViewHolder {
        val view = DatesUI().createView(AnkoContext.create(parent.context, parent))
        return DatesViewHolder(view)
    }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: DatesViewHolder, position: Int) {
        calendar.time = todayDate
        calendar.add(DAY_OF_MONTH, position - todayPos)
        holder.textDate.text = dateFormatter.format(calendar.time)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val offset = recyclerView.context.screenSize().x / 4
        layoutManager.scrollToPositionWithOffset(todayPos, offset)
    }


    private class DatesUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = textDateId
                layoutParams = ViewGroup.LayoutParams(context.screenSize().x / 2, dimen(R.dimen.date_height))
                gravity = CENTER
                textSize = 44f
                includeFontPadding = false
            }
        }

    }

    class DatesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textDate: TextView = itemView.findViewById(textDateId)

    }

}