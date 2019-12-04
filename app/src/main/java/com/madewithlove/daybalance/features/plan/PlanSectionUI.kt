/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.plan

import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.View
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import org.jetbrains.anko.*

class PlanSectionUI : AnkoComponent<PlanSectionFragment> {

    lateinit var descriptionText: TextView
    lateinit var amountText: TextView
    lateinit var annotationText: TextView


    override fun createView(ui: AnkoContext<PlanSectionFragment>): View = with(ui) {
        linearLayout {
            orientation = VERTICAL

            descriptionText = textView {
                textSize = 14f
                textColorResource = R.color.white_80
            }.lparams(matchParent, dimen(R.dimen.plan_description_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(16)
            }

            amountText = appCompatTextView {
                gravity = CENTER

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    128,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchParent, dimen(R.dimen.plan_amount_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(8)
            }

            annotationText = appCompatTextView {
                gravity = CENTER
                textColorResource = R.color.blue_80

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    32,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchParent, dimen(R.dimen.plan_annotation_height)) {
                marginStart = dip(16)
                marginEnd = dip(16)
                topMargin = dip(16)
            }
        }
    }

}