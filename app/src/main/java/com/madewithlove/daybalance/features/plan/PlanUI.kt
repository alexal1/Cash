/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.plan

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko._Toolbar
import com.madewithlove.daybalance.utils.anko.appCompatToolbar
import com.madewithlove.daybalance.utils.anko.floatingActionButton
import com.madewithlove.daybalance.utils.anko.tabLayout
import com.madewithlove.daybalance.utils.color
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.support.v4.viewPager

class PlanUI : AnkoComponent<PlanFragment> {

    lateinit var toolbar: _Toolbar
    lateinit var titleText: TextView
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    lateinit var floatingActionButton: FloatingActionButton


    override fun createView(ui: AnkoContext<PlanFragment>): View = with (ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark
            isClickable = true
            isFocusable = true

            toolbar = appCompatToolbar {
                id = R.id.plan_toolbar
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.soft_dark

                titleText = textView {
                    id = R.id.create_title
                    textColorResource = R.color.white_80
                    textSize = 16f
                    letterSpacing = 0.02f
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchConstraint, dimen(R.dimen.toolbar_height))

            tabLayout = tabLayout {
                id = R.id.plan_tab_layout
                tabMode = TabLayout.MODE_SCROLLABLE
                backgroundColorResource = R.color.steel_gray

                setTabTextColors(color(R.color.fog_white), color(R.color.white))
            }.lparams(matchParent, dimen(R.dimen.plan_tab_layout_height))

            viewPager = viewPager {
                id = R.id.view_pager
            }.lparams(matchConstraint, matchConstraint)

            floatingActionButton = floatingActionButton {
                id = R.id.floating_action_button
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dimen(R.dimen.floating_action_button_margin)
                bottomMargin = dimen(R.dimen.floating_action_button_margin)
            }


            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of tabLayout to START of PARENT_ID,
                    END of tabLayout to END of PARENT_ID,
                    TOP of tabLayout to BOTTOM of toolbar
                )

                connect(
                    START of viewPager to START of PARENT_ID,
                    END of viewPager to END of PARENT_ID,
                    TOP of viewPager to BOTTOM of tabLayout,
                    BOTTOM of viewPager to BOTTOM of PARENT_ID
                )

                connect(
                    END of floatingActionButton to END of PARENT_ID,
                    BOTTOM of floatingActionButton to BOTTOM of PARENT_ID
                )
            }
        }
    }

}