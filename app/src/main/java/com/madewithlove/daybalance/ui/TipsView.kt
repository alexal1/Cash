/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.TipsManager
import com.madewithlove.daybalance.helpers.enums.Periods
import com.madewithlove.daybalance.utils.replace
import com.madewithlove.daybalance.utils.setSelectableBackground
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class TipsView(context: Context) : _ConstraintLayout(context), KoinComponent {

    val closeClick: Observable<TipsManager.Tip>

    private val gainClickSubject = PublishSubject.create<Unit>()
    val gainClick: Observable<Unit> = gainClickSubject.throttleFirst(1, TimeUnit.SECONDS)

    private val lossClickSubject = PublishSubject.create<Unit>()
    val lossClick: Observable<Unit> = lossClickSubject.throttleFirst(1, TimeUnit.SECONDS)

    private val currencyManager: CurrencyManager by inject()

    private var currentTip: TipsManager.Tip? = null
    private var text: TextView


    init {
        val backgroundView = view {
            id = View.generateViewId()
            backgroundResource = R.drawable.bg_tip
        }.lparams(matchConstraint, matchConstraint)

        val icon = imageView {
            id = View.generateViewId()
            backgroundColor = Color.TRANSPARENT
            imageResource = R.drawable.ic_bulb
        }.lparams(wrapContent, wrapContent) {
            topMargin = dimen(R.dimen.tip_padding)
            bottomMargin = dimen(R.dimen.tip_padding)
            leftMargin = dimen(R.dimen.tip_padding)
        }

        text = textView {
            id = View.generateViewId()
            textSize = 12f
            textColorResource = R.color.white_80
            movementMethod = LinkMovementMethod.getInstance()
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dimen(R.dimen.tip_padding)
            bottomMargin = dimen(R.dimen.tip_padding)
            leftMargin = dimen(R.dimen.tip_padding)
            rightMargin = dimen(R.dimen.tip_padding)
        }

        val separator = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.white_60
        }.lparams(dip(1), matchConstraint)

        val cross = imageButton {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_cross_mini)

            setSelectableBackground()
        }.lparams(dip(32) + dimen(R.dimen.tip_padding) * 2, matchConstraint).also {
            closeClick = it.clicks().map { currentTip }
        }

        applyConstraintSet {
            connect(
                START of backgroundView to START of PARENT_ID,
                END of backgroundView to END of PARENT_ID,
                TOP of backgroundView to TOP of PARENT_ID,
                BOTTOM of backgroundView to BOTTOM of PARENT_ID
            )

            connect(
                START of icon to START of PARENT_ID,
                TOP of icon to TOP of PARENT_ID,
                BOTTOM of icon to BOTTOM of PARENT_ID
            )

            connect(
                START of text to END of icon,
                END of text to START of separator,
                TOP of text to TOP of PARENT_ID,
                BOTTOM of text to BOTTOM of PARENT_ID
            )

            connect(
                END of separator to START of cross,
                TOP of separator to TOP of PARENT_ID,
                BOTTOM of separator to BOTTOM of PARENT_ID
            )

            connect(
                END of cross to END of PARENT_ID,
                TOP of cross to TOP of PARENT_ID,
                BOTTOM of cross to BOTTOM of PARENT_ID
            )
        }
    }


    fun setData(tip: TipsManager.Tip) {
        currentTip = tip

        when (tip) {
            is TipsManager.Tip.Empty -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_empty))
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
            }

            is TipsManager.Tip.SingleLossOnThisDayOneDayLong -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_single_loss_on_this_day_one_day_long))
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{value}",
                        currencyManager.formatMoney(tip.amount)
                    )
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                gainClickSubject.onNext(Unit)
                            }

                            override fun updateDrawState(ds: TextPaint) {}
                        }
                    )
            }

            is TipsManager.Tip.SingleGainOnThisDayOneDayLong -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_single_gain_on_this_day_one_day_long))
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{value}",
                        currencyManager.formatMoney(tip.amount)
                    )
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                lossClickSubject.onNext(Unit)
                            }

                            override fun updateDrawState(ds: TextPaint) {}
                        }
                    )
            }

            is TipsManager.Tip.SingleLossOnThisDayManyDaysLong -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_single_loss_on_this_day_many_days_long))
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{period}",
                        context.getString(Periods.getByName(tip.transaction.period).fullString)
                    )
                    .replace(
                        "{amount}",
                        currencyManager.formatMoney(abs(tip.transaction.amount))
                    )
                    .replace(
                        "{period}",
                        context.getString(Periods.getByName(tip.transaction.period).shortString)
                    )
                    .replace(
                        "{amount_per_day}",
                        currencyManager.formatMoney(tip.transaction.getAmountPerDay()),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                gainClickSubject.onNext(Unit)
                            }

                            override fun updateDrawState(ds: TextPaint) {}
                        }
                    )
            }

            is TipsManager.Tip.SingleGainOnThisDayManyDaysLong -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_single_gain_on_this_day_many_days_long))
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{period}",
                        context.getString(Periods.getByName(tip.transaction.period).fullString)
                    )
                    .replace(
                        "{amount}",
                        currencyManager.formatMoney(abs(tip.transaction.amount))
                    )
                    .replace(
                        "{period}",
                        context.getString(Periods.getByName(tip.transaction.period).shortString)
                    )
                    .replace(
                        "{amount_per_day}",
                        currencyManager.formatMoney(tip.transaction.getAmountPerDay()),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                lossClickSubject.onNext(Unit)
                            }

                            override fun updateDrawState(ds: TextPaint) {}
                        }
                    )
            }

            is TipsManager.Tip.LossesFromOtherDaysOnly -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_losses_from_other_days_only))
                    .replace(
                        "{value}",
                        currencyManager.formatMoney(tip.sum),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{gain}",
                        context.getString(R.string.gain_insertion),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                gainClickSubject.onNext(Unit)
                            }

                            override fun updateDrawState(ds: TextPaint) {}
                        }
                    )
            }

            is TipsManager.Tip.GainsFromOtherDaysOnly -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_losses_from_other_days_only))
                    .replace(
                        "{value}",
                        currencyManager.formatMoney(tip.sum),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{loss}",
                        context.getString(R.string.loss_insertion),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                lossClickSubject.onNext(Unit)
                            }

                            override fun updateDrawState(ds: TextPaint) {}
                        }
                    )
            }

            is TipsManager.Tip.LossesAndGainsPositiveResult -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_losses_and_gains_positive_result))
                    .replace(
                        "{gains_value}",
                        currencyManager.formatMoney(tip.gainsSum),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{losses_value}",
                        currencyManager.formatMoney(tip.lossesSum),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{total_value}",
                        currencyManager.formatMoney(tip.totalSum),
                        StyleSpan(Typeface.BOLD),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.green))
                    )
            }

            is TipsManager.Tip.LossesAndGainsNegativeResult -> {
                text.text = SpannableStringBuilder(context.getString(R.string.tip_losses_and_gains_negative_result))
                    .replace(
                        "{gains_value}",
                        currencyManager.formatMoney(tip.gainsSum),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{losses_value}",
                        currencyManager.formatMoney(tip.lossesSum),
                        StyleSpan(Typeface.BOLD)
                    )
                    .replace(
                        "{total_value}",
                        currencyManager.formatMoney(tip.totalSum),
                        StyleSpan(Typeface.BOLD),
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.red))
                    )
            }
        }
    }

}