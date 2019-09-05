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
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SplashActivity : BaseActivity() {

    companion object {

        const val OPENED_BY_PUSH = "opened_by_push"

        private const val MAX_TIME_TO_OPEN_MAIN_ACTIVITY = 5L

    }


    private val viewModel: SplashViewModel by viewModel()
    private val analytics: Analytics by inject()
    private val dc = DisposableCache()

    private var timerDisposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("SplashActivity created")

        viewModel.finishCompletable.subscribeOnUi {
            Timber.i("Cache data obtained successfully, starting MainActivity...")
            MainActivity.start(this)
            timerDisposable?.dispose()
            finish()
        }.cache(dc)

        timerDisposable = Observable.timer(MAX_TIME_TO_OPEN_MAIN_ACTIVITY, TimeUnit.SECONDS).subscribe {
            throw Exception("MainActivity wasn't launched in $MAX_TIME_TO_OPEN_MAIN_ACTIVITY seconds")
        }.cache(dc)

        val isOpenedByPush = intent.getBooleanExtra(OPENED_BY_PUSH, false)
        if (isOpenedByPush) {
            Timber.i("SplashActivity is opened by push")
            analytics.clickOnPush()
        }

        window.decorView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@SplashActivity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    Timber.i("Double tap on SplashActivity, starting DebugSettingsActivity...")
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
        Timber.i("Destroying SplashActivity...")
        dc.drain()
        super.onDestroy()
    }

}