/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.getRect
import com.madewithlove.daybalance.utils.navigationBarHeight
import com.madewithlove.daybalance.utils.statusBarHeight
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.matchConstraint

class ShowcaseOverlayView(context: Context) : _ConstraintLayout(context) {

    var holeRect: Rect? = null
    var onCrossClick: (() -> Unit)? = null

    private val crossView: ImageView
    private val titleView: TextView
    private val descriptionView: TextView


    fun setTitle(title: String) {
        titleView.text = title
    }

    fun setDescription(description: String) {
        descriptionView.text = description
    }

    fun init(gravity: Int): ShowcaseOverlayView {
        applyConstraintSet {
            connect(
                START of crossView to START of PARENT_ID,
                TOP of crossView to TOP of PARENT_ID
            )

            connect(
                START of titleView to START of PARENT_ID,
                END of titleView to END of PARENT_ID
            )

            connect(
                START of descriptionView to START of PARENT_ID,
                END of descriptionView to END of PARENT_ID
            )

            when (gravity) {
                Gravity.TOP -> {
                    connect(
                        TOP of titleView to BOTTOM of crossView
                    )

                    connect(
                        TOP of descriptionView to BOTTOM of titleView
                    )
                }

                Gravity.BOTTOM -> {
                    connect(
                        BOTTOM of descriptionView to BOTTOM of PARENT_ID
                    )

                    connect(
                        BOTTOM of titleView to TOP of descriptionView
                    )
                }

                else -> throw IllegalArgumentException("Unexpected gravity value: $gravity")
            }
        }

        return this
    }


    init {
        crossView = imageView {
            id = View.generateViewId()
            scaleType = ImageView.ScaleType.CENTER

            setImageResource(R.drawable.ic_cross)
            setOnClickListener {
                onCrossClick?.invoke()
            }
        }.lparams(dip(49), dip(49)) {
            topMargin = context.statusBarHeight()
        }

        titleView = textView {
            id = View.generateViewId()
            textColorResource = R.color.white_80
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dip(32)
            bottomMargin = dip(16)
            leftMargin = dip(16)
            rightMargin = dip(16)
        }

        descriptionView = textView {
            id = View.generateViewId()
            textColorResource = R.color.white_80
            textSize = 16f

            setLineSpacing(0f, 1.1f)
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dip(16)
            bottomMargin = dip(32) + context.navigationBarHeight()
            leftMargin = dip(16)
            rightMargin = dip(16)
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