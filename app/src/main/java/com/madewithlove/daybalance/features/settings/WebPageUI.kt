/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.graphics.Color
import android.view.View
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.anko._Toolbar
import com.madewithlove.daybalance.utils.anko.appCompatToolbar
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class WebPageUI : AnkoComponent<WebPageFragment> {

    lateinit var toolbar: _Toolbar
    lateinit var titleText: TextView
    lateinit var progressBar: ProgressBar
    lateinit var errorText: TextView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var webView: WebView


    override fun createView(ui: AnkoContext<WebPageFragment>): View = with(ui) {
        constraintLayout {
            backgroundColorResource = R.color.white
            isClickable = true
            isFocusable = true

            toolbar = appCompatToolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.soft_dark

                titleText = textView {
                    id = View.generateViewId()
                    textColorResource = R.color.white_80
                    textSize = 16f
                    letterSpacing = 0.02f
                }.lparams(wrapContent, wrapContent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            progressBar = progressBar {
                id = View.generateViewId()
            }.lparams(dimen(R.dimen.progress_bar_size), dimen(R.dimen.progress_bar_size))

            errorText = textView {
                id = View.generateViewId()
                textSize = 16f
                textColorResource = R.color.fog_white
                textResource = R.string.connection_error
                isVisible = false
            }.lparams(wrapContent, wrapContent)

            swipeRefreshLayout = swipeRefreshLayout {
                id = View.generateViewId()

                webView = webView {
                    id = R.id.web_view
                    backgroundColor = Color.TRANSPARENT
                    settings.cacheMode = LOAD_NO_CACHE
                }
            }.lparams(matchConstraint, matchConstraint)

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of progressBar to START of PARENT_ID,
                    END of progressBar to END of PARENT_ID,
                    TOP of progressBar to BOTTOM of toolbar,
                    BOTTOM of progressBar to BOTTOM of PARENT_ID
                )

                connect(
                    START of errorText to START of PARENT_ID,
                    END of errorText to END of PARENT_ID,
                    TOP of errorText to BOTTOM of toolbar,
                    BOTTOM of errorText to BOTTOM of PARENT_ID
                )

                connect(
                    START of swipeRefreshLayout to START of PARENT_ID,
                    END of swipeRefreshLayout to END of PARENT_ID,
                    TOP of swipeRefreshLayout to BOTTOM of toolbar,
                    BOTTOM of swipeRefreshLayout to BOTTOM of PARENT_ID
                )
            }
        }
    }

}