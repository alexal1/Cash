/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.content.ContextCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.FancyButton
import com.madewithlove.daybalance.ui.dates.DatesRecyclerView
import com.madewithlove.daybalance.utils.anko.datesRecyclerView
import com.madewithlove.daybalance.utils.anko.fancyButton
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class MainUI : AnkoComponent<MainFragment> {

    lateinit var weekdayText: TextView
    lateinit var datesRecyclerView: DatesRecyclerView
    lateinit var gainButton: FancyButton
    lateinit var lossButton: FancyButton
    lateinit var largeButtonBackground: View
    lateinit var largeButtonText : TextView


    override fun createView(ui: AnkoContext<MainFragment>): View = with (ui) {
        constraintLayout {
            val datesSpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, dimen(R.dimen.date_height))

            weekdayText = textView {
                id = View.generateViewId()
                textSize = 14f
                textColorResource = R.color.white_80
                includeFontPadding = false
                letterSpacing = 0.02f
            }.lparams(wrapContent, wrapContent) {
                topMargin = dip(12)
            }

            datesRecyclerView = datesRecyclerView {
                id = R.id.dates_recycler_view
            }.lparams(matchConstraint, matchConstraint)

            gainButton = fancyButton {
                id = R.id.gain_button
                init(
                    ContextCompat.getColor(ctx, R.color.green),
                    ContextCompat.getColor(ctx, R.color.greenGradColor1),
                    ContextCompat.getColor(ctx, R.color.greenGradColor2)
                )
                setTextResource(R.string.gain)
            }.lparams(matchConstraint, dimen(R.dimen.fancy_button_height)) {
                bottomMargin = dip(8)
                leftMargin = dip(4)
            }

            lossButton = fancyButton {
                id = R.id.loss_button
                init(
                    ContextCompat.getColor(ctx, R.color.red),
                    ContextCompat.getColor(ctx, R.color.redGradColor1),
                    ContextCompat.getColor(ctx, R.color.redGradColor2)
                )
                setTextResource(R.string.loss)
            }.lparams(matchConstraint, dimen(R.dimen.fancy_button_height)) {
                bottomMargin = dip(8)
                rightMargin = dip(4)
            }

            largeButtonBackground = view {
                id = R.id.large_button_background
                backgroundResource = R.drawable.bg_large_button
            }.lparams(matchConstraint, dimen(R.dimen.large_button_height))

            largeButtonText = textView {
                id = R.id.large_button_text
                textColorResource = R.color.white_80
                textSize = 14f
                letterSpacing = 0.02f
                compoundDrawablePadding = dip(8)
            }.lparams(wrapContent, wrapContent)


            applyConstraintSet {
                connect(
                    START of weekdayText to START of PARENT_ID,
                    END of weekdayText to END of PARENT_ID,
                    TOP of weekdayText to TOP of PARENT_ID
                )

                connect(
                    START of datesRecyclerView to START of PARENT_ID,
                    END of datesRecyclerView to END of PARENT_ID,
                    TOP of datesRecyclerView to TOP of PARENT_ID,
                    BOTTOM of datesRecyclerView to BOTTOM of PARENT_ID
                )

                connect(
                    START of datesSpace to START of PARENT_ID,
                    END of datesSpace to END of PARENT_ID,
                    TOP of datesSpace to TOP of PARENT_ID
                )

                connect(
                    START of gainButton to START of PARENT_ID,
                    END of gainButton to START of lossButton,
                    BOTTOM of gainButton to TOP of largeButtonBackground
                )

                connect(
                    START of lossButton to END of gainButton,
                    END of lossButton to END of PARENT_ID,
                    BOTTOM of lossButton to TOP of largeButtonBackground
                )

                connect(
                    START of largeButtonBackground to START of PARENT_ID,
                    END of largeButtonBackground to END of PARENT_ID,
                    BOTTOM of largeButtonBackground to BOTTOM of PARENT_ID
                )

                connect(
                    START of largeButtonText to START of largeButtonBackground,
                    END of largeButtonText to END of largeButtonBackground,
                    TOP of largeButtonText to TOP of largeButtonBackground,
                    BOTTOM of largeButtonText to BOTTOM of largeButtonBackground
                )
            }
        }
    }

}