/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import com.madewithlove.daybalance.BaseViewModel
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.create.CreateFragment
import com.madewithlove.daybalance.features.create.CreateViewModel
import com.madewithlove.daybalance.features.moneybox.MoneyboxFragment
import com.madewithlove.daybalance.features.plan.PlanFragment
import com.madewithlove.daybalance.features.settings.SettingsFragment
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.navigation.FragmentNavigator
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textResource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class MainFragment : FragmentNavigator() {

    companion object {

        const val LARGE_BUTTON_ANIMATION_DURATION = 100L


        fun create(): MainFragment = MainFragment()

    }


    private val baseViewModel: BaseViewModel by sharedViewModel()
    private val viewModel: MainViewModel by viewModel()
    private val datesManager: DatesManager by inject()
    private val analytics: Analytics by inject()
    private val calendarDialog by lazy { createCalendarDialog() }
    private val calendar = GregorianCalendar.getInstance()
    private val ui: MainUI get() = mainUI ?: MainUI().also { mainUI = it }
    private val dc = DisposableCache()

    private var mainUI: MainUI? = null
    private var animator: Animator? = null


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

        ui.datesRecyclerView.apply {
            init(ctx.currentLocale())
            centerItemClickObservable.subscribeOnUi { viewModel.showCalendar() }.cache(dc)

            val lastDate = AtomicLong()

            dateObservable.subscribe { scrolledDate ->
                lastDate.set(scrolledDate.time)
                datesManager.updateCurrentDate(scrolledDate)
            }.cache(dc)

            viewModel.mainStateObservable
                .map { it.currentDate }
                .distinctUntilChanged()
                .filter { currentDate -> currentDate.time != lastDate.get() }
                .subscribeOnUi { currentDate ->
                    setDate(currentDate)
                }
                .cache(dc)

            goPrevSubject.subscribeOnUi { action ->
                when (action) {
                    ACTION_DOWN -> ui.prevButton.alpha = 1.0f
                    ACTION_CANCEL -> ui.prevButton.alpha = 0.8f
                    ACTION_UP -> {
                        ui.prevButton.alpha = 0.8f
                        swipePrev()
                    }
                }
            }.cache(dc)

            goNextSubject.subscribeOnUi { action ->
                when (action) {
                    ACTION_DOWN -> ui.nextButton.alpha = 1.0f
                    ACTION_CANCEL -> ui.nextButton.alpha = 0.8f
                    ACTION_UP -> {
                        ui.nextButton.alpha = 0.8f
                        swipeNext()
                    }
                }
            }.cache(dc)
        }

        ui.weekdayText.apply {
            viewModel.mainStateObservable
                .map { it.weekday to it.isToday }
                .distinctUntilChanged()
                .subscribeOnUi { (weekday, isToday) ->
                    text = if (isToday) {
                        "$weekday (${string(R.string.today)})"
                    } else {
                        weekday
                    }
                }
                .cache(dc)
        }

        ui.settingsButton.apply {
            setOnClickListener {
                val fragment = SettingsFragment.create()
                addFragment(fragment)
            }
        }

        ui.moneyboxButton.apply {
            setOnClickListener {
                val fragment = MoneyboxFragment.create()
                addFragment(fragment)
            }
        }

        ui.circleView.apply {
            viewModel.mainStateObservable
                .map { it.circleState }
                .distinctUntilChanged()
                .subscribeOnUi(this::setData)
                .cache(dc)
        }

        ui.monthPlanButton.apply {
            setOnClickListener {
                val fragment = PlanFragment.create()
                addFragment(fragment)
            }
        }

        ui.gainButton.apply {
            setOnClickListener {
                val fragment = CreateFragment.create(CreateViewModel.Type.GAIN)
                addFragment(fragment)
            }
        }

        ui.lossButton.apply {
            setOnClickListener {
                val fragment = CreateFragment.create(CreateViewModel.Type.LOSS)
                addFragment(fragment)
            }
        }

        ui.largeButtonBackground.apply {
            viewModel.mainStateObservable
                .map { it.isKeyboardOpened }
                .distinctUntilChanged()
                .subscribeOnUi { isKeyboardOpened ->
                    if (isKeyboardOpened) {
                        hideLargeButton()
                    } else {
                        showLargeButton()
                    }
                }
                .cache(dc)

            setOnClickListener {
                when (viewModel.mainState.largeButtonType) {
                    MainViewModel.LargeButtonType.HISTORY -> {
                        baseViewModel.openHistorySubject.onNext(HistorySpecification.Empty)
                    }

                    MainViewModel.LargeButtonType.KEYBOARD -> {
                        viewModel.openKeyboard()
                    }

                    MainViewModel.LargeButtonType.PLAN_GAIN -> {
                        val currentMonthFirstDay = datesManager.getCurrentMonthFirstDay()
                        baseViewModel.openHistorySubject.onNext(HistorySpecification.MonthTotalGainFilter(currentMonthFirstDay))
                    }

                    MainViewModel.LargeButtonType.PLAN_LOSS -> {
                        val currentMonthFirstDay = datesManager.getCurrentMonthFirstDay()
                        baseViewModel.openHistorySubject.onNext(HistorySpecification.MonthMandatoryLossFilter(currentMonthFirstDay))
                    }

                    MainViewModel.LargeButtonType.PLAN_MONEYBOX -> {
                        val fragment = MoneyboxFragment.create()
                        addFragment(fragment)
                    }

                    MainViewModel.LargeButtonType.MONEYBOX -> {
                        val fragment = CreateFragment.create(CreateViewModel.Type.INTO_MONEYBOX)
                        addFragment(fragment)
                    }

                    MainViewModel.LargeButtonType.SETTINGS -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=com.madewithlove.daybalance")
                            setPackage("com.android.vending")
                        }

                        if (intent.resolveActivity(ctx.packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                }
            }
        }

        ui.largeButtonText.apply {
            viewModel.mainStateObservable
                .map { it.largeButtonType }
                .distinctUntilChanged()
                .subscribeOnUi { largeButtonType ->
                    when (largeButtonType!!) {
                        MainViewModel.LargeButtonType.HISTORY -> {
                            textResource = R.string.large_button_history
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0)
                        }

                        MainViewModel.LargeButtonType.KEYBOARD -> {
                            textResource = R.string.large_button_keyboard
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard, 0)
                        }

                        MainViewModel.LargeButtonType.PLAN_GAIN -> {
                            textResource = R.string.large_button_plan_gain
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0)
                        }

                        MainViewModel.LargeButtonType.PLAN_LOSS -> {
                            textResource = R.string.large_button_plan_loss
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0)
                        }

                        MainViewModel.LargeButtonType.PLAN_MONEYBOX -> {
                            textResource = R.string.large_button_plan_moneybox
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyhole_small, 0)
                        }

                        MainViewModel.LargeButtonType.MONEYBOX -> {
                            textResource = R.string.large_button_moneybox
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_coin_stack, 0)
                        }

                        MainViewModel.LargeButtonType.SETTINGS -> {
                            textResource = R.string.large_button_settings
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_star, 0)
                        }
                    }
                }
                .cache(dc)
        }

        act.keyboardListener().subscribe { isVisible ->
            if (isVisible) {
                viewModel.openKeyboard()
            } else {
                viewModel.notifyKeyboardClosed()
            }
        }.cache(dc)

        viewModel.showCalendarObservable.subscribeOnUi {
            openCalendarDialog()
        }.cache(dc)

        view.post {
            startPostponedEnterTransition()
        }
    }

    override fun getNavigatorFragmentManager() = childFragmentManager

    override fun getFragmentContainerId() = R.id.main_container

    override fun onDestroyView() {
        animator?.cancel()
        dc.drain()
        mainUI = null
        super.onDestroyView()
    }


    private fun createCalendarDialog(): DatePickerDialog {
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            val date = calendar.time
            analytics.pickCalendarDate()
            datesManager.updateCurrentDate(date)
        }

        return DatePickerDialog(ctx, listener, 1970, 0, 1)
    }

    private fun openCalendarDialog() {
        calendar.time = datesManager.currentDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        calendarDialog.updateDate(year, month, day)
        calendarDialog.show()
    }

    private fun hideLargeButton() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(
            ui.largeButtonBackground.marginTop,
            ui.largeButtonBackground.height
        ).apply {
            startDelay = CreateFragment.KEYPAD_ANIMATION_DURATION
            duration = LARGE_BUTTON_ANIMATION_DURATION

            addUpdateListener {
                val lp = ui.largeButtonBackground.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = animatedValue as Int
                ui.largeButtonBackground.layoutParams = lp
            }

            start()
        }
    }

    private fun showLargeButton() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(
            ui.largeButtonBackground.marginTop,
            0
        ).apply {
            duration = LARGE_BUTTON_ANIMATION_DURATION

            addUpdateListener {
                val lp = ui.largeButtonBackground.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = animatedValue as Int
                ui.largeButtonBackground.layoutParams = lp
            }

            start()
        }
    }

}