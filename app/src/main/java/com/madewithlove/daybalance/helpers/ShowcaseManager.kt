/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.helpers

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import androidx.core.content.edit
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.CashApp.Companion.PREFS_SHOWCASE_STEP
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.main.MainFragment
import com.madewithlove.daybalance.features.plan.PlanFragment
import com.madewithlove.daybalance.features.plan.PlanViewModel
import com.madewithlove.daybalance.repository.TransactionsRepository
import com.madewithlove.daybalance.repository.specifications.MonthTotalGainSpecification
import com.madewithlove.daybalance.ui.ShowcaseOverlayView
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.currentLocale
import com.madewithlove.daybalance.utils.navigation.isOnTop
import com.madewithlove.daybalance.utils.subscribeOnUi
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.shape.Shape
import io.reactivex.Single
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat

class ShowcaseManager(
    private val context: Context,
    private val datesManager: DatesManager,
    private val sharedPreferences: SharedPreferences,
    private val repository: TransactionsRepository
) {

    companion object {

        private const val DELAY_BEFORE_SHOWCASE = 400L
        private const val ANIMATION_DURATION = 400L

        private val defaultStep = Step.WE_NEED_A_PLAN

    }


    private val monthFormatter by lazy { SimpleDateFormat("LLLL", context.currentLocale()) }
    private val handler = Handler(Looper.getMainLooper())
    private val dc = DisposableCache()

    private var currentSpotlight: Spotlight? = null
    private var step: Step = defaultStep
        set(value) {
            field = value
            sharedPreferences.edit {
                putString(PREFS_SHOWCASE_STEP, value.name)
            }
        }


    init {
        step = sharedPreferences.getString(PREFS_SHOWCASE_STEP, null)
            ?.let(Step::valueOf)
            ?: defaultStep
    }


    fun show(fragment: MainFragment) {
        when (step) {
            Step.WE_NEED_A_PLAN -> {
                show(fragment, step)
            }

            Step.ADD_LOSS -> {
                show(fragment, step) {
                    step = Step.FINISHED
                }
            }

            else -> { /* do nothing */ }
        }
    }

    fun show(fragment: PlanFragment) {
        when (step) {
            Step.WE_NEED_A_PLAN -> {
                step = Step.ADD_GAIN
                show(fragment)
            }

            Step.ADD_GAIN -> {
                isGainEntered().subscribeOnUi { isGainEntered ->
                    if (isGainEntered) {
                        step = Step.HOW_MUCH_TO_SAVE
                        show(fragment)
                    } else {
                        show(fragment, step) {
                            fragment.scrollTo(PlanViewModel.Section.GAIN)
                        }
                    }
                }.cache(dc)
            }

            Step.HOW_MUCH_TO_SAVE -> {
                show(fragment, step) {
                    fragment.scrollTo(PlanViewModel.Section.MONEYBOX)
                    step = Step.ADD_LOSS
                }
            }

            else -> { /* do nothing */ }
        }
    }

    fun reset() {
        step = Step.WE_NEED_A_PLAN
    }

    fun dispose(): Boolean {
        dc.drain()
        handler.removeCallbacksAndMessages(null)

        if (currentSpotlight != null) {
            currentSpotlight?.finish()
            currentSpotlight = null
            return true
        }

        return false
    }


    private fun show(fragment: Fragment, step: Step, doAfterShown: (() -> Unit)? = null) {
        if (!fragment.isOnTop()) {
            return
        }

        dispose()

        val targetViewId: Int = when (step) {
            Step.WE_NEED_A_PLAN -> {
                R.id.top_button_plan
            }

            Step.ADD_GAIN -> {
                R.id.floating_action_button
            }

            Step.HOW_MUCH_TO_SAVE -> {
                R.id.floating_action_button
            }

            Step.ADD_LOSS -> {
                R.id.loss_button
            }

            Step.FINISHED -> throw IllegalStateException("Why show() was invoked while step is FINISHED?")
        }

        val targetView = fragment.requireView().findViewById<View>(targetViewId)
        targetView.delay {
            if (!fragment.isOnTop()) {
                return@delay
            }

            val shape: Shape
            val title: String
            val description: String
            val overlayGravity: Int
            when (step) {
                Step.WE_NEED_A_PLAN -> {
                    val month = monthFormatter.format(datesManager.currentDate)

                    shape = RoundedRectangle(
                        targetView.height.toFloat(),
                        targetView.width - context.dip(8).toFloat(),
                        context.dip(8).toFloat()
                    )
                    title = context.getString(R.string.showcase_we_need_a_plan_title)
                    description = context.getString(R.string.showcase_we_need_a_plan_description, month)
                    overlayGravity = Gravity.BOTTOM
                }

                Step.ADD_GAIN -> {
                    val month = monthFormatter.format(datesManager.currentDate)

                    shape = Circle(context.dimen(R.dimen.floating_action_button_size) / 2f + context.dip(16))
                    title = context.getString(R.string.showcase_add_gain_title, month)
                    description = context.getString(R.string.showcase_add_gain_description, month)
                    overlayGravity = Gravity.TOP
                }

                Step.HOW_MUCH_TO_SAVE -> {
                    val month = monthFormatter.format(datesManager.currentDate)

                    shape = Circle(context.dimen(R.dimen.floating_action_button_size) / 2f + context.dip(16))
                    title = context.getString(R.string.showcase_how_much_to_save_title)
                    description = context.getString(R.string.showcase_how_much_to_save_description, month)
                    overlayGravity = Gravity.TOP
                }

                Step.ADD_LOSS -> {
                    shape = RoundedRectangle(
                        targetView.height.toFloat(),
                        targetView.width.toFloat(),
                        context.dip(8).toFloat()
                    )
                    title = context.getString(R.string.showcase_add_loss_title)
                    description = context.getString(R.string.showcase_add_loss_description)
                    overlayGravity = Gravity.TOP
                }

                Step.FINISHED -> throw IllegalStateException("Why show() was invoked while step is FINISHED?")
            }

            val viewRect = Rect().also { targetView.getGlobalVisibleRect(it) }

            val overlay = ShowcaseOverlayView(fragment.ctx).init(overlayGravity).apply {
                holeRect = viewRect
                onCrossClick = { dispose() }

                setTitle(title)
                setDescription(description)
            }

            val target = Target.Builder()
                .setAnchor(targetView)
                .setOverlay(overlay)
                .setShape(shape)
                .build()

            currentSpotlight = Spotlight.Builder(fragment.act)
                .setTargets(target)
                .setDuration(ANIMATION_DURATION)
                .build()
                .also { it.start() }

            doAfterShown?.invoke()
        }
    }

    private fun View.delay(action: () -> Unit) {
        doOnPreDraw {
            handler.postDelayed(action, DELAY_BEFORE_SHOWCASE)
        }
    }

    private fun isGainEntered(): Single<Boolean> {
        val currentMonthFirstDay = datesManager.getCurrentMonthFirstDay()
        return repository
            .query(MonthTotalGainSpecification(currentMonthFirstDay))
            .map { monthTotalGain -> monthTotalGain.toLong() > 0 }
    }


    enum class Step {
        WE_NEED_A_PLAN,
        ADD_GAIN,
        HOW_MUCH_TO_SAVE,
        ADD_LOSS,
        FINISHED
    }

}