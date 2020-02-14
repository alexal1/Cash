/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DebugSettingsFragment : Fragment() {

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
    }

    override fun onDestroyView() {
        debugSettingsUI = null
        super.onDestroyView()
    }

}