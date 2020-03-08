/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.graphics.Color
import android.view.Gravity
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.content.ContextCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.FancyButton
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.ui.dates.DatesRecyclerView
import com.madewithlove.daybalance.utils.anko.circleView
import com.madewithlove.daybalance.utils.anko.datesRecyclerView
import com.madewithlove.daybalance.utils.anko.fancyButton
import com.madewithlove.daybalance.utils.setSelectableBackground
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.support.v4.space

class MainUI : AnkoComponent<MainFragment> {

    lateinit var weekdayText: TextView
    lateinit var datesRecyclerView: DatesRecyclerView
    lateinit var settingsButton: TextView
    lateinit var planButton: TextView
    lateinit var moneyboxButton: TextView
    lateinit var container: FrameLayout
    lateinit var circleView: CircleView
    lateinit var nextButton: ImageView
    lateinit var prevButton: ImageView
    lateinit var gainButton: FancyButton
    lateinit var lossButton: FancyButton
    lateinit var largeButtonBackground: View
    lateinit var largeButtonText : TextView


    override fun createView(ui: AnkoContext<MainFragment>): View = with (ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark

            val separator1 = view {
                id = View.generateViewId()
                alpha = 0.5f
                backgroundColorResource = R.color.fog_white
            }.lparams(dip(2), dimen(R.dimen.top_buttons_height))

            val separator2 = view {
                id = View.generateViewId()
                alpha = 0.5f
                backgroundColorResource = R.color.fog_white
            }.lparams(dip(2), dimen(R.dimen.top_buttons_height))

            settingsButton = textView {
                id = View.generateViewId()
                textResource = R.string.top_buttons_settings
                textSize = 10f
                textColorResource = R.color.white
                allCaps = true
                letterSpacing = 0.08f
                gravity = Gravity.CENTER
                topPadding = dip(12)

                setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_settings_white, 0, 0)
                setSelectableBackground()
            }.lparams(matchConstraint, dimen(R.dimen.top_buttons_height))

            planButton = textView {
                id = R.id.top_button_plan
                textResource = R.string.top_buttons_plan
                textSize = 10f
                textColorResource = R.color.white
                allCaps = true
                letterSpacing = 0.08f
                gravity = Gravity.CENTER
                topPadding = dip(15)
                compoundDrawablePadding = dip(2)

                setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_plan, 0, 0)
                setSelectableBackground()
            }.lparams(matchConstraint, dimen(R.dimen.top_buttons_height))

            moneyboxButton = textView {
                id = View.generateViewId()
                textResource = R.string.top_buttons_moneybox
                textSize = 10f
                textColorResource = R.color.white
                allCaps = true
                letterSpacing = 0.08f
                gravity = Gravity.CENTER
                topPadding = dip(12)
                compoundDrawablePadding = dip(2)

                setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_moneybox, 0, 0)
                setSelectableBackground()
            }.lparams(matchConstraint, dimen(R.dimen.top_buttons_height))

            val datesSpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, dimen(R.dimen.date_height))

            weekdayText = textView {
                id = View.generateViewId()
                textSize = 14f
                textColorResource = R.color.white_80
                includeFontPadding = false
                letterSpacing = 0.02f
                gravity = CENTER_HORIZONTAL
            }.lparams(matchConstraint, wrapContent)

            datesRecyclerView = datesRecyclerView {
                id = R.id.dates_recycler_view
            }.lparams(matchConstraint, matchConstraint)

            circleView = circleView {
                id = R.id.circle_view
            }.lparams(matchConstraint, matchConstraint) {
                leftMargin = dimen(R.dimen.side_button_width)
                rightMargin = dimen(R.dimen.side_button_width)
                topMargin = dip(16)
                bottomMargin = dip(16)
            }

            prevButton = imageView {
                id = R.id.prev_button
                backgroundColor = Color.TRANSPARENT
                scaleType = ImageView.ScaleType.CENTER
                isClickable = false
                isFocusable = false
                alpha = 0.8f

                setImageResource(R.drawable.ic_back)
            }.lparams(dimen(R.dimen.side_button_width), matchConstraint)

            nextButton = imageView {
                id = R.id.next_button
                backgroundColor = Color.TRANSPARENT
                scaleType = ImageView.ScaleType.CENTER
                isClickable = false
                isFocusable = false
                alpha = 0.8f

                setImageResource(R.drawable.ic_next)
            }.lparams(dimen(R.dimen.side_button_width), matchConstraint)

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

            container = frameLayout {
                id = R.id.main_container
            }.lparams(matchConstraint, matchConstraint)

            val largeButtonSpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, dimen(R.dimen.large_button_height))

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
                gravity = CENTER_VERTICAL
            }.lparams(wrapContent, matchConstraint)


            applyConstraintSet {
                connect(
                    START of settingsButton to START of PARENT_ID,
                    END of settingsButton to START of separator1,
                    TOP of settingsButton to TOP of PARENT_ID,
                    BOTTOM of settingsButton to BOTTOM of separator1
                )

                connect(
                    START of planButton to END of separator1,
                    END of planButton to START of separator2,
                    TOP of planButton to TOP of PARENT_ID,
                    BOTTOM of planButton to BOTTOM of separator1
                )

                connect(
                    START of moneyboxButton to END of separator2,
                    END of moneyboxButton to END of PARENT_ID,
                    TOP of moneyboxButton to TOP of PARENT_ID,
                    BOTTOM of moneyboxButton to BOTTOM of separator2
                )

                connect(
                    START of separator1 to START of PARENT_ID,
                    END of separator1 to START of separator2,
                    TOP of separator1 to TOP of PARENT_ID
                )

                connect(
                    START of separator2 to END of separator1,
                    END of separator2 to END of PARENT_ID,
                    TOP of separator2 to TOP of PARENT_ID
                )

                connect(
                    START of weekdayText to START of PARENT_ID,
                    END of weekdayText to END of PARENT_ID,
                    BOTTOM of weekdayText to BOTTOM of datesSpace
                )

                connect(
                    START of datesRecyclerView to START of PARENT_ID,
                    END of datesRecyclerView to END of PARENT_ID,
                    TOP of datesRecyclerView to TOP of datesSpace,
                    BOTTOM of datesRecyclerView to BOTTOM of PARENT_ID
                )

                connect(
                    START of datesSpace to START of PARENT_ID,
                    END of datesSpace to END of PARENT_ID,
                    TOP of datesSpace to BOTTOM of separator1
                )

                connect(
                    START of circleView to START of PARENT_ID,
                    END of circleView to END of PARENT_ID,
                    TOP of circleView to BOTTOM of weekdayText,
                    BOTTOM of circleView to TOP of gainButton
                )

                connect(
                    START of prevButton to START of PARENT_ID,
                    TOP of prevButton to BOTTOM of datesSpace,
                    BOTTOM of prevButton to TOP of gainButton
                )

                connect(
                    END of nextButton to END of PARENT_ID,
                    TOP of nextButton to BOTTOM of datesSpace,
                    BOTTOM of nextButton to TOP of gainButton
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
                    START of container to START of PARENT_ID,
                    END of container to END of PARENT_ID,
                    TOP of container to TOP of PARENT_ID,
                    BOTTOM of container to TOP of largeButtonBackground
                )

                connect(
                    START of largeButtonSpace to START of PARENT_ID,
                    END of largeButtonSpace to END of PARENT_ID,
                    BOTTOM of largeButtonSpace to BOTTOM of PARENT_ID
                )

                connect(
                    START of largeButtonBackground to START of PARENT_ID,
                    END of largeButtonBackground to END of PARENT_ID,
                    TOP of largeButtonBackground to TOP of largeButtonSpace
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