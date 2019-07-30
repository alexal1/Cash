/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.madewithlove.daybalance.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class CurrencyPicker(context: Context) : _FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val currencySnapHelper = CurrencySnapHelper { pos -> itemPickedSubject.onNext(pos) }

    private val itemPickedSubject = BehaviorSubject.create<Int>()
    val itemPickedObservable: Observable<Int> = itemPickedSubject.distinctUntilChanged()


    init {
        view {
            backgroundResource = R.drawable.bg_coin
        }.lparams(dimen(R.dimen.currency_picker_item_size), dimen(R.dimen.currency_picker_item_size)) {
            gravity = CENTER_VERTICAL
        }

        recyclerView = recyclerView {
            id = View.generateViewId()
            currencySnapHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
            backgroundColor = Color.TRANSPARENT
        }.lparams(dimen(R.dimen.currency_picker_item_size), dimen(R.dimen.currency_picker_item_size) * 3)

        view {
            backgroundResource = R.drawable.currency_picker_shadow
        }.lparams(dimen(R.dimen.currency_picker_item_size), dimen(R.dimen.currency_picker_item_size) * 3)
    }


    fun setData(data: List<String>, startPos: Int) = post {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        recyclerView.adapter = CurrencyAdapter(data)
        recyclerView.layoutManager = layoutManager

        layoutManager.scrollToPositionWithOffset(startPos, dimen(R.dimen.currency_picker_item_size))
        itemPickedSubject.onNext(startPos)
    }


    private class CurrencyAdapter(private val data: List<String>) : RecyclerView.Adapter<CurrencyViewHolder>() {

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
                typeface = ResourcesCompat.getFont(context, R.font.currencies)
            }
        }

    }

    private class CurrencySnapHelper(private val onScrolledToPos: (pos: Int) -> Unit) : LinearSnapHelper() {

        override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
            val view = super.findSnapView(layoutManager)

            if (view != null) {
                val pos = layoutManager?.getPosition(view)
                pos?.let(onScrolledToPos)
            }

            return view
        }

    }

}