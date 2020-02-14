/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.anko

import android.view.ViewManager
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.madewithlove.daybalance.ui.FancyButton
import com.madewithlove.daybalance.ui.KeypadView
import com.madewithlove.daybalance.ui.PercentagePicker
import com.madewithlove.daybalance.ui.TransactionsList
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.ui.dates.DatesRecyclerView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.fancyButton(init: FancyButton.() -> Unit = {}): FancyButton {
    return ankoView({ FancyButton(it) }, theme = 0, init = init)
}

inline fun ViewManager.appCompatTextView(init: AppCompatTextView.() -> Unit = {}): AppCompatTextView {
    return ankoView({ AppCompatTextView(it) }, theme = 0, init = init)
}

inline fun ViewManager.transactionsList(init: TransactionsList.() -> Unit = {}): TransactionsList {
    return ankoView({ TransactionsList(it) }, theme = 0, init = init)
}

inline fun ViewManager.appCompatToolbar(init: _Toolbar.() -> Unit = {}): _Toolbar {
    return ankoView({ _Toolbar(it) }, theme = 0, init = init)
}

inline fun ViewManager.tabLayout(init: TabLayout.() -> Unit = {}): TabLayout {
    return ankoView({ TabLayout(it) }, theme = 0, init = init)
}

inline fun ViewManager.datesRecyclerView(init: DatesRecyclerView.() -> Unit = {}): DatesRecyclerView {
    return ankoView({ DatesRecyclerView(it) }, theme = 0, init = init)
}

inline fun ViewManager.floatingActionButton(init: FloatingActionButton.() -> Unit = {}): FloatingActionButton {
    return ankoView({ FloatingActionButton(it) }, theme = 0, init = init)
}

inline fun ViewManager.circleView(init: CircleView.() -> Unit = {}): CircleView {
    return ankoView({ CircleView(it) }, theme = 0, init = init)
}

inline fun ViewManager.keypadView(init: KeypadView.() -> Unit = {}): KeypadView {
    return ankoView({ KeypadView(it) }, theme = 0, init = init)
}

inline fun ViewManager.percentagePicker(init: PercentagePicker.() -> Unit = {}): PercentagePicker {
    return ankoView({ PercentagePicker(it) }, theme = 0, init = init)
}