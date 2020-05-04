/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.dates

import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.madewithlove.daybalance.helpers.Analytics
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.math.abs

class DatesSnapHelper : LinearSnapHelper(), KoinComponent {


    companion object {

        private const val VELOCITY_MIN = 200

    }


    private val analytics: Analytics by inject()

    var lastPos = NO_POSITION
    var isSwipeEnabled = false // swipes are disabled for too short touches


    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if (!isSwipeEnabled) return lastPos

        val currentView = findSnapView(layoutManager)
        return if (currentView != null) {
            val actualPos = layoutManager.getPosition(currentView)
            val targetPos = if (actualPos != lastPos) {
                actualPos
            } else {
                when {
                    abs(velocityX) <= VELOCITY_MIN -> lastPos
                    velocityX < 0 -> lastPos - 1
                    else -> lastPos + 1
                }
            }

            // Log analytics events
            if (targetPos > lastPos) {
                analytics.dateSwipeNext(isByButton = false)
            } else if (targetPos < lastPos) {
                analytics.dateSwipePrev(isByButton = false)
            }

            lastPos = targetPos
            lastPos
        } else {
            NO_POSITION
        }
    }

}