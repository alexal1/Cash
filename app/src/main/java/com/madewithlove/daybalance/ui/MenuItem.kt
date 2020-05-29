/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.ui

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.view.isInvisible
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.setSelectableBackground
import com.madewithlove.daybalance.utils.string
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

fun <V: View> _ConstraintLayout.menuItem(
    titleText: CharSequence,
    subtitleText: CharSequence,
    customView: V?,
    isBottomSeparatorShown: Boolean
): MenuItem<V> {
    val background = view {
        id = View.generateViewId()

        setSelectableBackground()
    }.lparams(matchConstraint, matchConstraint)

    val title = textView {
        id = View.generateViewId()
        textColorResource = R.color.white
        textSize = 16f
        backgroundColor = Color.TRANSPARENT
        text = titleText
    }.lparams(matchConstraint, wrapContent) {
        topMargin = dip(12)
        leftMargin = dip(24)
        rightMargin = dip(24)
    }

    val subtitle = textView {
        id = View.generateViewId()
        textColorResource = R.color.smoke
        textSize = 12f
        backgroundColor = Color.TRANSPARENT
        text = subtitleText
    }.lparams(matchConstraint, wrapContent) {
        leftMargin = dip(24)
        rightMargin = dip(24)
    }

    val separator = view {
        id = View.generateViewId()
        backgroundColorResource = R.color.palladium_80
        isInvisible = !isBottomSeparatorShown
    }.lparams(matchConstraint, dip(1)) {
        topMargin = dip(12)
        leftMargin = dip(24)
    }

    return MenuItem(background, title, subtitle, separator, customView)
}

fun <V: View> _ConstraintLayout.menuItem(
    titleTextRes: Int,
    subtitleTextRes: Int,
    customView: V?,
    isBottomSeparatorShown: Boolean
): MenuItem<V> {
    return menuItem(
        string(titleTextRes),
        string(subtitleTextRes),
        customView,
        isBottomSeparatorShown
    )
}

fun ConstraintSetBuilder.connect(menuItem: MenuItem<*>, topView: View?) {
    menuItem.apply {
        connect(
            if (topView != null) {
                TOP of background to BOTTOM of topView
            } else {
                TOP of background to TOP of PARENT_ID
            },
            BOTTOM of background to BOTTOM of separator,
            START of background to START of PARENT_ID,
            END of background to END of PARENT_ID
        )

        connect(
            START of title to START of PARENT_ID,
            if (customView != null) {
                END of title to START of customView
            } else {
                END of title to END of PARENT_ID
            },
            if (topView != null) {
                TOP of title to BOTTOM of topView
            } else {
                TOP of title to TOP of PARENT_ID
            }
        )

        connect(
            START of subtitle to START of PARENT_ID,
            if (customView != null) {
                END of subtitle to START of customView
            } else {
                END of subtitle to END of PARENT_ID
            },
            TOP of subtitle to BOTTOM of title
        )

        if (customView != null) {
            connect(
                TOP of customView to TOP of title,
                BOTTOM of customView to BOTTOM of subtitle,
                END of customView to END of PARENT_ID
            )
        }

        connect(
            START of separator to START of PARENT_ID,
            END of separator to END of PARENT_ID,
            TOP of separator to BOTTOM of subtitle
        )
    }
}

class MenuItem<V: View>(
    val background: View,
    val title: TextView,
    val subtitle: TextView,
    val separator: View,
    val customView: V?
) {

    fun requireCustomView(): V {
        return customView ?: throw IllegalStateException("customView not set!")
    }

}