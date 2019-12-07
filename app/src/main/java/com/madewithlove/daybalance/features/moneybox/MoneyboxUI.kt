/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.moneybox

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko._Toolbar
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


    override fun createView(ui: AnkoContext<MoneyboxFragment>) = with(ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark
            isClickable = true
            isFocusable = true

            toolbar = appCompatToolbar {
                id = R.id.moneybox_toolbar
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.soft_dark

                titleText = textView {
                    id = R.id.moneybox_title
                    textColorResource = R.color.white_80
                    textSize = 16f
                    textResource = R.string.moneybox_title
                    letterSpacing = 0.02f
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchConstraint, dimen(R.dimen.toolbar_height))

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )
            }
        }
    }

}