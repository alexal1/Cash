package com.alex_aladdin.cash.ui.activities

import android.os.Bundle
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.SplashViewModel
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