/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

abstract class FragmentNavigator : Fragment(), Navigator {

    private var backStackChangedListener: FragmentManager.OnBackStackChangedListener? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backStackChangedListener = FragmentManager.OnBackStackChangedListener {
            val topFragment = getNavigatorFragmentManager().fragments.lastOrNull()
            if (topFragment != null && topFragment is BackStackListener) {
                topFragment.onResumedFromBackStack()
            }
        }

        getNavigatorFragmentManager().addOnBackStackChangedListener(backStackChangedListener)
        this.backStackChangedListener = backStackChangedListener
    }

    override fun onDestroyView() {
        backStackChangedListener?.let(getNavigatorFragmentManager()::removeOnBackStackChangedListener)
        backStackChangedListener = null
        super.onDestroyView()
    }

}