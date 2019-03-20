package com.alex_aladdin.cash.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alex_aladdin.cash.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class CurrencyPicker(context: Context) : _FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val currencyLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    private val itemPickedSubject = BehaviorSubject.createDefault(0)
    val itemPickedObservable: Observable<Int> = itemPickedSubject


    init {
        view {
            backgroundResource = R.drawable.bg_coin
        }.lparams(dimen(R.dimen.currency_picker_item_size), dimen(R.dimen.currency_picker_item_size)) {
            gravity = CENTER_VERTICAL
        }

        recyclerView = recyclerView {
            id = View.generateViewId()
            layoutManager = currencyLayoutManager
            LinearSnapHelper().attachToRecyclerView(this)
            setHasFixedSize(true)
            backgroundColor = Color.TRANSPARENT
        }.lparams(dimen(R.dimen.currency_picker_item_size), dimen(R.dimen.currency_picker_item_size) * 3)

        view {
            backgroundResource = R.drawable.fancy_picker_shadow
        }.lparams(dimen(R.dimen.currency_picker_item_size), dimen(R.dimen.currency_picker_item_size) * 3)
    }


    private val currencyScrollListener = CurrencyScrollListener {
        val itemCount = recyclerView.adapter?.itemCount ?: return@CurrencyScrollListener
        val pos = currencyLayoutManager.run {
            when {
                findFirstVisibleItemPosition() == 0 && findLastVisibleItemPosition() == 1 -> findFirstVisibleItemPosition()
                findFirstVisibleItemPosition() == itemCount - 2 && findLastVisibleItemPosition() == itemCount - 1 -> findLastVisibleItemPosition()
                else -> (findFirstVisibleItemPosition() + findLastVisibleItemPosition()) / 2
            }
        }
        itemPickedSubject.onNext(pos)
    }


    fun setData(data: List<String>) = post {
        recyclerView.adapter = CurrencyAdapter(data, currencyScrollListener)
    }

    fun setCurrentPos(pos: Int) = post {
        currencyLayoutManager.scrollToPositionWithOffset(pos, dimen(R.dimen.currency_picker_item_size))
        itemPickedSubject.onNext(pos)
    }


    private class CurrencyAdapter(
        private val data: List<String>,
        private val scrollListener: RecyclerView.OnScrollListener
    ) : RecyclerView.Adapter<CurrencyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
            val view = CurrencyUI().createView(AnkoContext.create(parent.context, parent))
            return CurrencyViewHolder(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
            holder.textView.apply {
                text = data[position]

                val layoutParams = layoutParams as MarginLayoutParams
                layoutParams.topMargin = if (position == 0) dimen(R.dimen.currency_picker_item_size) else 0
                layoutParams.bottomMargin = if (position == itemCount - 1) dimen(R.dimen.currency_picker_item_size) else 0
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            recyclerView.addOnScrollListener(scrollListener)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            recyclerView.removeOnScrollListener(scrollListener)
        }

    }

    private class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.currency_picker_text)

    }

    private class CurrencyUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.currency_picker_text
                layoutParams = RecyclerView.LayoutParams(ui.owner.width, dimen(R.dimen.currency_picker_item_size))
                gravity = CENTER
                textSize = 32f
                textColorResource = R.color.white
                includeFontPadding = false
                backgroundColor = Color.TRANSPARENT
            }
        }

    }

    private class CurrencyScrollListener(private val onScrolled: () -> Unit) : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                onScrolled()
            }
        }

    }

}