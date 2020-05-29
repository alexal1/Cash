/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout.VERTICAL
import android.widget.Switch
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import com.madewithlove.daybalance.BuildConfig
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.MenuItem
import com.madewithlove.daybalance.ui.connect
import com.madewithlove.daybalance.ui.menuItem
import com.madewithlove.daybalance.utils.anko._Toolbar
import com.madewithlove.daybalance.utils.anko.appCompatToolbar
import com.madewithlove.daybalance.utils.setSelectableBackground
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class SettingsUI : AnkoComponent<SettingsFragment> {

    lateinit var toolbar: _Toolbar
    lateinit var logoBackground: View
    lateinit var pushMenuItem: MenuItem<Switch>
    lateinit var policyMenuItem: MenuItem<*>


    override fun createView(ui: AnkoContext<SettingsFragment>): View = with(ui) {
        linearLayout {
            orientation = VERTICAL
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
                    textResource = R.string.settings
                    letterSpacing = 0.02f
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            scrollView {
                id = R.id.settings_scroll_view
                isVerticalScrollBarEnabled = false

                constraintLayout {
                    logoBackground = view {
                        id = View.generateViewId()

                        setSelectableBackground()
                    }.lparams(matchConstraint, matchConstraint)

                    val logoIcon = imageView {
                        id = View.generateViewId()
                        scaleType = ImageView.ScaleType.CENTER

                        setImageResource(R.drawable.ic_launcher_foreground)
                    }.lparams(dip(80), dip(80)) {
                        leftMargin = dip(10)
                        topMargin = dip(8)
                    }

                    val logoText = textView {
                        id = View.generateViewId()
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        textColorResource = R.color.white
                        textSize = 14f
                        alpha = 0.8f

                        @SuppressLint("SetTextI18n")
                        text = "${ctx.getString(R.string.app_name)}  v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    }.lparams(matchConstraint, wrapContent)

                    val logoSeparator = view {
                        id = View.generateViewId()
                        backgroundColorResource = R.color.palladium_80
                    }.lparams(matchConstraint, dip(1)) {
                        topMargin = dip(8)
                    }

                    pushMenuItem = menuItem(
                        R.string.settings_push_notifications_title,
                        R.string.settings_push_notifications_subtitle,
                        switch {
                            id = View.generateViewId()
                            isClickable = false
                            isFocusable = false
                        }.lparams(wrapContent, wrapContent) {
                            rightMargin = dip(16)
                        },
                        true
                    )

                    policyMenuItem = menuItem(
                        R.string.settings_privacy_policy_title,
                        R.string.settings_privacy_policy_subtitle,
                        null,
                        false
                    )

                    applyConstraintSet {
                        connect(
                            START of logoBackground to START of PARENT_ID,
                            END of logoBackground to END of PARENT_ID,
                            TOP of logoBackground to TOP of PARENT_ID,
                            BOTTOM of logoBackground to BOTTOM of logoSeparator
                        )

                        connect(
                            START of logoIcon to START of PARENT_ID,
                            TOP of logoIcon to TOP of PARENT_ID
                        )

                        connect(
                            START of logoText to END of logoIcon,
                            TOP of logoText to TOP of logoIcon,
                            BOTTOM of logoText to BOTTOM of logoIcon
                        )

                        connect(
                            START of logoSeparator to START of PARENT_ID,
                            END of logoSeparator to END of PARENT_ID,
                            TOP of logoSeparator to BOTTOM of logoIcon
                        )

                        connect(pushMenuItem, logoSeparator)

                        connect(policyMenuItem, pushMenuItem.separator)
                    }
                }.lparams(matchParent, matchParent)
            }.lparams(matchParent, matchParent)
        }
    }

}