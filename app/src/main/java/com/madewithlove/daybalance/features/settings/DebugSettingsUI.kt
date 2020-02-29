/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.view.isInvisible
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko._Toolbar
import com.madewithlove.daybalance.utils.anko.appCompatToolbar
import com.madewithlove.daybalance.utils.setSelectableBackground
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class DebugSettingsUI : AnkoComponent<DebugSettingsFragment> {

    lateinit var toolbar: _Toolbar
    lateinit var showPushBackground: View
    lateinit var enableLogsBackground: View
    lateinit var enableLogsSwitch: Switch
    lateinit var repeatShowcaseBackground: View


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
                    showPushBackground = view {
                        id = View.generateViewId()

                        setSelectableBackground()
                    }.lparams(matchConstraint, matchConstraint)

                    val showPushTitle = textView {
                        id = View.generateViewId()
                        textColorResource = R.color.white
                        textSize = 16f
                        backgroundColor = Color.TRANSPARENT
                        textResource = R.string.debug_settings_show_push_title
                    }.lparams(matchConstraint, wrapContent) {
                        topMargin = dip(12)
                        leftMargin = dip(24)
                        rightMargin = dip(24)
                    }

                    val showPushSubtitle = textView {
                        id = View.generateViewId()
                        textColorResource = R.color.smoke
                        textSize = 12f
                        backgroundColor = Color.TRANSPARENT
                        textResource = R.string.debug_settings_show_push_subtitle
                    }.lparams(matchConstraint, wrapContent) {
                        leftMargin = dip(24)
                        rightMargin = dip(24)
                    }

                    val showPushSeparator = view {
                        id = View.generateViewId()
                        backgroundColorResource = R.color.palladium_80
                    }.lparams(matchConstraint, dip(1)) {
                        topMargin = dip(12)
                        leftMargin = dip(24)
                    }

                    enableLogsBackground = view {
                        id = View.generateViewId()

                        setSelectableBackground()
                    }.lparams(matchConstraint, matchConstraint)

                    val enableLogsTitle = textView {
                        id = View.generateViewId()
                        textColorResource = R.color.white
                        textSize = 16f
                        backgroundColor = Color.TRANSPARENT
                        textResource = R.string.debug_settings_enable_logs_title
                    }.lparams(matchConstraint, wrapContent) {
                        topMargin = dip(12)
                        leftMargin = dip(24)
                        rightMargin = dip(24)
                    }

                    val enableLogsSubtitle = textView {
                        id = View.generateViewId()
                        textColorResource = R.color.smoke
                        textSize = 12f
                        backgroundColor = Color.TRANSPARENT
                        textResource = R.string.debug_settings_enable_logs_subtitle
                    }.lparams(matchConstraint, wrapContent) {
                        leftMargin = dip(24)
                        rightMargin = dip(24)
                    }

                    enableLogsSwitch = switch {
                        id = View.generateViewId()
                        isClickable = false
                        isFocusable = false
                    }.lparams(wrapContent, wrapContent) {
                        rightMargin = dip(16)
                    }

                    val enableLogsSeparator = view {
                        id = View.generateViewId()
                        backgroundColorResource = R.color.palladium_80
                    }.lparams(matchConstraint, dip(1)) {
                        topMargin = dip(12)
                        leftMargin = dip(24)
                    }

                    repeatShowcaseBackground = view {
                        id = View.generateViewId()

                        setSelectableBackground()
                    }.lparams(matchConstraint, matchConstraint)

                    val repeatShowcaseTitle = textView {
                        id = View.generateViewId()
                        textColorResource = R.color.white
                        textSize = 16f
                        backgroundColor = Color.TRANSPARENT
                        textResource = R.string.debug_settings_repeat_showcase_title
                    }.lparams(matchConstraint, wrapContent) {
                        topMargin = dip(12)
                        leftMargin = dip(24)
                        rightMargin = dip(24)
                    }

                    val repeatShowcaseSubtitle = textView {
                        id = View.generateViewId()
                        textColorResource = R.color.smoke
                        textSize = 12f
                        backgroundColor = Color.TRANSPARENT
                        textResource = R.string.debug_settings_repeat_showcase_subtitle
                    }.lparams(matchConstraint, wrapContent) {
                        leftMargin = dip(24)
                        rightMargin = dip(24)
                    }

                    val repeatShowcaseSeparator = view {
                        id = View.generateViewId()
                        backgroundColorResource = R.color.palladium_80
                        isInvisible = true
                    }.lparams(matchConstraint, dip(1)) {
                        topMargin = dip(12)
                        leftMargin = dip(24)
                    }

                    applyConstraintSet {
                        connect(
                            START of showPushTitle to START of PARENT_ID,
                            END of showPushTitle to END of PARENT_ID,
                            TOP of showPushTitle to TOP of PARENT_ID
                        )

                        connect(
                            START of showPushSubtitle to START of PARENT_ID,
                            END of showPushSubtitle to END of PARENT_ID,
                            TOP of showPushSubtitle to BOTTOM of showPushTitle
                        )

                        connect(
                            START of showPushSeparator to START of PARENT_ID,
                            END of showPushSeparator to END of PARENT_ID,
                            TOP of showPushSeparator to BOTTOM of showPushSubtitle
                        )

                        connect(
                            START of showPushBackground to START of PARENT_ID,
                            END of showPushBackground to END of PARENT_ID,
                            TOP of showPushBackground to TOP of PARENT_ID,
                            BOTTOM of showPushBackground to BOTTOM of showPushSeparator
                        )

                        connect(
                            START of enableLogsTitle to START of PARENT_ID,
                            END of enableLogsTitle to START of enableLogsSwitch,
                            TOP of enableLogsTitle to BOTTOM of showPushSeparator
                        )

                        connect(
                            START of enableLogsSubtitle to START of PARENT_ID,
                            END of enableLogsSubtitle to START of enableLogsSwitch,
                            TOP of enableLogsSubtitle to BOTTOM of enableLogsTitle
                        )

                        connect(
                            START of enableLogsSeparator to START of PARENT_ID,
                            END of enableLogsSeparator to END of PARENT_ID,
                            TOP of enableLogsSeparator to BOTTOM of enableLogsSubtitle
                        )

                        connect(
                            TOP of enableLogsSwitch to TOP of enableLogsTitle,
                            BOTTOM of enableLogsSwitch to BOTTOM of enableLogsSubtitle,
                            END of enableLogsSwitch to END of PARENT_ID
                        )

                        connect(
                            START of enableLogsBackground to START of PARENT_ID,
                            END of enableLogsBackground to END of PARENT_ID,
                            TOP of enableLogsBackground to BOTTOM of showPushSeparator,
                            BOTTOM of enableLogsBackground to BOTTOM of enableLogsSeparator
                        )

                        connect(
                            START of repeatShowcaseTitle to START of PARENT_ID,
                            END of repeatShowcaseTitle to END of PARENT_ID,
                            TOP of repeatShowcaseTitle to BOTTOM of enableLogsSeparator
                        )

                        connect(
                            START of repeatShowcaseSubtitle to START of PARENT_ID,
                            END of repeatShowcaseSubtitle to END of PARENT_ID,
                            TOP of repeatShowcaseSubtitle to BOTTOM of repeatShowcaseTitle
                        )

                        connect(
                            START of repeatShowcaseSeparator to START of PARENT_ID,
                            END of repeatShowcaseSeparator to END of PARENT_ID,
                            TOP of repeatShowcaseSeparator to BOTTOM of repeatShowcaseSubtitle
                        )

                        connect(
                            START of repeatShowcaseBackground to START of PARENT_ID,
                            END of repeatShowcaseBackground to END of PARENT_ID,
                            TOP of repeatShowcaseBackground to BOTTOM of enableLogsSeparator,
                            BOTTOM of repeatShowcaseBackground to BOTTOM of repeatShowcaseSeparator
                        )
                    }
                }.lparams(matchParent, matchParent)
            }.lparams(matchParent, matchParent)
        }
    }

}