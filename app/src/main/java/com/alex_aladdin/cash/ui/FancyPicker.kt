package com.alex_aladdin.cash.ui

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alex_aladdin.cash.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class FancyPicker(context: Context) : _FrameLayout(context) {

    private val recyclerView: RecyclerView

    private val itemPickedSubject = BehaviorSubject.createDefault(0)
    val itemPickedObservable: Observable<Int> = itemPickedSubject


    init {
        recyclerView = recyclerView {
            id = R.id.category_picker_recycler_view
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            FancySnapHelper().attachToRecyclerView(this)
            setHasFixedSize(true)
        }.lparams(matchParent, dimen(R.dimen.fancy_picker_item_height) * 3)

        view {
            backgroundResource = R.drawable.fancy_picker_shadow
        }.lparams(matchParent, dimen(R.dimen.fancy_picker_item_height) * 3)
    }


    fun setData(data: List<String>) = post {
        recyclerView.adapter = FancyAdapter(data)
    }


    private class FancyAdapter(private val data: List<String>) : RecyclerView.Adapter<FancyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FancyViewHolder {
            val view = FancyUI().createView(AnkoContext.create(parent.context, parent))
            return FancyViewHolder(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: FancyViewHolder, position: Int) {
            holder.textView.apply {
                text = data[position]

                val layoutParams = layoutParams as MarginLayoutParams
                layoutParams.topMargin = if (position == 0) dimen(R.dimen.fancy_picker_item_height) else 0
                layoutParams.bottomMargin = if (position == itemCount - 1) dimen(R.dimen.fancy_picker_item_height) else 0
            }
        }

    }


    private class FancyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.category_picker_text)

    }

    private class FancyUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.category_picker_text
                layoutParams = RecyclerView.LayoutParams(ui.owner.width, dimen(R.dimen.fancy_picker_item_height))
                gravity = CENTER
                textSize = 16f
                textColorResource = R.color.white
                includeFontPadding = false
                letterSpacing = 0.06f
                typeface = Typeface.DEFAULT_BOLD
                allCaps = true
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        }

    }

    private inner class FancySnapHelper : LinearSnapHelper() {

        override fun findTargetSnapPosition(
            layoutManager: RecyclerView.LayoutManager?,
            velocityX: Int,
            velocityY: Int
        ): Int {
            val pos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
            itemPickedSubject.onNext(pos)
            return pos
        }

    }

}