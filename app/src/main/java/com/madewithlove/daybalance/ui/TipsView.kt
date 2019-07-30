package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.jakewharton.rxbinding3.view.clicks
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.TipsManager
import com.madewithlove.daybalance.utils.replace
import com.madewithlove.daybalance.utils.setSelectableBackground
import io.reactivex.Observable
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.matchConstraint

class TipsView(context: Context) : _ConstraintLayout(context) {

    val closeClick: Observable<TipsManager.Tip>

    private var currentTip: TipsManager.Tip? = null
    private var text: TextView


    init {
        val backgroundView = view {
            id = View.generateViewId()
            backgroundResource = R.drawable.bg_tip
        }.lparams(matchConstraint, matchConstraint)

        val icon = imageView {
            id = View.generateViewId()
            backgroundColor = Color.TRANSPARENT
            imageResource = R.drawable.ic_bulb
        }.lparams(wrapContent, wrapContent) {
            topMargin = dimen(R.dimen.tip_padding)
            bottomMargin = dimen(R.dimen.tip_padding)
            leftMargin = dimen(R.dimen.tip_padding)
        }

        text = textView {
            id = View.generateViewId()
            textSize = 12f
            textColorResource = R.color.white_80
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dimen(R.dimen.tip_padding)
            bottomMargin = dimen(R.dimen.tip_padding)
            leftMargin = dimen(R.dimen.tip_padding)
            rightMargin = dimen(R.dimen.tip_padding)
        }

        val separator = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.white_60
        }.lparams(dip(1), matchConstraint)

        val cross = imageButton {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_cross_mini)

            setSelectableBackground()
        }.lparams(dip(32) + dimen(R.dimen.tip_padding) * 2, matchConstraint).also {
            closeClick = it.clicks().map { currentTip }
        }

        applyConstraintSet {
            connect(
                START of backgroundView to START of PARENT_ID,
                END of backgroundView to END of PARENT_ID,
                TOP of backgroundView to TOP of PARENT_ID,
                BOTTOM of backgroundView to BOTTOM of PARENT_ID
            )

            connect(
                START of icon to START of PARENT_ID,
                TOP of icon to TOP of PARENT_ID,
                BOTTOM of icon to BOTTOM of PARENT_ID
            )

            connect(
                START of text to END of icon,
                END of text to START of separator,
                TOP of text to TOP of PARENT_ID,
                BOTTOM of text to BOTTOM of PARENT_ID
            )

            connect(
                END of separator to START of cross,
                TOP of separator to TOP of PARENT_ID,
                BOTTOM of separator to BOTTOM of PARENT_ID
            )

            connect(
                END of cross to END of PARENT_ID,
                TOP of cross to TOP of PARENT_ID,
                BOTTOM of cross to BOTTOM of PARENT_ID
            )
        }
    }


    fun setData(tip: TipsManager.Tip) {
        currentTip = tip

        when (tip) {
            is TipsManager.Tip.Empty -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_empty))
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
            }

            is TipsManager.Tip.SingleLossOnThisDayOneDayLong -> {}
            is TipsManager.Tip.SingleGainOnThisDayOneDayLong -> {}
            is TipsManager.Tip.SingleLossOnThisDayManyDaysLong -> {}
            is TipsManager.Tip.SingleGainOnThisDayManyDaysLong -> {}
        }
    }

}