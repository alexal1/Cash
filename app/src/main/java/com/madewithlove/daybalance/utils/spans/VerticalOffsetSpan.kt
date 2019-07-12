package com.madewithlove.daybalance.utils.spans

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class VerticalOffsetSpan(private val ratio: Float) : MetricAffectingSpan() {

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.baselineShift -= (textPaint.ascent() / ratio).toInt()
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.baselineShift -= (textPaint.ascent() / ratio).toInt()
    }

}