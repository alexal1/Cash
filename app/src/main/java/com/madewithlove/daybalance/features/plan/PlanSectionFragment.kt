/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.*
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColorResource
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class PlanSectionFragment : Fragment() {

    companion object {

        private const val SECTION = "section"


        fun create(section: PlanViewModel.Section): PlanSectionFragment = PlanSectionFragment().apply {
            arguments = bundleOf(SECTION to section)
        }

    }


    private val viewModel by sharedViewModel<PlanViewModel>(from = { parentFragment!! })
    private val section by lazy { arguments!!.get(SECTION) as PlanViewModel.Section }
    private val monthFormatter by lazy { SimpleDateFormat("LLLL", ctx.currentLocale()) }
    private val ui: PlanSectionUI get() = planSectionUI ?: PlanSectionUI().also { planSectionUI = it }
    private val dc = DisposableCache()

    private var planSectionUI: PlanSectionUI? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.descriptionText.apply {
            val month = viewModel.planState.currentDate.toMonth()
            text = when (section) {
                PlanViewModel.Section.GAIN -> string(R.string.plan_section_gain_description, "{month}" to month)
                PlanViewModel.Section.LOSS -> string(R.string.plan_section_loss_description)
                PlanViewModel.Section.MONEYBOX -> string(R.string.plan_section_moneybox_description)
            }
        }

        ui.amountText.apply {
            when (section) {
                PlanViewModel.Section.GAIN -> {
                    textColorResource = R.color.green

                    viewModel.planStateObservable
                        .filter { !it.isLoading }
                        .map { it.gain!! }
                        .distinctUntilChanged()
                        .subscribeOnUi { money ->
                            text = TextFormatter.formatMoney(money)
                        }
                        .cache(dc)
                }

                PlanViewModel.Section.LOSS -> {
                    textColorResource = R.color.red

                    viewModel.planStateObservable
                        .filter { !it.isLoading }
                        .map { it.loss!! }
                        .distinctUntilChanged()
                        .subscribeOnUi { money ->
                            text = TextFormatter.formatMoney(money)
                        }
                        .cache(dc)
                }

                PlanViewModel.Section.MONEYBOX -> {
                    textColorResource = R.color.blue

                    viewModel.planStateObservable
                        .filter { !it.isLoading }
                        .map { it.savingsRatio!! }
                        .distinctUntilChanged()
                        .subscribeOnUi { savingsRatio ->
                            text = TextFormatter.formatSavingsRatio(savingsRatio)
                        }
                        .cache(dc)
                }
            }
        }
    }

    override fun onDestroyView() {
        dc.drain()
        planSectionUI = null
        super.onDestroyView()
    }


    private fun Date.toMonth(): String {
        return monthFormatter.format(this)
    }

}