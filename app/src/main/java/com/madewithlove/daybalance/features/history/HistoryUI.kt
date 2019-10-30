/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko.floatingActionButton
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.BOTTOM
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.END
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.wrapContent

class HistoryUI : AnkoComponent<HistoryFragment> {

    lateinit var floatingActionButton: FloatingActionButton


    override fun createView(ui: AnkoContext<HistoryFragment>): View = with (ui){
        constraintLayout {
            floatingActionButton = floatingActionButton {
                id = View.generateViewId()
                setImageResource(R.drawable.ic_double_arrow)
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dip(16)
                bottomMargin = dip(16)
            }


            applyConstraintSet {
                connect(
                    END of floatingActionButton to END of PARENT_ID,
                    BOTTOM of floatingActionButton to BOTTOM of PARENT_ID
                )
            }
        }
    }

}