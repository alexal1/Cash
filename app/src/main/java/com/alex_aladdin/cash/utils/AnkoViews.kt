package com.alex_aladdin.cash.utils

import android.view.ViewManager
import com.alex_aladdin.cash.ui.FancyButton
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.fancyButton(init: FancyButton.() -> Unit = {}): FancyButton {
    return ankoView({ FancyButton(it) }, theme = 0, init = init)
}