/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.madewithlove.daybalance.ui.ScreenFragment
import com.madewithlove.daybalance.R

interface Navigator : BackPressHandler {

    fun setFragment(fragment: ScreenFragment, callback: () -> Unit = {}) {
        if (fragment.isAddedToNavigator()) {
            return
        }

        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.fade_in, 0)
            .add(getFragmentContainerId(), fragment, fragment.getNavigatorTag())
            .runOnCommit(callback)
            .commit()

        fragment.sendScreenNameToAnalytics()
    }

    fun addFragment(fragment: ScreenFragment) {
        if (fragment.isAddedToNavigator()) {
            return
        }

        getNavigatorFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.slide_in_left, 0, 0, R.anim.slide_out_right)
            .add(getFragmentContainerId(), fragment, fragment.getNavigatorTag())
            .addToBackStack(fragment.getNavigatorName())
            .commit()

        fragment.sendScreenNameToAnalytics()
    }

    override fun handleBackPress(): Boolean {
        val topFragment = getNavigatorFragmentManager().fragments.lastOrNull() ?: return false

        if (topFragment !is BackPressHandler || !topFragment.handleBackPress()) {
            val isPopped = getNavigatorFragmentManager().popBackStackImmediate()
            if (isPopped) {
                val newTopFragment = (getNavigatorFragmentManager().fragments.lastOrNull() ?: this) as? ScreenFragment
                newTopFragment?.sendScreenNameToAnalytics()
            }
            return isPopped
        }

        return true
    }

    fun isFragmentOnTop(fragment: ScreenFragment): Boolean {
        val topEntryIndex = getNavigatorFragmentManager().backStackEntryCount - 1
        val topEntry = getNavigatorFragmentManager().getBackStackEntryAt(topEntryIndex)
        return topEntry.name == fragment.getNavigatorName()
    }

    fun getNavigatorFragmentManager(): FragmentManager

    fun getFragmentContainerId(): Int


    private fun ScreenFragment.getNavigatorTag(): String {
        return this.screenName
    }

    private fun ScreenFragment.getNavigatorName(): String {
        return this.screenName
    }

    private fun ScreenFragment.isAddedToNavigator(): Boolean {
        return getNavigatorFragmentManager().findFragmentByTag(this.getNavigatorTag()) != null
    }

}

fun Fragment.isOnTop(): Boolean {
    var topFragment: Fragment?
    var navigator = activity as Navigator

    while (true) {
        topFragment = navigator.getNavigatorFragmentManager().fragments.lastOrNull()

        if (topFragment != null && topFragment is Navigator) {
            navigator = topFragment
        } else if (topFragment == null) {
            topFragment = navigator as Fragment
            break
        } else {
            break
        }
    }

    if (topFragment == null) {
        throw RuntimeException("Cannot find top fragment")
    }

    return this == topFragment
}