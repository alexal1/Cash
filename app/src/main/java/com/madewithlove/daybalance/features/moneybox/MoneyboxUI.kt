/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.moneybox

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.CHAIN_PACKED
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.widget.TextViewCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko._Toolbar
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import com.madewithlove.daybalance.utils.anko.appCompatToolbar
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class MoneyboxUI : AnkoComponent<MoneyboxFragment> {

    lateinit var toolbar: _Toolbar
    lateinit var titleText: TextView
    lateinit var totalMoneyAmount: TextView
    lateinit var totalMoneyDescription: TextView
    lateinit var previousMoneyAmount: TextView
    lateinit var monthMoneyTitle: TextView
    lateinit var monthMoneyDescription: TextView
    lateinit var monthMoneyAmount: TextView


    override fun createView(ui: AnkoContext<MoneyboxFragment>) = with(ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark
            isClickable = true
            isFocusable = true

            toolbar = appCompatToolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.soft_dark

                titleText = textView {
                    id = View.generateViewId()
                    textColorResource = R.color.white_80
                    textSize = 16f
                    textResource = R.string.moneybox_title
                    letterSpacing = 0.02f
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchConstraint, dimen(R.dimen.toolbar_height))

            val totalMoneySpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, matchConstraint)

            val totalMoneyTitle = textView {
                id = View.generateViewId()
                textResource = R.string.moneybox_total_money_title
                textColorResource = R.color.white
                textSize = 14f
                letterSpacing = 0.08f
                allCaps = true
            }.lparams(matchConstraint, wrapContent) {
                marginStart = dip(16)
                marginEnd = dip(16)
                verticalChainStyle = CHAIN_PACKED
            }

            totalMoneyDescription = textView {
                id = View.generateViewId()
                textColorResource = R.color.fog_white
                textSize = 14f
                letterSpacing = 0.02f
            }.lparams(matchConstraint, wrapContent) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
                verticalChainStyle = CHAIN_PACKED
            }

            totalMoneyAmount = appCompatTextView {
                id = View.generateViewId()
                gravity = Gravity.CENTER
                letterSpacing = 0.02f
                textColorResource = R.color.white
                gravity = Gravity.START

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    128,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.moneybox_amount_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
                verticalChainStyle = CHAIN_PACKED
            }

            val previousMoneySpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, matchConstraint) {
                leftMargin = dip(32)
            }

            val previousMoneyTitle = textView {
                id = View.generateViewId()
                textResource = R.string.moneybox_previous_money_title
                textColorResource = R.color.white
                textSize = 14f
                letterSpacing = 0.08f
                allCaps = true
            }.lparams(matchConstraint, wrapContent) {
                marginStart = dip(16)
                marginEnd = dip(16)
                verticalChainStyle = CHAIN_PACKED
            }

            val previousMoneyDescription = textView {
                id = View.generateViewId()
                textResource = R.string.moneybox_previous_money_description
                textColorResource = R.color.fog_white
                textSize = 14f
                letterSpacing = 0.02f
            }.lparams(matchConstraint, wrapContent) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
                verticalChainStyle = CHAIN_PACKED
            }

            previousMoneyAmount = appCompatTextView {
                id = View.generateViewId()
                gravity = Gravity.CENTER
                letterSpacing = 0.02f
                textColorResource = R.color.white
                gravity = Gravity.START

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    128,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.moneybox_amount_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
                verticalChainStyle = CHAIN_PACKED
            }

            val monthMoneySpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, matchConstraint) {
                leftMargin = dip(32)
            }

            monthMoneyTitle = textView {
                id = View.generateViewId()
                textColorResource = R.color.white
                textSize = 14f
                letterSpacing = 0.08f
                allCaps = true
            }.lparams(matchConstraint, wrapContent) {
                marginStart = dip(16)
                marginEnd = dip(16)
                verticalChainStyle = CHAIN_PACKED
            }

            monthMoneyDescription = textView {
                id = View.generateViewId()
                textColorResource = R.color.fog_white
                textSize = 14f
                letterSpacing = 0.02f
            }.lparams(matchConstraint, wrapContent) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
                verticalChainStyle = CHAIN_PACKED
            }

            monthMoneyAmount = appCompatTextView {
                id = View.generateViewId()
                gravity = Gravity.CENTER
                letterSpacing = 0.02f
                textColorResource = R.color.white
                gravity = Gravity.START

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    128,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.moneybox_amount_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
                verticalChainStyle = CHAIN_PACKED
            }

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of totalMoneySpace to START of PARENT_ID,
                    END of totalMoneySpace to END of PARENT_ID,
                    TOP of totalMoneySpace to BOTTOM of toolbar,
                    BOTTOM of totalMoneySpace to TOP of previousMoneySpace
                )

                connect(
                    START of previousMoneySpace to START of PARENT_ID,
                    END of previousMoneySpace to END of PARENT_ID,
                    TOP of previousMoneySpace to BOTTOM of totalMoneySpace,
                    BOTTOM of previousMoneySpace to TOP of monthMoneySpace
                )

                connect(
                    START of monthMoneySpace to START of PARENT_ID,
                    END of monthMoneySpace to END of PARENT_ID,
                    TOP of monthMoneySpace to BOTTOM of previousMoneySpace,
                    BOTTOM of monthMoneySpace to BOTTOM of PARENT_ID
                )

                connect(
                    START of totalMoneyTitle to START of totalMoneySpace,
                    END of totalMoneyTitle to END of totalMoneySpace,
                    TOP of totalMoneyTitle to TOP of totalMoneySpace,
                    BOTTOM of totalMoneyTitle to TOP of totalMoneyDescription
                )
                
                connect(
                    START of totalMoneyDescription to START of totalMoneySpace,
                    END of totalMoneyDescription to END of totalMoneySpace,
                    TOP of totalMoneyDescription to BOTTOM of totalMoneyTitle,
                    BOTTOM of totalMoneyDescription to TOP of totalMoneyAmount
                )
                
                connect(
                    START of totalMoneyAmount to START of totalMoneySpace,
                    END of totalMoneyAmount to END of totalMoneySpace,
                    TOP of totalMoneyAmount to BOTTOM of totalMoneyDescription,
                    BOTTOM of totalMoneyAmount to BOTTOM of totalMoneySpace
                )

                connect(
                    START of previousMoneyTitle to START of previousMoneySpace,
                    END of previousMoneyTitle to END of previousMoneySpace,
                    TOP of previousMoneyTitle to TOP of previousMoneySpace,
                    BOTTOM of previousMoneyTitle to TOP of previousMoneyDescription
                )

                connect(
                    START of previousMoneyDescription to START of previousMoneySpace,
                    END of previousMoneyDescription to END of previousMoneySpace,
                    TOP of previousMoneyDescription to BOTTOM of previousMoneyTitle,
                    BOTTOM of previousMoneyDescription to TOP of previousMoneyAmount
                )

                connect(
                    START of previousMoneyAmount to START of previousMoneySpace,
                    END of previousMoneyAmount to END of previousMoneySpace,
                    TOP of previousMoneyAmount to BOTTOM of previousMoneyDescription,
                    BOTTOM of previousMoneyAmount to BOTTOM of previousMoneySpace
                )

                connect(
                    START of monthMoneyTitle to START of monthMoneySpace,
                    END of monthMoneyTitle to END of monthMoneySpace,
                    TOP of monthMoneyTitle to TOP of monthMoneySpace,
                    BOTTOM of monthMoneyTitle to TOP of monthMoneyDescription
                )

                connect(
                    START of monthMoneyDescription to START of monthMoneySpace,
                    END of monthMoneyDescription to END of monthMoneySpace,
                    TOP of monthMoneyDescription to BOTTOM of monthMoneyTitle,
                    BOTTOM of monthMoneyDescription to TOP of monthMoneyAmount
                )

                connect(
                    START of monthMoneyAmount to START of monthMoneySpace,
                    END of monthMoneyAmount to END of monthMoneySpace,
                    TOP of monthMoneyAmount to BOTTOM of monthMoneyDescription,
                    BOTTOM of monthMoneyAmount to BOTTOM of monthMoneySpace
                )
            }
        }
    }

}