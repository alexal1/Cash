/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.getRect
import com.madewithlove.daybalance.utils.statusBarHeight
import org.jetbrains.anko.*

class ShowcaseOverlayView(context: Context) : FrameLayout(context) {

    var holeRect: Rect? = null
    var onCrossClick: (() -> Unit)? = null

    private lateinit var crossView: ImageView
    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView


    fun setTitle(title: String) {
        titleView.text = title
    }

    fun setDescription(description: String) {
        descriptionView.text = description
    }


    init {
        linearLayout {
            orientation = VERTICAL

            crossView = imageView {
                scaleType = ImageView.ScaleType.CENTER

                setImageResource(R.drawable.ic_cross)
                setOnClickListener {
                    onCrossClick?.invoke()
                }
            }.lparams(dip(49), dip(49)) {
                topMargin = context.statusBarHeight()
            }

            titleView = textView {
                textColorResource = R.color.white_80
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
            }.lparams(matchParent, wrapContent) {
                topMargin = dip(32)
                leftMargin = dip(16)
                rightMargin = dip(16)
            }

            descriptionView = textView {
                textColorResource = R.color.white_80
                textSize = 16f

                setLineSpacing(0f, 1.1f)
            }.lparams(matchParent, wrapContent) {
                topMargin = dip(16)
                leftMargin = dip(16)
                rightMargin = dip(16)
            }
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (holeRect?.contains(ev.x.toInt(), ev.y.toInt()) == true
            || crossView.getRect().contains(ev.x.toInt(), ev.y.toInt())) {
            super.dispatchTouchEvent(ev)
        } else {
            true
        }
    }

}