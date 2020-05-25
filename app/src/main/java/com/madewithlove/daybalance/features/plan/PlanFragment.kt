/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.plan

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.create.CreateFragment
import com.madewithlove.daybalance.features.create.CreateViewModel
import com.madewithlove.daybalance.features.history.HistoryViewModel
import com.madewithlove.daybalance.features.main.MainViewModel
import com.madewithlove.daybalance.helpers.ShowcaseManager
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.ui.PercentagePicker
import com.madewithlove.daybalance.ui.ScreenFragment
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.anko.percentagePicker
import com.madewithlove.daybalance.utils.navigation.BackStackListener
import com.madewithlove.daybalance.utils.navigation.Navigator
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class PlanFragment : ScreenFragment("plan"), BackStackListener {

    companion object {

        fun create(): PlanFragment = PlanFragment()

    }


    private val historyViewModel by sharedViewModel<HistoryViewModel>()
    private val mainViewModel by sharedViewModel<MainViewModel>(from = { requireParentFragment() })
    private val viewModel by viewModel<PlanViewModel>()
    private val showcaseManager: ShowcaseManager by inject()
    private val navigator by lazy { parentFragment as Navigator }
    private val monthFormatter by lazy { SimpleDateFormat("LLLL", ctx.currentLocale()) }
    private val ui: PlanUI get() = planUI ?: PlanUI().also { planUI = it }
    private val dc = DisposableCache()

    private var planUI: PlanUI? = null
    private var percentagePickerDialog: AlertDialog? = null


    fun scrollTo(section: PlanViewModel.Section) {
        ui.viewPager.currentItem = section.ordinal
    }


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

        ui.titleText.apply {
            val month = viewModel.planState.currentDate.toMonth()
            text = string(R.string.plan_title, "{month}" to month)
        }

        ui.tabLayout.setupWithViewPager(ui.viewPager)

        ui.viewPager.apply {
            adapter = ViewPagerAdapter(childFragmentManager)

            val lastSectionIndex = AtomicInteger()

            viewModel.planStateObservable
                .map { it.currentSection.ordinal }
                .distinctUntilChanged()
                .filter { sectionIndex -> sectionIndex != lastSectionIndex.get() }
                .subscribeOnUi {
                    currentItem = it
                }
                .cache(dc)

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    lastSectionIndex.set(position)
                    val section = PlanViewModel.Section.values()[position]
                    viewModel.setSection(section)

                    if (navigator.isFragmentOnTop(this@PlanFragment)) {
                        mainViewModel.notifyPlanOpened(PlanViewModel.Section.values()[position])
                        historyViewModel.setFilter(viewModel.planState.historyFilter)
                    }
                }

            })
        }

        ui.floatingActionButton.apply {
            viewModel.planStateObservable
                .map { it.currentSection }
                .distinctUntilChanged()
                .subscribeOnUi { section ->
                    when (section!!) {
                        PlanViewModel.Section.GAIN -> {
                            backgroundTintList = ColorStateList.valueOf(color(R.color.green_80))
                            setImageResource(R.drawable.ic_plus)
                        }

                        PlanViewModel.Section.LOSS -> {
                            backgroundTintList = ColorStateList.valueOf(color(R.color.red_80))
                            setImageResource(R.drawable.ic_plus)
                        }

                        PlanViewModel.Section.MONEYBOX -> {
                            backgroundTintList = ColorStateList.valueOf(color(R.color.blue_80))
                            setImageResource(R.drawable.ic_gear)
                        }
                    }
                }
                .cache(dc)

            setOnClickListenerWithThrottle {
                when (viewModel.planState.currentSection) {
                    PlanViewModel.Section.GAIN -> {
                        val createFragment = CreateFragment.create(CreateViewModel.Type.GAIN, 0)
                        (parentFragment as Navigator).addFragment(createFragment)
                    }

                    PlanViewModel.Section.LOSS -> {
                        val createFragment = CreateFragment.create(CreateViewModel.Type.MANDATORY_LOSS, 0)
                        (parentFragment as Navigator).addFragment(createFragment)
                    }

                    PlanViewModel.Section.MONEYBOX -> {
                        openPercentagePickerDialog()
                        showcaseManager.dispose()
                    }
                }
            }.cache(dc)

            viewModel.amountClickSubject.subscribeOnUi {
                performClick()
            }.cache(dc)
        }

        view.post {
            startPostponedEnterTransition()
            viewModel.requestData()

            if (navigator.isFragmentOnTop(this@PlanFragment)) {
                mainViewModel.notifyPlanOpened(viewModel.planState.currentSection)
                historyViewModel.setFilter(viewModel.planState.historyFilter)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showcaseManager.show(this)
    }

    override fun onResumedFromBackStack() {
        viewModel.requestData()
        mainViewModel.notifyPlanOpened(viewModel.planState.currentSection)
        historyViewModel.setFilter(viewModel.planState.historyFilter)
        showcaseManager.show(this)
    }

    override fun onPause() {
        super.onPause()
        showcaseManager.dispose()
    }

    override fun onDestroyView() {
        ui.viewPager.clearOnPageChangeListeners()
        dc.drain()
        planUI = null
        percentagePickerDialog?.dismiss()
        percentagePickerDialog?.setOnDismissListener(null)
        percentagePickerDialog = null
        super.onDestroyView()

        mainViewModel.notifyPlanClosed()
        historyViewModel.setFilter(HistorySpecification.Empty)
    }


    private fun Date.toMonth(): String {
        return monthFormatter.format(this)
    }

    private fun createPercentagePickerDialog(): AlertDialog {
        var currentItemPos = -1
        var percentagePicker: PercentagePicker? = null

        val customTitle = ctx.linearLayout {
            orientation = VERTICAL

            textView {
                textColorResource = R.color.white
                textSize = 16f
                textResource = R.string.plan_section_moneybox_percentage_picker_title
            }.lparams(matchParent, wrapContent) {
                bottomMargin = dip(32)
            }

            percentagePicker = percentagePicker {
                viewModel.planStateObservable
                    .mapNotNull { it.savingsRatio }
                    .subscribeOnUi { savingsRatio ->
                        setData(savingsRatio)
                    }
                    .cache(dc)

                itemPickedObservable.subscribeOnUi { itemPos ->
                    currentItemPos = itemPos
                }.cache(dc)
            }.lparams(matchParent, wrapContent)

            setPadding(dip(16), dip(16), dip(16), dip(16))
        }

        return AlertDialog.Builder(ctx)
            .setCustomTitle(customTitle)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                val ratio = (currentItemPos * 5) * 0.01f
                viewModel.setSavingsRatio(ratio)
                dialog.dismiss()
            }
            .setOnDismissListener {
                val ratio = viewModel.planState.savingsRatio ?: return@setOnDismissListener
                percentagePicker?.setData(ratio)
            }
            .create()
    }

    private fun openPercentagePickerDialog() {
        val percentagePickerDialog = percentagePickerDialog ?: createPercentagePickerDialog().also {
            this.percentagePickerDialog = it
        }

        percentagePickerDialog.show()
    }


    private inner class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getItem(position: Int) = when (position) {
            0 -> PlanSectionFragment.create(PlanViewModel.Section.GAIN)
            1 -> PlanSectionFragment.create(PlanViewModel.Section.LOSS)
            2 -> PlanSectionFragment.create(PlanViewModel.Section.MONEYBOX)
            else -> throw IllegalArgumentException()
        }

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            0 -> ctx.string(R.string.plan_section_gain_title)
            1 -> ctx.string(R.string.plan_section_loss_title)
            2 -> ctx.string(R.string.plan_section_moneybox_title)
            else -> throw IllegalArgumentException()
        }

        override fun getCount() = 3

    }

}