package com.alex_aladdin.cash.utils.ankolayouts

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.coordinatorlayout.widget.CoordinatorLayout

@Suppress("ClassName")
open class _CoordinatorLayout(ctx: Context): CoordinatorLayout(ctx) {

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