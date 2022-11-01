/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.main.MainViewModel
import com.madewithlove.daybalance.ui.ScreenFragment
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.navigation.Navigator
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : ScreenFragment("settings") {

    companion object {

        fun create(): SettingsFragment = SettingsFragment()

    }

    private val mainViewModel by sharedViewModel<MainViewModel>(from = { requireParentFragment() })
    private val viewModel: SettingsViewModel by viewModel()
    private val ui: SettingsUI get() = settingsUI ?: SettingsUI().also { settingsUI = it }
    private val dc = DisposableCache()

    private var settingsUI: SettingsUI? = null
    private var enableNotificationsInSettingsDialog: AlertDialog? = null
    private var areDebugSettingsEnabled = false


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
        super.onViewCreated(view, savedInstanceState)

        ui.toolbar.apply {
            setNavigationOnClickListener {
                act.onBackPressed()
            }
        }

        ui.logoBackground.apply {
            setOnClickListener {
                areDebugSettingsEnabled = true
            }

            setOnLongClickListener {
                if (areDebugSettingsEnabled) {
                    val fragment = DebugSettingsFragment.create()
                    (act as? Navigator)?.addFragment(fragment)
                    return@setOnLongClickListener true
                }

                return@setOnLongClickListener false
            }
        }

        ui.pushMenuItem.requireCustomView().apply {
            isChecked = viewModel.areNotificationsEnabled()
        }

        ui.pushMenuItem.background.apply {
            setOnClickListener {
                if (ui.pushMenuItem.requireCustomView().isChecked) {
                    viewModel.disableNotifications()
                    ui.pushMenuItem.requireCustomView().isChecked = false
                } else {
                    if (viewModel.tryEnableNotifications()) {
                        ui.pushMenuItem.requireCustomView().isChecked = true
                    } else {
                        showEnableNotificationsInSettingsDialog()
                    }
                }
            }
        }

        ui.policyMenuItem.background.apply {
            setOnClickListener {
                val policyTitle = getString(R.string.privacy_policy)
                val fragment = WebPageFragment.create(
                    "https://alexal1.github.io/daybalance/",
                    policyTitle
                )

                (act as? Navigator)?.addFragment(fragment)
            }
        }

        view.post {
            startPostponedEnterTransition()
            mainViewModel.notifySettingsOpened()
        }
    }

    override fun onDestroyView() {
        dc.drain()
        settingsUI = null
        enableNotificationsInSettingsDialog = null
        super.onDestroyView()

        mainViewModel.notifySettingsClosed()
    }


    private fun showEnableNotificationsInSettingsDialog() {
        enableNotificationsInSettingsDialog?.dismiss()
        enableNotificationsInSettingsDialog = AlertDialog.Builder(ctx)
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
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", ctx.packageName)
            intent.putExtra("app_uid", ctx.applicationInfo.uid)
        }
        startActivity(intent)
    }

}