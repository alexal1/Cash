/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.dates

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DatesLayoutManager(context: Context) : LinearLayoutManager(context) {

    companion object {

        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 1.0f

        private const val MIN_ALPHA = 0.5f
        private const val MAX_ALPHA = 0.8f

        private const val MIN_DISTANCE = 0f
        private const val MAX_DISTANCE = 1.0f // of RecyclerView's half-width

    }

    init {
        orientation = HORIZONTAL
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val parentMidpoint = width / 2f
        val minDistance = MIN_DISTANCE * parentMidpoint
        val maxDistance = MAX_DISTANCE * parentMidpoint

        (0 until childCount).mapNotNull { i -> getChildAt(i) }.forEach { child ->
            val childMidpoint = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2f
            val distance = min(maxDistance, max(minDistance, abs(parentMidpoint - childMidpoint)))

            val scale = MAX_SCALE + (MIN_SCALE - MAX_SCALE) * (distance - minDistance) / (maxDistance - minDistance)
            child.scaleX = scale
            child.scaleY = scale

            val alpha = MAX_ALPHA + (MIN_ALPHA - MAX_ALPHA) * (distance - minDistance) / (maxDistance - minDistance)
            child.alpha = alpha
        }

        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        scrollHorizontallyBy(0, recycler, state)
    }

}