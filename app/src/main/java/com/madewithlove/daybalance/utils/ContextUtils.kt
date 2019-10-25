/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.jetbrains.anko.windowManager
import java.util.*

fun Context.screenSize(): Point {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return Point(metrics.widthPixels, metrics.heightPixels - statusBarHeight())
}

fun Context.statusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}

fun Context.currentLocale(): Locale =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }

fun Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun View.color(@ColorRes colorRes: Int): Int = context.color(colorRes)

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable = ContextCompat.getDrawable(this, drawableRes)!!

fun View.drawable(@DrawableRes drawableRes: Int): Drawable = context.drawable(drawableRes)

fun Context.string(@StringRes stringRes: Int): String = getString(stringRes)

fun Context.string(@StringRes res: Int, vararg replacements: Pair<String, String>): String = string(res).run {
    var result = this

    replacements.forEach { (target, replacement) ->
        val index = indexOf(target)
        result = result.replaceRange(index, index + target.length, replacement)
    }

    result
}

fun View.string(@StringRes stringRes: Int): String = context.getString(stringRes)