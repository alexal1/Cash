/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.MenuItem
import com.madewithlove.daybalance.ui.connect
import com.madewithlove.daybalance.ui.menuItem
import com.madewithlove.daybalance.utils.anko._Toolbar
import com.madewithlove.daybalance.utils.anko.appCompatToolbar
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout

class DebugSettingsUI : AnkoComponent<DebugSettingsFragment> {

    lateinit var toolbar: _Toolbar
    lateinit var showPushMenuItem: MenuItem<*>
    lateinit var enableLogsMenuItem: MenuItem<Switch>
    lateinit var repeatShowcaseMenuItem: MenuItem<*>


    override fun createView(ui: AnkoContext<DebugSettingsFragment>): View = with(ui) {
        linearLayout {
            orientation = LinearLayout.VERTICAL
            backgroundColorResource = R.color.deepDark
            isClickable = true
            isFocusable = true

            toolbar = appCompatToolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.soft_dark

                textView {
                    id = View.generateViewId()
                    textColorResource = R.color.white_80
                    textSize = 16f
                    textResource = R.string.debug_settings
                    letterSpacing = 0.02f
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            scrollView {
                id = R.id.settings_scroll_view
                isVerticalScrollBarEnabled = false
                topPadding = dip(12)
                bottomPadding = dip(12)

                constraintLayout {
                    showPushMenuItem = menuItem(
                        R.string.debug_settings_show_push_title,
                        R.string.debug_settings_show_push_subtitle,
                        null,
                        true
                    )

                    enableLogsMenuItem = menuItem(
                        R.string.debug_settings_enable_logs_title,
                        R.string.debug_settings_enable_logs_subtitle,
                        switch {
                            id = View.generateViewId()
                            isClickable = false
                            isFocusable = false
                        }.lparams(wrapContent, wrapContent) {
                            rightMargin = dip(16)
                        },
                        true
                    )

                    repeatShowcaseMenuItem = menuItem(
                        R.string.debug_settings_repeat_showcase_title,
                        R.string.debug_settings_repeat_showcase_subtitle,
                        null,
                        false
                    )

                    applyConstraintSet {
                        connect(showPushMenuItem, null)

                        connect(enableLogsMenuItem, showPushMenuItem.separator)

                        connect(repeatShowcaseMenuItem, enableLogsMenuItem.separator)
                    }
                }.lparams(matchParent, matchParent)
            }.lparams(matchParent, matchParent)
        }
    }

}