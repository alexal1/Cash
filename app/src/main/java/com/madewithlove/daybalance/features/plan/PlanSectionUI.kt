/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.plan

import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.CHAIN_PACKED
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.widget.TextViewCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class PlanSectionUI : AnkoComponent<PlanSectionFragment> {

    lateinit var descriptionText: TextView
    lateinit var amountText: TextView
    lateinit var annotationText: TextView


    override fun createView(ui: AnkoContext<PlanSectionFragment>): View = with(ui) {
        constraintLayout {
            descriptionText = appCompatTextView {
                id = View.generateViewId()
                gravity = CENTER_VERTICAL
                textSize = 14f
                textColorResource = R.color.white_80
                backgroundResource = R.drawable.bg_plan_section_description
                letterSpacing = 0.02f

                setPadding(dip(16), dip(2), dip(16), dip(2))

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    14,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.plan_description_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(16)
            }

            amountText = appCompatTextView {
                id = View.generateViewId()
                gravity = CENTER

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    128,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.plan_amount_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                verticalChainStyle = CHAIN_PACKED
            }

            annotationText = appCompatTextView {
                id = View.generateViewId()
                gravity = CENTER
                textColorResource = R.color.blue_80

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    32,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.plan_annotation_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(16)
                verticalChainStyle = CHAIN_PACKED
            }

            val floatingActionButtonSpace = space {
                id = View.generateViewId()
            }.lparams(dimen(R.dimen.floating_action_button_size), dimen(R.dimen.floating_action_button_size)) {
                rightMargin = dimen(R.dimen.floating_action_button_margin)
                bottomMargin = dimen(R.dimen.floating_action_button_margin)
            }


            applyConstraintSet {
                connect(
                    START of descriptionText to START of PARENT_ID,
                    END of descriptionText to END of PARENT_ID,
                    TOP of descriptionText to TOP of PARENT_ID
                )

                connect(
                    START of amountText to START of PARENT_ID,
                    END of amountText to END of PARENT_ID,
                    TOP of amountText to BOTTOM of descriptionText,
                    BOTTOM of amountText to TOP of annotationText
                )

                connect(
                    START of annotationText to START of PARENT_ID,
                    END of annotationText to END of PARENT_ID,
                    TOP of annotationText to BOTTOM of amountText,
                    BOTTOM of annotationText to TOP of floatingActionButtonSpace
                )

                connect(
                    END of floatingActionButtonSpace to END of PARENT_ID,
                    BOTTOM of floatingActionButtonSpace to BOTTOM of PARENT_ID
                )
            }
        }
    }

}