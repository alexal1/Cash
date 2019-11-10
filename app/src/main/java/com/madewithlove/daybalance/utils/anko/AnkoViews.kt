/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.utils.anko

import android.app.Activity
import android.view.ViewManager
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.madewithlove.daybalance.ui.*
import com.madewithlove.daybalance.ui.chart.ChartView
import com.madewithlove.daybalance.ui.circle.CircleView
import com.madewithlove.daybalance.ui.dates.DatesRecyclerView
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

inline fun ViewManager.currencyPicker(init: CurrencyPicker.() -> Unit = {}): CurrencyPicker {
    return ankoView({ CurrencyPicker(it) }, theme = 0, init = init)
}

inline fun ViewManager.categoryPicker(init: CategoryPicker.() -> Unit = {}): CategoryPicker {
    return ankoView({ CategoryPicker(it) }, theme = 0, init = init)
}

inline fun Activity.coordinatorLayout(init: _CoordinatorLayout.() -> Unit = {}): _CoordinatorLayout {
    return ankoView({ _CoordinatorLayout(it) }, theme = 0, init = init)
}

inline fun ViewManager.appBarLayout(init: _AppBarLayout.() -> Unit = {}): _AppBarLayout {
    return ankoView({ _AppBarLayout(it) }, theme = 0, init = init)
}

inline fun ViewManager.collapsingToolbarLayout(init: _CollapsingToolbarLayout.() -> Unit = {}): _CollapsingToolbarLayout {
    return ankoView({ _CollapsingToolbarLayout(it) }, theme = 0, init = init)
}

inline fun ViewManager.transactionsList(init: TransactionsList.() -> Unit = {}): TransactionsList {
    return ankoView({ TransactionsList(it) }, theme = 0, init = init)
}

inline fun ViewManager.transactionsListOld(init: TransactionsListOld.() -> Unit = {}): TransactionsListOld {
    return ankoView({ TransactionsListOld(it) }, theme = 0, init = init)
}

inline fun ViewManager.appCompatToolbar(init: _Toolbar.() -> Unit = {}): _Toolbar {
    return ankoView({ _Toolbar(it) }, theme = 0, init = init)
}

inline fun ViewManager.tabLayout(init: TabLayout.() -> Unit = {}): TabLayout {
    return ankoView({ TabLayout(it) }, theme = 0, init = init)
}

inline fun ViewManager.shortTransactionsList(init: ShortTransactionsList.() -> Unit = {}): ShortTransactionsList {
    return ankoView({ ShortTransactionsList(it) }, theme = 0, init = init)
}

inline fun ViewManager.dashedLineView(init: DashedLineView.() -> Unit = {}): DashedLineView {
    return ankoView({ DashedLineView(it) }, theme = 0, init = init)
}

inline fun ViewManager.datesRecyclerView(init: DatesRecyclerView.() -> Unit = {}): DatesRecyclerView {
    return ankoView({ DatesRecyclerView(it) }, theme = 0, init = init)
}

inline fun ViewManager.tipsView(init: TipsView.() -> Unit = {}): TipsView {
    return ankoView({ TipsView(it) }, theme = 0, init = init)
}

inline fun ViewManager.shortStatisticsView(init: ShortStatisticsView.() -> Unit = {}): ShortStatisticsView {
    return ankoView({ ShortStatisticsView(it) }, theme = 0, init = init)
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