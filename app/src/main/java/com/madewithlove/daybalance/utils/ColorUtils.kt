package com.madewithlove.daybalance.utils

import android.graphics.Color



object ColorUtils {

    fun mix(color1: Int, color2: Int, ratio: Float): Int {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        val r = (r1 * (1f - ratio) + r2 * ratio).toInt()
        val g = (g1 * (1f - ratio) + g2 * ratio).toInt()
        val b = (b1 * (1f - ratio) + b2 * ratio).toInt()

        return (0xff shl 24) or (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff)
    }

}