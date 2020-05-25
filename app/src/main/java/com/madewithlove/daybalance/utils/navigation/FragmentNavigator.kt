/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.madewithlove.daybalance.ui.ScreenFragment

abstract class FragmentNavigator(screenName: String) : ScreenFragment(screenName), Navigator {

    private var backStackChangedListener: FragmentManager.OnBackStackChangedListener? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backStackChangedListener = FragmentManager.OnBackStackChangedListener {
            val topFragment = getNavigatorFragmentManager().fragments.lastOrNull()
            if (topFragment != null && topFragment is BackStackListener) {
                topFragment.onResumedFromBackStack()
            } else if (this is BackStackListener) {
                this.onResumedFromBackStack()
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