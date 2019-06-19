package com.alex_aladdin.cash.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.StringRes
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent

class DialogCheckBox(context: Context, text: String, isChecked: Boolean) : CheckBox(context) {

    constructor(context: Context) : this(context, "", false)

    constructor(context: Context, @StringRes textResId: Int) : this(context, context.getString(textResId), false)


    init {
        this.text = text
        this.isChecked = isChecked
        leftPadding = dip(4)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (layoutParams as ViewGroup.MarginLayoutParams).leftMargin = dip(16)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(matchParent, MeasureSpec.AT_MOST)
        val height = resolveSize(dip(64), MeasureSpec.UNSPECIFIED)
        setMeasuredDimension(width, height)
    }
}