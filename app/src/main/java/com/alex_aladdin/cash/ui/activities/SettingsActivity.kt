package com.alex_aladdin.cash.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import com.alex_aladdin.cash.BuildConfig
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.SettingsViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    companion object {

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
        val currencyTitle = textView {
            id = View.generateViewId()
            textColorResource = R.color.white
            textSize = 16f
            backgroundColor = Color.TRANSPARENT
            textResource = R.string.settings_currency_title
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dip(24)
            leftMargin = dip(24)
        }

        val currencySubtitle = textView {
            id = View.generateViewId()
            textColorResource = R.color.smoke
            textSize = 12f
            backgroundColor = Color.TRANSPARENT
            textResource = R.string.settings_currency_subtitle
        }.lparams(matchConstraint, wrapContent) {
            leftMargin = dip(24)
        }

        val currency = textView {
            id = View.generateViewId()
            textColorResource = R.color.white
            textSize = 32f
            backgroundColor = Color.TRANSPARENT
            includeFontPadding = false

            viewModel.currencyObservable.subscribeOnUi {
                text = it
            }.cache(dc)
        }.lparams(wrapContent, wrapContent) {
            topMargin = dip(24)
            leftMargin = dip(12)
        }

        val currencyChangeButton = imageButton {
            id = View.generateViewId()
            padding = dip(12)
            setImageResource(R.drawable.ic_drop_down)

            setSelectableBackground(true)
            setOnClickListenerWithThrottle {
                showCurrenciesDialog()
            }.cache(dc)
        }

        val currencySeparator = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.palladium_80
        }.lparams(matchConstraint, dip(1)) {
            topMargin = dip(12)
            leftMargin = dip(24)
        }

        applyConstraintSet {
            connect(
                START of currencyTitle to START of PARENT_ID,
                END of currencyTitle to START of currency,
                TOP of currencyTitle to TOP of PARENT_ID
            )

            connect(
                START of currencySubtitle to START of PARENT_ID,
                END of currencySubtitle to START of currency,
                TOP of currencySubtitle to BOTTOM of currencyTitle
            )

            connect(
                TOP of currency to TOP of PARENT_ID,
                END of currency to START of currencyChangeButton
            )

            connect(
                TOP of currencyChangeButton to TOP of currency,
                BOTTOM of currencyChangeButton to BOTTOM of currency,
                END of currencyChangeButton to END of PARENT_ID
            )

            connect(
                START of currencySeparator to START of PARENT_ID,
                END of currencySeparator to END of PARENT_ID,
                TOP of currencySeparator to BOTTOM of currencySubtitle
            )
        }
    }

    private fun showCurrenciesDialog() {
        var chosenIndex = currencyManager.getCurrentCurrencyIndex()

        currenciesDialog?.dismiss()
        currenciesDialog = AlertDialog.Builder(this@SettingsActivity)
            .setTitle(R.string.settings_currency_title)
            .setSingleChoiceItems(
                currencyManager.getCurrenciesList().toTypedArray(),
                chosenIndex
            ) { _, index -> chosenIndex = index }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                currencyManager.setCurrentCurrencyIndex(chosenIndex)
                viewModel.notifyCurrencyWasChanged()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
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