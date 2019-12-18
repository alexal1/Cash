/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.moneybox

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.main.MainViewModel
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.navigation.BackStackListener
import com.madewithlove.daybalance.utils.navigation.Navigator
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColorResource
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class MoneyboxFragment : Fragment(), BackStackListener {

    companion object {

        fun create(): MoneyboxFragment = MoneyboxFragment()

    }


    private val mainViewModel by sharedViewModel<MainViewModel>(from = { requireParentFragment() })
    private val viewModel by viewModel<MoneyboxViewModel>()
    private val navigator by lazy { parentFragment as Navigator }
    private val monthFormatter by lazy { SimpleDateFormat("LLLL", ctx.currentLocale()) }
    private val ui: MoneyboxUI get() = moneyboxUI ?: MoneyboxUI().also { moneyboxUI = it }
    private val dc = DisposableCache()

    private var moneyboxUI: MoneyboxUI? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.toolbar.apply {
            setNavigationOnClickListener {
                act.onBackPressed()
            }
        }

        ui.monthMoneyTitle.apply {
            viewModel.moneyboxStateObservable
                .map { it.monthFirstDay }
                .distinctUntilChanged()
                .subscribeOnUi { currentDate ->
                    val month = currentDate.toMonth()
                    text = getString(R.string.moneybox_month_money_title, month)
                }
                .cache(dc)
        }

        ui.monthMoneyDescription.apply {
            viewModel.moneyboxStateObservable
                .map { it.savingsRatio }
                .distinctUntilChanged()
                .subscribeOnUi { ratio ->
                    val formattedRatio = TextFormatter.formatSavingsRatio(ratio)
                    text = getString(R.string.moneybox_month_money_description)
                        .asSpannableBuilder()
                        .replace("{percent}", formattedRatio, StyleSpan(Typeface.BOLD))
                }
                .cache(dc)
        }

        ui.totalMoneyAmount.apply {
            viewModel.moneyboxStateObservable
                .filter { !it.isLoading }
                .map { it.totalMoney!! }
                .distinctUntilChanged()
                .subscribeOnUi { money ->
                    textColorResource = when {
                        money.isGain() -> R.color.white
                        money.isZero() -> R.color.smoke
                        else -> R.color.venous_blood
                    }
                    text = TextFormatter.formatMoney(money)
                }
                .cache(dc)
        }

        ui.monthMoneyAmount.apply {
            viewModel.moneyboxStateObservable
                .filter { !it.isLoading }
                .map { it.monthMoney!! }
                .distinctUntilChanged()
                .subscribeOnUi { money ->
                    textColorResource = when {
                        money.isGain() -> R.color.white
                        money.isZero() -> R.color.smoke
                        else -> R.color.venous_blood
                    }
                    text = TextFormatter.formatMoney(money)
                }
                .cache(dc)
        }

        viewModel.requestData()

        viewModel.moneyboxStateObservable
            .map { it.isLoading }
            .distinctUntilChanged()
            .filter { isLoading -> !isLoading }
            .take(1)
            .subscribeOnUi {
                startPostponedEnterTransition()

                if (navigator.isFragmentOnTop(this@MoneyboxFragment)) {
                    mainViewModel.notifyMoneyboxOpened()
                }
            }
            .cache(dc)
    }

    override fun onResumedFromBackStack() {
        viewModel.requestData()
        mainViewModel.notifyMoneyboxOpened()
    }

    override fun onDestroyView() {
        dc.drain()
        moneyboxUI = null
        super.onDestroyView()

        mainViewModel.notifyMoneyboxClosed()
    }


    private fun Date.toMonth(): String {
        return monthFormatter.format(this)
    }

}