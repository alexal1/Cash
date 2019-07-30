/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.MailTo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.madewithlove.daybalance.R
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import timber.log.Timber

class WebPageActivity : BaseActivity() {

    companion object {

        private const val URL_EXTRA = "url_extra"

        fun start(activity: Activity, url: String) {
            val intent = Intent(activity, WebPageActivity::class.java).apply {
                putExtra(URL_EXTRA, url)
            }

            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.slide_in_left,
                R.anim.slide_out_left
            ).toBundle()

            activity.startActivity(intent, options)
        }

    }


    private val webPageUrl: String by lazy { intent.getStringExtra(URL_EXTRA) }

    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var webView: WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        constraintLayout {
            val toolbar = toolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.deepDark

                setNavigationOnClickListener {
                    onBackPressed()
                }

                textView {
                    id = View.generateViewId()
                    textColorResource = R.color.white
                    textSize = 16f
                    backgroundColor = Color.TRANSPARENT
                    gravity = Gravity.CENTER_VERTICAL
                    includeFontPadding = false
                    textResource = R.string.privacy_policy
                }.lparams(wrapContent, matchParent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            progressBar = progressBar {
                id = View.generateViewId()
            }.lparams(
                dimen(R.dimen.day_transactions_progress_bar_size),
                dimen(R.dimen.day_transactions_progress_bar_size)
            )

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
                    webViewClient = MyWebViewClient()

                    loadUrl(webPageUrl)
                }

                setOnRefreshListener {
                    webView.isVisible = true
                    webView.loadUrl(webPageUrl)
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }


    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            when {
                url.startsWith("mailto:") -> try {
                    val mailTo = MailTo.parse(url).to
                    composeEmail(listOf(mailTo).toTypedArray(), "")
                } catch (e: Exception) {
                    Timber.e(e)
                }

                url != webPageUrl -> try {
                    openBrowser(url)
                } catch (e: Exception) {
                    Timber.e(e)
                }

                else -> view.loadUrl(url)
            }

            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            progressBar.isVisible = false
            swipeRefreshLayout.isRefreshing = false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Timber.e("error code = ${error.errorCode} ${error.description}")
            }

            view.isVisible = false
            errorText.isVisible = true
        }

        private fun composeEmail(addresses: Array<String>, subject: String) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, addresses)
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        private fun openBrowser(url: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

    }

}