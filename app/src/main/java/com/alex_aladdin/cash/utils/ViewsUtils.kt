package com.alex_aladdin.cash.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

fun View.blink(doOnInvisible: (view: View) -> Unit) {
    val animationDuration = 400L

    val fadeOut = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
        duration = animationDuration / 2
        addUpdateListener { alpha = it.animatedValue as Float }
        doOnEnd { doOnInvisible(this@blink) }
    }

    val fadeIn = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
        duration = animationDuration / 2
        addUpdateListener { alpha = it.animatedValue as Float }
    }

    AnimatorSet().apply {
        playSequentially(fadeOut, fadeIn)
        start()
    }
}

fun View.setOnClickListenerWithThrottle(action: () -> Unit): Disposable = clicks().throttleFirst(1, TimeUnit.SECONDS).subscribeOnUi {
    action()
}