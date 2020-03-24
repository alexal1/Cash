/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.DisposableCache
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import timber.log.Timber

class WebPageFragment : Fragment() {

    companion object {

        private const val EXTRA_WEB_PAGE_URL = "extra_web_page_url"
        private const val EXTRA_TITLE = "extra_title"


        fun create(webPageUrl: String, title: String): WebPageFragment = WebPageFragment().apply {
            arguments = bundleOf(
                EXTRA_WEB_PAGE_URL to webPageUrl,
                EXTRA_TITLE to title
            )
        }

    }


    private val ui: WebPageUI get() = webPageUI ?: WebPageUI().also { webPageUI = it }
    private val webPageUrl by lazy { requireArguments().getString(EXTRA_WEB_PAGE_URL) }
    private val title by lazy { requireArguments().getString(EXTRA_TITLE) }
    private val dc = DisposableCache()

    private var webPageUI: WebPageUI? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.toolbar.apply {
            setNavigationOnClickListener {
                act.onBackPressed()
            }
        }

        ui.titleText.apply {
            text = title
        }

        ui.webView.apply {
            webViewClient = MyWebViewClient()

            postDelayed({
                loadUrl(webPageUrl)
            }, resources.getInteger(R.integer.activity_anim_duration_horizontal).toLong())
        }

        ui.swipeRefreshLayout.apply {
            setOnRefreshListener {
                ui.webView.isVisible = true
                ui.webView.loadUrl(webPageUrl)
            }
        }

        view.post {
            startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        dc.drain()
        ui.webView.webViewClient = null
        webPageUI = null
        super.onDestroyView()
    }


    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            when {
                url.startsWith("mailto:") -> try {
                    val mailTo = MailTo.parse(url).to
                    composeEmail(listOf(mailTo).toTypedArray())
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
            ui.progressBar.isVisible = false
            ui.swipeRefreshLayout.isRefreshing = false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Timber.e("error code = ${error.errorCode} ${error.description}")
            }

            view.isVisible = false
            ui.errorText.isVisible = true
        }

        private fun composeEmail(addresses: Array<String>) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, addresses)
            }

            if (intent.resolveActivity(ctx.packageManager) != null) {
                startActivity(intent)
            }
        }

        private fun openBrowser(url: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }

            if (intent.resolveActivity(ctx.packageManager) != null) {
                startActivity(intent)
            }
        }

    }

}