/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout

class SwitchableMotionLayout(context: Context, attributeSet: AttributeSet) : MotionLayout(context, attributeSet) {

    var isSwitchedOff = false


    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return if (isSwitchedOff) false else super.onInterceptTouchEvent(event)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (!isSwitchedOff || !target.canScrollVertically(-1)) {
            super.onNestedPreScroll(target, dx, dy, consumed, type)
        }
    }

}