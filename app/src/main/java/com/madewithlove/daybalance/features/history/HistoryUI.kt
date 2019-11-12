/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.view.Gravity.CENTER_HORIZONTAL
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.TransactionsList
import com.madewithlove.daybalance.utils.anko.floatingActionButton
import com.madewithlove.daybalance.utils.anko.transactionsList
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class HistoryUI : AnkoComponent<HistoryFragment> {

    lateinit var transactionsList: TransactionsList
    lateinit var floatingActionButton: FloatingActionButton
    lateinit var emptyView: TextView
    lateinit var loadingView: TextView


    override fun createView(ui: AnkoContext<HistoryFragment>): View = with (ui){
        constraintLayout {
            backgroundColorResource = R.color.deepDark

            transactionsList = transactionsList {
                id = R.id.transactions_list
            }.lparams(matchConstraint, matchConstraint)

            floatingActionButton = floatingActionButton {
                id = R.id.floating_action_button
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dip(16)
                bottomMargin = dip(16)
            }

            emptyView = textView {
                id = R.id.empty_view
                textResource = R.string.history_empty
                textSize = 32f
                textColorResource = R.color.fog_white
                gravity = CENTER_HORIZONTAL
                letterSpacing = 0.02f
                isVisible = false

                setLineSpacing(0f, 1.2f)
            }.lparams(wrapContent, wrapContent)

            loadingView = textView {
                id = R.id.loading_view
                textResource = R.string.history_loading
                textSize = 16f
                textColorResource = R.color.fog_white
                letterSpacing = 0.02f
                isVisible = false
            }.lparams(wrapContent, wrapContent)


            applyConstraintSet {
                connect(
                    START of transactionsList to START of PARENT_ID,
                    END of transactionsList to END of PARENT_ID,
                    TOP of transactionsList to TOP of PARENT_ID,
                    BOTTOM of transactionsList to BOTTOM of PARENT_ID
                )

                connect(
                    END of floatingActionButton to END of PARENT_ID,
                    BOTTOM of floatingActionButton to BOTTOM of PARENT_ID
                )

                connect(
                    START of emptyView to START of PARENT_ID,
                    END of emptyView to END of PARENT_ID,
                    TOP of emptyView to TOP of PARENT_ID,
                    BOTTOM of emptyView to BOTTOM of PARENT_ID
                )

                connect(
                    START of loadingView to START of PARENT_ID,
                    END of loadingView to END of PARENT_ID,
                    TOP of loadingView to TOP of PARENT_ID,
                    BOTTOM of loadingView to BOTTOM of PARENT_ID
                )
            }
        }
    }

}