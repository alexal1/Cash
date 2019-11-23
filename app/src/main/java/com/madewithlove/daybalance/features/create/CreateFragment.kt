/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.widget.textChanges
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.main.MainFragment
import com.madewithlove.daybalance.features.main.MainViewModel
import com.madewithlove.daybalance.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

class CreateFragment : Fragment() {

    companion object {

        const val KEYPAD_ANIMATION_DURATION = 200L

        private const val TYPE = "type"


        fun create(type: CreateViewModel.Type): CreateFragment = CreateFragment().apply {
            arguments = bundleOf(TYPE to type)
        }

    }


    private val mainViewModel by sharedViewModel<MainViewModel>(from = { parentFragment!! })
    private val initialType by lazy { arguments!!.getSerializable(TYPE) as CreateViewModel.Type }
    private val viewModel by viewModel<CreateViewModel> { parametersOf(initialType) }
    private val dateLossFormatter  by lazy { SimpleDateFormat("d MMM", ctx.currentLocale()) }
    private val dateGainFormatter by lazy { SimpleDateFormat("LLLL", ctx.currentLocale()) }
    private val ui: CreateUI get() = createUI ?: CreateUI().also { createUI = it }
    private val dc = DisposableCache()

    private var createUI: CreateUI? = null
    private var animator: Animator? = null
    private var monthPickerDialog: AlertDialog? = null


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
            setOnClickListener {
                viewModel.switchType()
            }

            viewModel.createStateObservable
                .map { it.type }
                .distinctUntilChanged()
                .subscribeOnUi { type ->
                    if (type == CreateViewModel.Type.LOSS) {
                        textResource = R.string.create_loss
                        backgroundResource = R.drawable.bg_loss
                    } else {
                        textResource = R.string.create_gain
                        backgroundResource = R.drawable.bg_gain
                    }
                }
                .cache(dc)
        }

        ui.datePicker.apply {
            viewModel.createStateObservable
                .map {
                    if (it.type == CreateViewModel.Type.LOSS) {
                        it.type to it.lossDate
                    } else {
                        it.type to it.gainAvailableMonths[it.gainChosenMonth]
                    }
                }
                .distinctUntilChanged()
                .subscribeOnUi { (type, date) ->
                    val formattedDate = when (type) {
                        CreateViewModel.Type.LOSS -> dateLossFormatter.format(date)
                        CreateViewModel.Type.GAIN -> dateGainFormatter.format(date)
                    }

                    text = formattedDate
                }
                .cache(dc)

            setOnClickListener {
                if (viewModel.createState.type == CreateViewModel.Type.LOSS) {
                    mainViewModel.showCalendar()
                } else {
                    openMonthPickerDialog(viewModel.createState.gainAvailableMonths, viewModel.createState.gainChosenMonth)
                }
            }
        }

        ui.inputIcon.apply {
            viewModel.createStateObservable
                .map { it.inputValidation }
                .distinctUntilChanged()
                .filter { it == CreateViewModel.InputValidation.ERROR }
                .subscribeOnUi {
                    val animation = AnimationUtils.loadAnimation(ctx, R.anim.shake)
                    startAnimation(animation)
                }
                .cache(dc)
        }

        ui.miniTextView.apply {
            setOnClickListener {
                if (text.isNotEmpty()) {
                    if (mainViewModel.mainState.isKeyboardOpened) {
                        ui.commentEditText.hideKeyboard()
                    } else {
                        mainViewModel.openKeyboard()
                    }
                }
            }

            mainViewModel.mainStateObservable
                .map { it.isKeyboardOpened }
                .distinctUntilChanged()
                .subscribeOnUi { isKeyboardOpened ->
                    text = if (isKeyboardOpened) {
                        viewModel.createState.amountString
                    } else {
                        viewModel.createState.comment
                    }
                }
                .cache(dc)
        }

        ui.inputTextView.apply {
            mainViewModel.mainStateObservable
                .map { it.isKeyboardOpened }
                .distinctUntilChanged()
                .subscribeOnUi { isKeyboardOpened ->
                    isVisible = !isKeyboardOpened
                }
                .cache(dc)

            viewModel.createStateObservable
                .map { it.amountString }
                .distinctUntilChanged()
                .subscribeOnUi { textAmount ->
                    if (textAmount.isNotEmpty()) {
                        text = textAmount
                        textColorResource = R.color.white
                    } else {
                        @SuppressLint("SetTextI18n")
                        text = "0.00"
                        textColorResource = R.color.palladium
                    }
                }
                .cache(dc)
        }

        ui.commentEditText.apply {
            textChanges().subscribe(viewModel.commentTextConsumer).cache(dc)

            mainViewModel.mainStateObservable
                .map { it.isKeyboardOpened }
                .distinctUntilChanged()
                .subscribeOnUi { isKeyboardOpened ->
                    isVisible = isKeyboardOpened
                }
                .cache(dc)
        }

        ui.keypadView.apply {
            actionsObservable.subscribe(viewModel.keypadActionsConsumer).cache(dc)

            mainViewModel.mainStateObservable
                .map { it.isKeyboardOpened }
                .distinctUntilChanged()
                .subscribeOnUi { isKeyboradOpened ->
                    if (isKeyboradOpened) {
                        hideKeypad()
                    } else {
                        showKeypad()
                    }
                }
                .cache(dc)
        }

        viewModel.createStateObservable
            .map { it.inputValidation }
            .ofType(CreateViewModel.InputValidation.OK::class.java)
            .map { it.transaction }
            .subscribeOnUi {
                mainViewModel.saveTransaction(it)
                act.onBackPressed()
            }
            .cache(dc)

        view.post {
            startPostponedEnterTransition()
            mainViewModel.notifyCreateOpened()
        }
    }

    override fun onDestroyView() {
        animator?.cancel()
        monthPickerDialog?.dismiss()
        dc.drain()
        ui.commentEditText.hideKeyboard()
        createUI = null
        super.onDestroyView()

        mainViewModel.notifyCreateClosed()
    }


    private fun hideKeypad() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(
            ui.keypadView.marginTop,
            ui.keypadView.height
        ).apply {
            duration = KEYPAD_ANIMATION_DURATION

            addUpdateListener {
                val lp = ui.keypadView.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = animatedValue as Int
                ui.keypadView.layoutParams = lp
            }

            view?.postDelayed({
                ui.commentEditText.showKeyboard()
            }, KEYPAD_ANIMATION_DURATION + MainFragment.LARGE_BUTTON_ANIMATION_DURATION)

            start()
        }
    }

    private fun showKeypad() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(
            ui.keypadView.marginTop,
            0
        ).apply {
            startDelay = MainFragment.LARGE_BUTTON_ANIMATION_DURATION
            duration = KEYPAD_ANIMATION_DURATION

            addUpdateListener {
                val lp = ui.keypadView.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = animatedValue as Int
                ui.keypadView.layoutParams = lp
            }

            start()
        }
    }

    private fun openMonthPickerDialog(availableMonths: List<Date>, chosenMonth: Int) {
        monthPickerDialog?.dismiss()

        val customTitle = ctx.linearLayout {
            orientation = LinearLayout.VERTICAL

            textView {
                textColorResource = R.color.white
                textSize = 16f
                textResource = R.string.create_gain_chosen_month_title
            }.lparams(matchParent, wrapContent) {
                bottomMargin = dip(8)
            }

            textView {
                textColorResource = R.color.white_80
                textSize = 14f
                textResource = R.string.create_gain_chosen_month_description
            }.lparams(matchParent, wrapContent)

            setPadding(dip(16), dip(16), dip(16), dip(16))
        }

        monthPickerDialog = AlertDialog.Builder(ctx)
            .setCustomTitle(customTitle)
            .setSingleChoiceItems(
                availableMonths.map { dateGainFormatter.format(it) }.toTypedArray(),
                chosenMonth
            ) { _, index -> viewModel.setGainChosenMonth(index) }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}