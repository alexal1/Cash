/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.madewithlove.daybalance.features.history.HistoryFragment
import com.madewithlove.daybalance.features.main.MainFragment
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.RxErrorHandler
import com.madewithlove.daybalance.helpers.push.PushManager.Companion.OPENED_BY_PUSH
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.navigation.Navigator
import com.madewithlove.daybalance.utils.string
import com.madewithlove.daybalance.utils.subscribeOnUi
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.okButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BaseActivity : FragmentActivity(), Navigator {

    private val viewModel: BaseViewModel by viewModel()
    private val analytics: Analytics by inject()
    private val errorHandler: RxErrorHandler by inject()
    private val dc = DisposableCache()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = if (CashApp.isDebugBuild) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }

        val isOpenedByPush = intent.getBooleanExtra(OPENED_BY_PUSH, false)
        if (isOpenedByPush) {
            Timber.i("BaseActivity is opened by push")
            analytics.clickOnPush()
        }

        if (savedInstanceState == null) {
            setFragment(MainFragment.create())
        }

        errorHandler.errorMessageObservable.subscribeOnUi { errorMessage ->
            alert(Appcompat, errorMessage, string(R.string.error_title)) {
                okButton {}
            }.show()
        }.cache(dc)

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