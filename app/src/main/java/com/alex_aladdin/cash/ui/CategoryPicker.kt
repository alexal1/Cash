package com.alex_aladdin.cash.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.ColorUtils
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.jakewharton.rxbinding3.view.globalLayouts
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class CategoryPicker(context: Context) : _FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val dc = DisposableCache()

    private val averageItemSubject = PublishSubject.create<Pair<Float, Int>>()
    val averageItemObservable: Observable<Pair<Float, Int>> = averageItemSubject

    private val itemPickedSubject = BehaviorSubject.create<Categories>()
    val itemPickedObservable: Observable<Categories> = itemPickedSubject.distinctUntilChanged()


    init {
        recyclerView = recyclerView {
            id = View.generateViewId()
            setHasFixedSize(true)
            backgroundColor = Color.TRANSPARENT
            clipToPadding = false

            globalLayouts().take(1).subscribeOnUi {
                topPadding = (height - dimen(R.dimen.category_picker_item_height)) / 2
                bottomPadding = (height - dimen(R.dimen.category_picker_item_height)) / 2
            }.cache(dc)
        }.lparams(matchParent, matchParent) {
            gravity = CENTER
        }

        view {
            backgroundResource = R.drawable.category_picker_shadow
        }.lparams(matchParent, matchParent)
    }


    fun setData(data: List<Categories>, startPos: Int) = post {
        val layoutManager = LinearLayoutManager(context, VERTICAL, false)
        val scrollListener = CategoryScrollListener(data, context, layoutManager) { averageWidth, averageColor ->
            averageItemSubject.onNext(averageWidth to averageColor)
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = CategoryAdapter(data, scrollListener)
        CategorySnapHelper(data) { category -> itemPickedSubject.onNext(category) }.attachToRecyclerView(recyclerView)

        post {
            layoutManager.scrollToPosition(startPos)
        }
        itemPickedSubject.onNext(data[startPos])
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dc.drain()
    }


    private class CategoryAdapter(
        private val categories: List<Categories>,
        private val scrollListener: RecyclerView.OnScrollListener
    ) : RecyclerView.Adapter<CategoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = CategoryUI().createView(AnkoContext.create(parent.context, parent))
            return CategoryViewHolder(view)
        }

        override fun getItemCount() = categories.size

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.textView.apply {
                text = context.getString(categories[position].stringRes)
                layoutParams =
                    RecyclerView.LayoutParams(WRAP_CONTENT, context.dimen(R.dimen.category_picker_item_height))
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            recyclerView.addOnScrollListener(scrollListener)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            recyclerView.removeOnScrollListener(scrollListener)
        }

    }

    private class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.category_picker_text)

    }

    private class CategoryUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.category_picker_text
                textSize = 16f
                textColorResource = R.color.white
                gravity = Gravity.CENTER_VERTICAL
                includeFontPadding = false
                backgroundColor = Color.TRANSPARENT
                setPadding(dip(12), 0, dip(12), 0)
            }
        }

    }

    private class CategoryScrollListener(
        var categories: List<Categories>,
        private val context: Context,
        private val layoutManager: RecyclerView.LayoutManager,
        private val onAverageUpdated: (width: Float, color: Int) -> Unit
    ) : RecyclerView.OnScrollListener() {

        private val itemHeight = context.dimen(R.dimen.category_picker_item_height).toFloat()

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            layoutManager.apply {
                val parentMidpoint = height / 2f

                val topChild = (0 until childCount)
                    .mapNotNull { i -> getChildAt(i) }
                    .filter { child -> child.midPoint() <= parentMidpoint }
                    .minBy { child -> parentMidpoint - child.midPoint() }
                    ?: return

                val bottomChild = (0 until childCount)
                    .mapNotNull { i -> getChildAt(i) }
                    .filter { child -> child.midPoint() >= parentMidpoint }
                    .minBy { child -> child.midPoint() - parentMidpoint }
                    ?: return

                val averageWidth =
                    topChild.width + (parentMidpoint - topChild.midPoint()) * (bottomChild.width - topChild.width) / itemHeight

                val topChildPos = getPosition(topChild)
                val topCategory = categories.getOrNull(topChildPos)

                val bottomChildPos = getPosition(bottomChild)
                val bottomCategory = categories.getOrNull(bottomChildPos)

                val averageColor = if (topCategory != null && bottomCategory != null) {
                    val topColor = ContextCompat.getColor(context, topCategory.colorRes)
                    val bottomColor = ContextCompat.getColor(context, bottomCategory.colorRes)
                    ColorUtils.mix(topColor, bottomColor, (parentMidpoint - topChild.midPoint()) / itemHeight)
                } else {
                    Color.TRANSPARENT
                }

                onAverageUpdated(averageWidth, averageColor)
            }
        }

        private fun View.midPoint() =
            (layoutManager.getDecoratedTop(this) + layoutManager.getDecoratedBottom(this)) / 2f

    }

    private class CategorySnapHelper(
        private val data: List<Categories>,
        private val onScrolledToPos: (category: Categories) -> Unit
    ) : LinearSnapHelper() {

        override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
            val view = super.findSnapView(layoutManager)

            if (view != null) {
                val pos = layoutManager?.getPosition(view)
                pos?.let(data::getOrNull)?.let(onScrolledToPos)
            }

            return view
        }

    }

}