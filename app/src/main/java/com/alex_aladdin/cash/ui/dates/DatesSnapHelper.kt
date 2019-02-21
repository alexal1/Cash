package com.alex_aladdin.cash.ui.dates

import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_POSITION

class DatesSnapHelper : LinearSnapHelper() {

    private var lastPos = -1

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
                if (velocityX < 0) {
                    --lastPos
                } else {
                    ++lastPos
                }
            }
        } else {
            NO_POSITION
        }
    }

}