/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.helpers.Analytics
import org.koin.android.ext.android.inject

abstract class ScreenFragment(val screenName: String) : Fragment() {

    private val analytics: Analytics by inject()

    private var delayedAction: (() -> Unit)? = null


    fun sendScreenNameToAnalytics() {
        if (isAdded) {
            analytics.setCurrentScreen(requireActivity(), screenName)
        } else {
            delayedAction = {
                analytics.setCurrentScreen(requireActivity(), screenName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        delayedAction?.invoke()
        delayedAction = null
    }

}