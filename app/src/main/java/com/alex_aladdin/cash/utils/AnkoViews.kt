package com.alex_aladdin.cash.utils

import android.view.ViewManager
import androidx.appcompat.widget.AppCompatTextView
import com.alex_aladdin.cash.ui.FancyButton
import com.alex_aladdin.cash.ui.chart.ChartView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.fancyButton(init: FancyButton.() -> Unit = {}): FancyButton {
    return ankoView({ FancyButton(it) }, theme = 0, init = init)
}

inline fun ViewManager.chartView(init: ChartView.() -> Unit = {}): ChartView {
    return ankoView({ ChartView(it) }, theme = 0, init = init)
}

inline fun ViewManager.appCompatTextView(init: AppCompatTextView.() -> Unit = {}): AppCompatTextView {
    return ankoView({ AppCompatTextView(it) }, theme = 0, init = init)
}