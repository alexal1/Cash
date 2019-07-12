package com.madewithlove.daybalance.ui.activities

import android.os.Bundle
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.subscribeOnUi
import com.madewithlove.daybalance.viewmodels.SplashViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {

    private val viewModel: SplashViewModel by viewModel()
    private val dc = DisposableCache()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.finishCompletable.subscribeOnUi {
            MainActivity.start(this)
            finish()
        }.cache(dc)
    }

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }

}