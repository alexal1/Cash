/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.madewithlove.daybalance.ScreenFragment
import com.madewithlove.daybalance.CashApp
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DebugSettingsFragment : ScreenFragment("debug_settings") {

    companion object {

        fun create(): DebugSettingsFragment = DebugSettingsFragment()

    }


    private val viewModel: SettingsViewModel by sharedViewModel()
    private val ui: DebugSettingsUI get() = debugSettingsUI ?: DebugSettingsUI().also { debugSettingsUI = it }

    private var debugSettingsUI: DebugSettingsUI? = null


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

        ui.showPushBackground.apply {
            setOnClickListener {
                viewModel.showPush()
            }
        }

        ui.enableLogsSwitch.apply {
            isEnabled = !CashApp.isDebugBuild
        }

        ui.enableLogsBackground.apply {
            val enableLogsSwitch = ui.enableLogsSwitch
            enableLogsSwitch.isChecked = viewModel.areLogsEnabled()

            setOnClickListener {
                val succeed = viewModel.setLogsEnabled(!enableLogsSwitch.isChecked)
                if (succeed) {
                    enableLogsSwitch.isChecked = !enableLogsSwitch.isChecked
                }
            }
        }

        ui.repeatShowcaseBackground.apply {
            setOnClickListener {
                viewModel.repeatShowcase()
            }
        }
    }

    override fun onDestroyView() {
        debugSettingsUI = null
        super.onDestroyView()
    }

}