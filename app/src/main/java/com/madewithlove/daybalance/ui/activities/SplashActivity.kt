/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
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

        window.decorView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@SplashActivity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    DebugSettingsActivity.start(this@SplashActivity)
                    return true
                }
            })

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                return true
            }
        })
    }

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }

}