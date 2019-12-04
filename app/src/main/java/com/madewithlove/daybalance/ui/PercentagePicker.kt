/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity.*
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.madewithlove.daybalance.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import timber.log.Timber
import kotlin.math.ceil

class PercentagePicker(context: Context) : _FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val percentageSnapHelper = PercentageSnapHelper { pos ->
        itemPickedSubject.onNext(pos)
    }

    private val itemPickedSubject = BehaviorSubject.create<Int>()
    val itemPickedObservable: Observable<Int> = itemPickedSubject.distinctUntilChanged()


    init {
        textView {
            text = "%"
            gravity = END or CENTER_VERTICAL
            textSize = 32f
            textColorResource = R.color.white
            includeFontPadding = false
            backgroundColor = Color.TRANSPARENT
        }.lparams(dimen(R.dimen.percentage_picker_item_size) * 3, dimen(R.dimen.percentage_picker_item_size)) {
            gravity = CENTER
        }

        recyclerView = recyclerView {
            id = View.generateViewId()
            backgroundColor = Color.TRANSPARENT
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            percentageSnapHelper.attachToRecyclerView(this)

            setHasFixedSize(true)
            post {
                adapter = PercentageAdapter()
            }
        }.lparams(matchParent, dimen(R.dimen.percentage_picker_item_size) * 3)

        view {
            backgroundResource = R.drawable.percentage_picker_shadow
        }.lparams(dimen(R.dimen.percentage_picker_item_size), dimen(R.dimen.percentage_picker_item_size) * 3) {
            gravity = CENTER_HORIZONTAL
        }
    }


    fun setData(initialValue: Float) = post {
        val startPos = ceil(initialValue * 100 / 5).toInt()
        Timber.i("p = $startPos")
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        layoutManager.scrollToPositionWithOffset(startPos, dimen(R.dimen.percentage_picker_item_size))
        itemPickedSubject.onNext(startPos)
    }


    private class PercentageAdapter : RecyclerView.Adapter<PercentageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PercentageViewHolder {
            val view = PercentageUI().createView(AnkoContext.create(parent.context, parent))
            return PercentageViewHolder(view)
        }

        override fun getItemCount() = 21

        override fun onBindViewHolder(holder: PercentageViewHolder, position: Int) {
            holder.textView.apply {
                text = (position * 5).toString()

                val layoutParams = layoutParams as MarginLayoutParams
                layoutParams.topMargin = if (position == 0) dimen(R.dimen.percentage_picker_item_size) else 0
                layoutParams.bottomMargin = if (position == itemCount - 1) dimen(R.dimen.percentage_picker_item_size) else 0
                this.layoutParams = layoutParams
            }
        }

    }

    private class PercentageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.percentage_picker_text)

    }

    private class PercentageUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.percentage_picker_text
                layoutParams = RecyclerView.LayoutParams(matchParent, dimen(R.dimen.percentage_picker_item_size))
                gravity = CENTER
                textSize = 32f
                textColorResource = R.color.white
                includeFontPadding = false
                backgroundColor = Color.TRANSPARENT
            }
        }

    }

    private class PercentageSnapHelper(private val onScrolledToPos: (pos: Int) -> Unit) : LinearSnapHelper() {

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