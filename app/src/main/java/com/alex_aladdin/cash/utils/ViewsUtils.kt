package com.alex_aladdin.cash.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import org.jetbrains.anko.dip

fun View.appear(animationDuration: Long = 200, maxTranslation: Int = dip(10)) {
    (tag as? Animator)?.cancel()

    if (alpha == 1.0f) {
        return
    }

    val animator = ValueAnimator.ofFloat(alpha, 1.0f).apply {
        duration = animationDuration

        addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            alpha = value
            translationY = (1.0f - value) * maxTranslation
        }

        start()
    }

    tag = animator
}

fun View.disappear(animationDuration: Long = 200, maxTranslation: Int = dip(10)) {
    (tag as? Animator)?.cancel()

    if (alpha == 0.0f) {
        return
    }

    val animator = ValueAnimator.ofFloat(alpha, 0.0f).apply {
        duration = animationDuration

        addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            alpha = value
            translationY = (1.0f - value) * maxTranslation
        }

        start()
    }

    tag = animator
}