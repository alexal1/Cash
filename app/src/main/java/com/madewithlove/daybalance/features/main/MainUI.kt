/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.core.graphics.contains
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.FancyButton
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.ui.dates.DatesRecyclerView
import com.madewithlove.daybalance.utils.anko.circleView
import com.madewithlove.daybalance.utils.anko.datesRecyclerView
import com.madewithlove.daybalance.utils.anko.fancyButton
import com.madewithlove.daybalance.utils.getRect
import com.madewithlove.daybalance.utils.screenSize
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class MainUI : AnkoComponent<MainFragment> {

    lateinit var weekdayText: TextView
    lateinit var datesRecyclerView: DatesRecyclerView
    lateinit var container: FrameLayout
    lateinit var circleView: CircleView
    lateinit var nextButton: ImageView
    lateinit var prevButton: ImageView
    lateinit var monthPlanButton: View
    lateinit var gainButton: FancyButton
    lateinit var lossButton: FancyButton
    lateinit var largeButtonBackground: View
    lateinit var largeButtonText : TextView


    override fun createView(ui: AnkoContext<MainFragment>): View = with (ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark

            val datesSpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, dimen(R.dimen.date_height_visual))

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

            val monthPlanLabel = textView {
                id = View.generateViewId()
                textSize = 14f
                textColorResource = R.color.blue
                textResource = R.string.month_plan
                letterSpacing = 0.02f
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                gravity = CENTER_HORIZONTAL
                alpha = 0.8f
            }.lparams(ctx.screenSize().x / 2, wrapContent) {
                bottomMargin = dip(32)
            }

            monthPlanButton = view {
                id = R.id.month_plan_button

                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            monthPlanLabel.alpha = 1.0f
                            true
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            monthPlanLabel.alpha = 0.8f

                            if (getRect().contains(Point(event.x.toInt() + left, event.y.toInt() + top))) {
                                performClick()
                            }

                            true
                        }

                        else -> false
                    }
                }
            }.lparams(matchConstraint, dip(96))

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
                    START of circleView to START of PARENT_ID,
                    END of circleView to END of PARENT_ID,
                    TOP of circleView to BOTTOM of datesSpace,
                    BOTTOM of circleView to TOP of monthPlanLabel
                )

                connect(
                    START of monthPlanLabel to START of PARENT_ID,
                    END of monthPlanLabel to END of PARENT_ID,
                    BOTTOM of monthPlanLabel to TOP of gainButton
                )

                connect(
                    START of monthPlanButton to START of monthPlanLabel,
                    END of monthPlanButton to END of monthPlanLabel,
                    TOP of monthPlanButton to TOP of monthPlanLabel,
                    BOTTOM of monthPlanButton to BOTTOM of monthPlanLabel
                )

                connect(
                    START of prevButton to START of PARENT_ID,
                    TOP of prevButton to BOTTOM of datesSpace,
                    BOTTOM of prevButton to TOP of monthPlanLabel
                )

                connect(
                    END of nextButton to END of PARENT_ID,
                    TOP of nextButton to BOTTOM of datesSpace,
                    BOTTOM of nextButton to TOP of monthPlanLabel
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