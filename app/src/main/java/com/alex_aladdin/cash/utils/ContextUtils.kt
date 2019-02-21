package com.alex_aladdin.cash.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import org.jetbrains.anko.windowManager
import java.util.*

fun Context.screenSize(): Point {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return Point(metrics.widthPixels, metrics.heightPixels)
}

fun Context.currentLocale(): Locale =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }