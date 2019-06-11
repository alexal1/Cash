package com.alex_aladdin.cash.ui.dates

import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import kotlin.math.abs

class DatesSnapHelper : LinearSnapHelper() {


    companion object {

        private const val VELOCITY_MIN = 200

    }


    var lastPos = -1


    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val currentView = findSnapView(layoutManager)
        return if (currentView != null) {
            val position = layoutManager.getPosition(currentView)
            return if (position != lastPos) {
                lastPos = position
                lastPos
            } else {
                when {
                    abs(velocityX) <= VELOCITY_MIN -> return lastPos
                    velocityX < 0 -> --lastPos
                    else -> ++lastPos
                }
            }
        } else {
            NO_POSITION
        }
    }

}