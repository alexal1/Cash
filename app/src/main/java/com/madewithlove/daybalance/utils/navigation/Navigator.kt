/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.madewithlove.daybalance.R

interface Navigator : BackPressHandler {

    fun setFragment(fragment: Fragment) {
        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.fade_in, 0)
            .add(getFragmentContainerId(), fragment)
            .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.go_in_up, R.anim.go_out_up, R.anim.go_in_down, R.anim.go_out_down)
            .replace(getFragmentContainerId(), fragment)
            .addToBackStack(null)
            .commit()
    }

    fun addFragment(fragment: Fragment) {
        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.slide_in_left, 0, 0, R.anim.slide_out_right)
            .add(getFragmentContainerId(), fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun handleBackPress(): Boolean {
        val topFragment = getNavigatorFragmentManager().fragments.lastOrNull() ?: return false

        if (topFragment !is BackPressHandler || !topFragment.handleBackPress()) {
            return getNavigatorFragmentManager().popBackStackImmediate()
        }

        return true
    }

    fun getNavigatorFragmentManager(): FragmentManager

    fun getFragmentContainerId(): Int

}