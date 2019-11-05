/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.animation.doOnEnd
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
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

fun View.setSelectableBackground(isBorderless: Boolean = false) {
    isClickable = true
    isFocusable = true

    val attribute = if (isBorderless) {
        android.R.attr.selectableItemBackgroundBorderless
    } else {
        android.R.attr.selectableItemBackground
    }

    val outValue = TypedValue()
    context.theme.resolveAttribute(attribute, outValue, true)
    setBackgroundResource(outValue.resourceId)
}

/**
 * Increases View's touch area by scaling it with given coefficient.
 * Note that this function sets TouchDelegate to this View's parent, thus you cannot use this
 * function on multiple children inside one parent.
 */
fun View.expandHitArea(scale: Float) {
    fun expand() {
        val viewArea = Rect()
        val delegateArea = Rect()
        getHitRect(viewArea)
        delegateArea.set(
            viewArea.left - (viewArea.width() * (scale - 1) / 2f).toInt(),
            viewArea.top - (viewArea.height() * (scale - 1) / 2f).toInt(),
            viewArea.right + (viewArea.width() * (scale - 1) / 2f).toInt(),
            viewArea.bottom + (viewArea.height() * (scale - 1) / 2f).toInt()
        )
        (parent as View).touchDelegate = TouchDelegate(delegateArea, this)
    }

    if (width > 0 && height > 0) {
        expand()
    } else {
        // If sizes are 0, wait until view is layouted
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                expand()
            }
        })
    }
}

fun View.getRect(): Rect {
    val rect = Rect()
    getHitRect(rect)
    return rect
}

fun EditText.showKeyboard() {
    requestFocus()

    post {
        val mgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }
}

fun View.hideKeyboard() {
    val mgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    mgr.hideSoftInputFromWindow(windowToken, 0)

    clearFocus()
}

fun Activity.keyboardListener(): Observable<Boolean> = window.decorView.keyboardObservable().map { it.isVisible }

fun View.keyboardObservable(): Observable<KeyboardState> = Observable.create<KeyboardState> { emitter ->
    val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect().apply { getWindowVisibleDisplayFrame(this) }

        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        val screenHeight = metrics.heightPixels
        val dy = screenHeight - bottom
        val keypadHeight = screenHeight - dy - rect.bottom

        if (keypadHeight > screenHeight * 0.15) {
            emitter.onNext(KeyboardState(true, keypadHeight))
        } else {
            emitter.onNext(KeyboardState(false, 0))
        }
    }

    viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

    emitter.setCancellable {
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }
}.distinctUntilChanged()

data class KeyboardState(val isVisible: Boolean, val height: Int)