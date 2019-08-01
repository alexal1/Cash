/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.SpannableString
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import com.jakewharton.rxbinding3.view.clicks
import com.madewithlove.daybalance.BuildConfig
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.spans.TypefaceSpan
import com.madewithlove.daybalance.viewmodels.SettingsViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class SettingsActivity : BaseActivity() {

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
    private var enableNotficationsInSettingsDialog: AlertDialog? = null


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
                backgroundResource = R.drawable.bg_debug_settings_button
                textColorResource = R.color.smoke
                textSize = 12f
                gravity = CENTER
                letterSpacing = 0.01f

                @SuppressLint("SetTextI18n")
                text = "${getString(R.string.app_name)}  v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

                clicks()
                    .scanWith({ 0 }, { sum, _ -> sum + 1 })
                    .filter {
                        @Suppress("ConstantConditionIf")
                        if (BuildConfig.BUILD_TYPE == "debug") {
                            it >= 1
                        } else {
                            it >= 10
                        }
                    }
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .subscribeOnUi {
                        DebugSettingsActivity.start(this@SettingsActivity)
                    }.cache(dc)
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
            showSeparator = true,
            throttleClicks = true,
            controlView = textView {
                id = View.generateViewId()
                textColorResource = R.color.blue
                textSize = 16f
                backgroundColor = Color.TRANSPARENT
                typeface = ResourcesCompat.getFont(context, R.font.currencies)

                viewModel.currencyObservable.subscribeOnUi {
                    text = it
                }.cache(dc)
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dip(24)
            },
            onClickListener = {
                showCurrenciesDialog()
            }
        )

        val autoSwitchItem = getSettingsItem(
            R.string.settings_auto_switch_title,
            R.string.settings_auto_switch_subtitle,
            showSeparator = true,
            throttleClicks = true,
            controlView = textView {
                id = View.generateViewId()
                textColorResource = R.color.blue
                textSize = 14f
                backgroundColor = Color.TRANSPARENT

                viewModel.autoSwitchCurrencyObservable.subscribeOnUi {
                    text = it
                }.cache(dc)
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dip(24)
            },
            onClickListener = {
                showAutoSwitchCurrencyDialog()
            }
        )

        val pushNotificationsSwitch = switch {
            id = View.generateViewId()
            isChecked = viewModel.getNotificationsEnabled()
        }.lparams(wrapContent, wrapContent) {
            rightMargin = dip(16)
        }

        val pushNotificationsItem = getSettingsItem(
            R.string.settings_push_notifications_title,
            R.string.settings_push_notifications_subtitle,
            showSeparator = true,
            throttleClicks = false,
            controlView = pushNotificationsSwitch,
            onClickListener = {
                if (pushNotificationsSwitch.isChecked) {
                    viewModel.disableNotifications()
                    pushNotificationsSwitch.isChecked = false
                } else {
                    if (viewModel.tryEnableNotifications()) {
                        pushNotificationsSwitch.isChecked = true
                    } else {
                        showEnableNotificationsInSettingsDialog()
                    }
                }
            }
        )

        val tipsItem = getSettingsItem(
            R.string.settings_tips_title,
            R.string.settings_tips_subtitle,
            showSeparator = true,
            throttleClicks = true,
            controlView = textView {
                id = View.generateViewId()
                textColorResource = R.color.blue
                textSize = 14f
                textResource = R.string.settings_tips_reset
                backgroundColor = Color.TRANSPARENT
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dip(24)
            },
            onClickListener = {
                viewModel.resetTips()
                toast(R.string.settings_tips_have_been_reset)
            }
        )

        val privacyPolicyItem = getSettingsItem(
            R.string.settings_privacy_policy_title,
            R.string.settings_privacy_policy_subtitle,
            showSeparator = true,
            throttleClicks = true,
            controlView = space {
                id = View.generateViewId()
            },
            onClickListener = {
                WebPageActivity.start(this@SettingsActivity, "https://daybalance.github.io/privacy_policy.html")
            }
        )

        val googlePlayItem = getSettingsItem(
            R.string.settings_google_play_title,
            R.string.settings_google_play_subtitle,
            showSeparator = false,
            throttleClicks = true,
            controlView = space {
                id = View.generateViewId()
            },
            onClickListener = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    setPackage("com.android.vending")
                }
                startActivity(intent)
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

            connect(
                START of pushNotificationsItem to START of PARENT_ID,
                END of pushNotificationsItem to END of PARENT_ID,
                TOP of pushNotificationsItem to BOTTOM of autoSwitchItem
            )

            connect(
                START of tipsItem to START of PARENT_ID,
                END of tipsItem to END of PARENT_ID,
                TOP of tipsItem to BOTTOM of pushNotificationsItem
            )

            connect(
                START of privacyPolicyItem to START of PARENT_ID,
                END of privacyPolicyItem to END of PARENT_ID,
                TOP of privacyPolicyItem to BOTTOM of tipsItem
            )

            connect(
                START of googlePlayItem to START of PARENT_ID,
                END of googlePlayItem to END of PARENT_ID,
                TOP of googlePlayItem to BOTTOM of privacyPolicyItem
            )
        }
    }

    private fun _ConstraintLayout.getSettingsItem(
        @StringRes titleRes: Int,
        @StringRes subtitleRes: Int,
        showSeparator: Boolean,
        throttleClicks: Boolean,
        controlView: View,
        onClickListener: () -> Unit
    ): View {
        val backgroundView = view {
            id = View.generateViewId()

            setSelectableBackground()

            if (throttleClicks) {
                setOnClickListenerWithThrottle {
                    onClickListener()
                }.cache(dc)
            } else {
                setOnClickListener {
                    onClickListener()
                }
            }
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

        val separator = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.palladium_80
            isInvisible = !showSeparator
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
        val typeface = ResourcesCompat.getFont(this, R.font.currencies)!!
        val typefaceSpan = TypefaceSpan(typeface)

        currenciesDialog?.dismiss()
        currenciesDialog = AlertDialog.Builder(this@SettingsActivity)
            .setTitle(R.string.settings_currency_title)
            .setSingleChoiceItems(
                currencyManager.getCurrenciesList()
                    .map { currency ->
                        SpannableString(currency).apply {
                            setSpan(typefaceSpan, 0, currency.length, 0)
                        }
                    }
                    .toTypedArray(),
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

    private fun showEnableNotificationsInSettingsDialog() {
        enableNotficationsInSettingsDialog?.dismiss()
        enableNotficationsInSettingsDialog = AlertDialog.Builder(this@SettingsActivity)
            .setMessage(R.string.settings_push_notifications_dialog_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                openDeviceSettings()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openDeviceSettings() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)
        }
        startActivity(intent)
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
        autoSwitchCurrencyDialog?.dismiss()
        enableNotficationsInSettingsDialog?.dismiss()
        super.onDestroy()
    }

}