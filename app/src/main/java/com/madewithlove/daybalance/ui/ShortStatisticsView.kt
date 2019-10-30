/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.text.toSpannable
import androidx.core.widget.TextViewCompat
import com.github.iojjj.rcbs.RoundedCornersBackgroundSpan
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import com.madewithlove.daybalance.utils.color
import com.madewithlove.daybalance.utils.string
import com.madewithlove.daybalance.viewmodels.MainViewModelOld
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.math.abs

class ShortStatisticsView(context: Context) : _ConstraintLayout(context), KoinComponent {

    private val currencyManager: CurrencyManager by inject()
    private val statisticsText: TextView


    init {
        backgroundResource = R.drawable.bg_bottom_bar

        statisticsText = appCompatTextView {
            id = View.generateViewId()
            backgroundColor = Color.TRANSPARENT
            textColorResource = R.color.smoke
            textSize = 12f
            letterSpacing = 0.01f
            typeface = ResourcesCompat.getFont(context, R.font.currencies)
            maxLines = 1
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            typeface = ResourcesCompat.getFont(context, R.font.currencies)

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this@appCompatTextView,
                1,
                12,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }.lparams(matchConstraint, matchConstraint) {
            marginStart = dip(16)
            marginEnd = dip(16)
        }

        val showFullStatisticsButton = imageView {
            id = View.generateViewId()
            backgroundColor = Color.TRANSPARENT

            setImageResource(R.drawable.ic_expand)
        }.lparams(wrapContent, wrapContent) {
            bottomMargin = dip(6)
            topMargin = dip(2)
        }

        applyConstraintSet {
            connect(
                START of statisticsText to START of PARENT_ID,
                END of statisticsText to END of PARENT_ID,
                TOP of statisticsText to TOP of PARENT_ID,
                BOTTOM of statisticsText to TOP of showFullStatisticsButton
            )

            connect(
                START of showFullStatisticsButton to START of PARENT_ID,
                END of showFullStatisticsButton to END of PARENT_ID,
                TOP of showFullStatisticsButton to BOTTOM of statisticsText,
                BOTTOM of showFullStatisticsButton to BOTTOM of PARENT_ID
            )
        }
    }


    fun setData(shortStatistics: MainViewModelOld.ShortStatistics) {
        val (balance, month, monthDiff) = shortStatistics
        statisticsText.text = generateShortStatistics(balance, month, monthDiff)
    }


    private fun generateShortStatistics(balance: Double, month: String, monthDiff: Double): Spannable {
        val rawString = string(R.string.short_statistics)
        val spansList = LinkedList<StringBuilder>()

        val balanceTarget = "{balance}"
        val balanceStart = rawString.indexOf(balanceTarget)
        val balanceEnd = balanceStart + balanceTarget.count()

        val monthTarget = "{month}"
        val monthStart = rawString.indexOf(monthTarget)
        val monthEnd = monthStart + monthTarget.count()

        val monthDiffTarget = "{month_diff}"
        val monthDiffStart = rawString.indexOf(monthDiffTarget)
        val monthDiffEnd = monthDiffStart + monthDiffTarget.count()

        val boundIndices = listOf(0, balanceStart, balanceEnd, monthStart, monthEnd, monthDiffStart, monthDiffEnd)

        rawString.forEachIndexed { index, char ->
            if (boundIndices.contains(index)) {
                spansList.add(StringBuilder())
            }

            spansList.last().append(char)
        }

        val spannableBuilder = RoundedCornersBackgroundSpan.Builder(context)
            .setCornersRadius(dip(10).toFloat())
            .setTextPadding(dip(1).toFloat())
            .setTextAlignment(RoundedCornersBackgroundSpan.ALIGN_CENTER)

        while (spansList.isNotEmpty()) {
            when (val string = spansList.poll()!!.toString()) {
                balanceTarget -> {
                    val text = "\u00A0\u00A0\u00A0${currencyManager.formatMoney(balance)}\u00A0\u00A0\u00A0".toSpannable()
                    text.setSpan(
                        ForegroundColorSpan(color(R.color.white_65)),
                        0,
                        text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannableBuilder.addTextPart(text, color(R.color.palladium))
                }

                monthTarget -> {
                    spannableBuilder.addTextPart(month, Color.TRANSPARENT)
                }

                monthDiffTarget -> {
                    val text = "\u00A0\u00A0\u00A0${currencyManager.formatMoneyWithSign(monthDiff)}\u00A0\u00A0\u00A0".toSpannable()

                    val backgroundColor: Int
                    val foregroundColor: Int
                    when {
                        abs(monthDiff) < 0.01 -> {
                            backgroundColor = color(R.color.palladium)
                            foregroundColor = color(R.color.white_65)
                        }

                        monthDiff > 0 -> {
                            backgroundColor = ColorUtils.setAlphaComponent(color(R.color.green), (255 * 0.4).toInt())
                            foregroundColor = color(R.color.white_70)
                        }

                        else -> {
                            backgroundColor = ColorUtils.setAlphaComponent(color(R.color.red), (255 * 0.4).toInt())
                            foregroundColor = color(R.color.white_70)
                        }
                    }

                    text.setSpan(
                        ForegroundColorSpan(foregroundColor),
                        0,
                        text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannableBuilder.addTextPart(text, backgroundColor)
                }

                else -> {
                    spannableBuilder.addTextPart(string, Color.TRANSPARENT)
                }
            }
        }

        return spannableBuilder.build()
    }

}