/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.madewithlove.daybalance.R

interface Navigator : BackPressHandler {

    fun setFragment(fragment: Fragment) {
        if (fragment.isAddedToNavigator()) {
            return
        }

        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.fade_in, 0)
            .add(getFragmentContainerId(), fragment, fragment.getNavigatorTag())
            .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        if (fragment.isAddedToNavigator()) {
            return
        }

        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.go_in_up, R.anim.go_out_up, R.anim.go_in_down, R.anim.go_out_down)
            .replace(getFragmentContainerId(), fragment, fragment.getNavigatorTag())
            .addToBackStack(null)
            .commit()
    }

    fun addFragment(fragment: Fragment) {
        if (fragment.isAddedToNavigator()) {
            return
        }

        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.slide_in_left, 0, 0, R.anim.slide_out_right)
            .add(getFragmentContainerId(), fragment, fragment.getNavigatorTag())
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


    private fun Fragment.getNavigatorTag(): String {
        return this.javaClass.simpleName
    }

    private fun Fragment.isAddedToNavigator(): Boolean {
        return getNavigatorFragmentManager().findFragmentByTag(this.getNavigatorTag()) != null
    }

}