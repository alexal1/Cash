package com.alex_aladdin.cash.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import com.alex_aladdin.cash.BuildConfig
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.SettingsViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    companion object {

        private const val DIALOG_CHECK_ANIMATION_DURATION = 400L

        fun start(activity: Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.slide_in_up,
                R.anim.slide_out_up
            ).toBundle()

            activity.startActivity(intent, options)
        }

    }


    private val viewModel: SettingsViewModel by viewModel()
    private val currencyManager: CurrencyManager by inject()
    private val dc = DisposableCache()

    private var currenciesDialog: AlertDialog? = null
    private var autoSwitchCurrencyDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        constraintLayout {
            val toolbar = toolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_cross
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
                    textResource = R.string.settings
                }.lparams(wrapContent, matchParent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            val scrollView = scrollView {
                id = R.id.settings_scroll_view
                isVerticalScrollBarEnabled = false
                topPadding = dip(12)
                bottomPadding = dip(12)

                getScrollContent().lparams(matchParent, matchParent)
            }.lparams(matchConstraint, matchConstraint)

            val versionText = textView {
                id = View.generateViewId()
                backgroundColorResource = R.color.soft_dark
                textColorResource = R.color.palladium
                textSize = 12f
                gravity = CENTER

                @SuppressLint("SetTextI18n")
                text = "${getString(R.string.app_name)}  v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            }.lparams(matchConstraint, dip(32))

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of scrollView to START of PARENT_ID,
                    END of scrollView to END of PARENT_ID,
                    TOP of scrollView to BOTTOM of toolbar,
                    BOTTOM of scrollView to TOP of versionText
                )

                connect(
                    START of versionText to START of PARENT_ID,
                    END of versionText to END of PARENT_ID,
                    BOTTOM of versionText to BOTTOM of PARENT_ID
                )
            }
        }
    }

    private fun _ScrollView.getScrollContent(): ViewGroup = constraintLayout {
        val currencyItem = getSettingsItem(
            R.string.settings_currency_title,
            R.string.settings_currency_subtitle,
            true,
            controlViewInflater = {
                textView {
                    id = View.generateViewId()
                    textColorResource = R.color.blue
                    textSize = 16f
                    backgroundColor = Color.TRANSPARENT

                    viewModel.currencyObservable.subscribeOnUi {
                        text = it
                    }.cache(dc)
                }.lparams(wrapContent, wrapContent) {
                    rightMargin = dip(24)
                }
            },
            onClickListener = {
                showCurrenciesDialog()
            }
        )

        val autoSwitchItem = getSettingsItem(
            R.string.settings_auto_switch_title,
            R.string.settings_auto_switch_subtitle,
            true,
            controlViewInflater = {
                textView {
                    id = View.generateViewId()
                    textColorResource = R.color.blue
                    textSize = 14f
                    backgroundColor = Color.TRANSPARENT

                    viewModel.autoSwitchCurrencyObservable.subscribeOnUi {
                        text = it
                    }.cache(dc)
                }.lparams(wrapContent, wrapContent) {
                    rightMargin = dip(24)
                }
            },
            onClickListener = {
                showAutoSwitchCurrencyDialog()
            }
        )

        applyConstraintSet {
            connect(
                START of currencyItem to START of PARENT_ID,
                END of currencyItem to END of PARENT_ID,
                TOP of currencyItem to TOP of PARENT_ID
            )

            connect(
                START of autoSwitchItem to START of PARENT_ID,
                END of autoSwitchItem to END of PARENT_ID,
                TOP of autoSwitchItem to BOTTOM of currencyItem
            )
        }
    }

    private inline fun _ConstraintLayout.getSettingsItem(
        @StringRes titleRes: Int,
        @StringRes subtitleRes: Int,
        showSeparator: Boolean,
        controlViewInflater: (_ConstraintLayout) -> View,
        crossinline onClickListener: () -> Unit
    ): View {
        val backgroundView = view {
            id = View.generateViewId()

            setSelectableBackground()
            setOnClickListenerWithThrottle {
                onClickListener()
            }.cache(dc)
        }.lparams(matchConstraint, matchConstraint)

        val title = textView {
            id = View.generateViewId()
            textColorResource = R.color.white
            textSize = 16f
            backgroundColor = Color.TRANSPARENT
            textResource = titleRes
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dip(12)
            leftMargin = dip(24)
            rightMargin = dip(24)
        }

        val subtitle = textView {
            id = View.generateViewId()
            textColorResource = R.color.smoke
            textSize = 12f
            backgroundColor = Color.TRANSPARENT
            textResource = subtitleRes
        }.lparams(matchConstraint, wrapContent) {
            leftMargin = dip(24)
            rightMargin = dip(24)
        }

        val controlView = controlViewInflater(this)

        val separator = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.palladium_80
            isVisible = showSeparator
        }.lparams(matchConstraint, dip(1)) {
            topMargin = dip(12)
            leftMargin = dip(24)
        }

        applyConstraintSet {
            connect(
                START of title to START of backgroundView,
                END of title to START of controlView,
                TOP of title to TOP of backgroundView
            )

            connect(
                START of subtitle to START of backgroundView,
                END of subtitle to START of controlView,
                TOP of subtitle to BOTTOM of title
            )

            connect(
                TOP of controlView to TOP of title,
                BOTTOM of controlView to BOTTOM of subtitle,
                END of controlView to END of backgroundView
            )

            connect(
                START of separator to START of backgroundView,
                END of separator to END of backgroundView,
                TOP of separator to BOTTOM of subtitle
            )

            connect(
                BOTTOM of backgroundView to BOTTOM of separator
            )
        }

        return backgroundView
    }

    private fun showCurrenciesDialog() {
        currenciesDialog?.dismiss()
        currenciesDialog = AlertDialog.Builder(this@SettingsActivity)
            .setTitle(R.string.settings_currency_title)
            .setSingleChoiceItems(
                currencyManager.getCurrenciesList().toTypedArray(),
                currencyManager.getCurrentCurrencyIndex()
            ) { dialog, index ->
                currencyManager.setCurrentCurrencyIndex(index)
                viewModel.notifyCurrencyWasChanged()

                Handler().postDelayed({
                    dialog.dismiss()
                }, DIALOG_CHECK_ANIMATION_DURATION)
            }
            .show()
    }

    private fun showAutoSwitchCurrencyDialog() {
        autoSwitchCurrencyDialog?.dismiss()
        autoSwitchCurrencyDialog = AlertDialog.Builder(this@SettingsActivity)
            .setTitle(R.string.settings_auto_switch_title)
            .setSingleChoiceItems(
                arrayOf(getString(R.string.yes), getString(R.string.no), getString(R.string.ask)),
                viewModel.autoSwitchCurrency
            ) { dialog, index ->
                viewModel.autoSwitchCurrency = index

                Handler().postDelayed({
                    dialog.dismiss()
                }, DIALOG_CHECK_ANIMATION_DURATION)
            }
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)

        if (viewModel.areSettingsChanged) {
            toast(R.string.settings_saved)
        }
    }

    override fun onDestroy() {
        dc.drain()
        currenciesDialog?.dismiss()
        super.onDestroy()
    }

}