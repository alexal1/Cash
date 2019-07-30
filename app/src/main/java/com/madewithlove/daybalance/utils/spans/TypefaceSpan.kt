/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = typeface
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }

}