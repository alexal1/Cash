/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.activities

import android.os.Bundle
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.subscribeOnUi
import com.madewithlove.daybalance.viewmodels.SplashViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {

    companion object {

        const val OPENED_BY_PUSH = "opened_by_push"

    }


    private val viewModel: SplashViewModel by viewModel()
    private val analytics: Analytics by inject()
    private val dc = DisposableCache()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.finishCompletable.subscribeOnUi {
            MainActivity.start(this)
            finish()
        }.cache(dc)

        val isOpenedByPush = intent.getBooleanExtra(OPENED_BY_PUSH, false)
        if (isOpenedByPush) {
            analytics.clickOnPush()
        }
    }

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }

}