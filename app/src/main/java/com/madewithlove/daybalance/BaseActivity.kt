/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.madewithlove.daybalance.features.history.HistoryFragment
import com.madewithlove.daybalance.features.main.MainFragment
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.navigation.FragmentNavigator
import com.madewithlove.daybalance.utils.subscribeOnUi
import org.jetbrains.anko.frameLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class BaseActivity : AppCompatActivity(), FragmentNavigator {

    private val viewModel: BaseViewModel by viewModel()
    private val dc = DisposableCache()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("ConstantConditionIf", "LiftReturnOrAssignment")
        if (BuildConfig.BUILD_TYPE == "debug") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }


        frameLayout {
            id = R.id.base_container
        }

        if (savedInstanceState == null) {
            setFragment(MainFragment.create())
        }

        viewModel.openHistorySubject.subscribeOnUi {
            replaceFragment(HistoryFragment.create())
        }.cache(dc)
    }

    override fun onBackPressed() {
        if (!onNavigatorBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun getNavigatorFragmentManager() = supportFragmentManager

    override fun getFragmentContainerId() = R.id.base_container

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }

}