/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.madewithlove.daybalance.features.history.HistoryFragment
import com.madewithlove.daybalance.features.main.MainFragment
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.push.PushManager.Companion.OPENED_BY_PUSH
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.navigation.Navigator
import com.madewithlove.daybalance.utils.subscribeOnUi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BaseActivity : FragmentActivity(), Navigator {

    private val viewModel: BaseViewModel by viewModel()
    private val analytics: Analytics by inject()
    private val dc = DisposableCache()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("ConstantConditionIf", "LiftReturnOrAssignment")
        if (BuildConfig.BUILD_TYPE == "debug") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val isOpenedByPush = intent.getBooleanExtra(OPENED_BY_PUSH, false)
        if (isOpenedByPush) {
            Timber.i("BaseActivity is opened by push")
            analytics.clickOnPush()
        }

        if (savedInstanceState == null) {
            setFragment(MainFragment.create())
        }

        viewModel.openHistorySubject.subscribeOnUi { filter ->
            replaceFragment(HistoryFragment.create(filter))
        }.cache(dc)
    }

    override fun onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }

    override fun getNavigatorFragmentManager() = supportFragmentManager

    override fun getFragmentContainerId() = android.R.id.content

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }

}