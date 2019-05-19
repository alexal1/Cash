package com.alex_aladdin.cash.utils.rxlayouts

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.google.android.material.appbar.AppBarLayout

@Suppress("ClassName")
open class _AppBarLayout(ctx: Context): AppBarLayout(ctx) {

    inline fun <T: View> T.lparams(
        width: Int = WRAP_CONTENT,
        height: Int = WRAP_CONTENT,
        init: LayoutParams.() -> Unit = {}
    ): T {
        val layoutParams = LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

}