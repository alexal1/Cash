/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.utils

import android.view.animation.Interpolator

class ZeroInterpolator : Interpolator {

    override fun getInterpolation(input: Float): Float {
        return 1f
    }

}