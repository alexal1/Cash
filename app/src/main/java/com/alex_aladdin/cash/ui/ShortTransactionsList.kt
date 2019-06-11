package com.alex_aladdin.cash.ui

import android.content.Context
import android.graphics.Point
import android.view.Gravity.CENTER
import android.view.MotionEvent
import android.view.View
import android.widget.Space
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.graphics.contains
import androidx.core.view.isVisible
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.utils.anko.dashedLineView
import com.alex_aladdin.cash.utils.getRect
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class ShortTransactionsList(context: Context) : _ConstraintLayout(context), KoinComponent {

    private val currencyManager: CurrencyManager by inject()

    private val block0: Block
    private val block1: Block
    private val showAllView: View
    private val stubView: View


    init {
        block0 = getTransactionBlock(false)
        block1 = getTransactionBlock(true)

        showAllView = textView {
            id = View.generateViewId()
            textSize = 14f
            textColorResource = R.color.blue
            textResource = R.string.transactions_show_all
            alpha = 0.8f
        }.lparams(wrapContent, wrapContent)

        stubView = textView {
            id = View.generateViewId()
            backgroundColorResource = R.color.dark
            textSize = 14f
            textColorResource = R.color.palladium
            textResource = R.string.no_transactions_stub
            gravity = CENTER
        }.lparams(matchConstraint, matchConstraint)

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    showAllView.alpha = 1.0f
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    showAllView.alpha = 0.8f

                    if (getRect().contains(Point(event.x.toInt() + left, event.y.toInt() + top))) {
                        performClick()
                    }

                    true
                }

                else -> false
            }
        }

        applyConstraintSet {
            connect(
                START of block0.space to START of PARENT_ID,
                END of block0.space to END of PARENT_ID,
                TOP of block0.space to TOP of PARENT_ID,
                BOTTOM of block0.space to TOP of block1.space
            )

            connect(
                START of block1.space to START of PARENT_ID,
                END of block1.space to END of PARENT_ID,
                TOP of block1.space to BOTTOM of block0.space,
                BOTTOM of block1.space to TOP of showAllView
            )

            connect(
                START of showAllView to START of PARENT_ID,
                END of showAllView to END of PARENT_ID,
                TOP of showAllView to BOTTOM of block1.space,
                BOTTOM of showAllView to BOTTOM of PARENT_ID
            )

            connect(
                START of stubView to START of PARENT_ID,
                END of stubView to END of PARENT_ID,
                TOP of stubView to TOP of PARENT_ID,
                BOTTOM of stubView to BOTTOM of PARENT_ID
            )
        }
    }


    val showAllClicks: Observable<Unit> = clicks().filter { !stubView.isVisible }.throttleFirst(1, TimeUnit.SECONDS)


    fun setData(transactions: List<Transaction>) = post {
        if (transactions.isNotEmpty()) {
            stubView.isVisible = false

            val transaction0 = transactions.getOrNull(0)
            if (transaction0 != null) {
                block0.isVisible = true

                block0.category.textResource = Categories.findById(transaction0.categoryId, transaction0.isGain).stringRes

                val sign = if (transaction0.isGain) "+" else "-"
                val amount =
                    "$sign ${currencyManager.formatMoney(transaction0.amount, transaction0.account!!.currencyIndex)}"
                block0.amount.text = amount
            } else {
                block0.isVisible = false
            }

            val transaction1 = transactions.getOrNull(1)
            if (transaction1 != null) {
                block1.isVisible = true

                block1.category.textResource = Categories.findById(transaction1.categoryId, transaction1.isGain).stringRes

                val sign = if (transaction1.isGain) "+" else "-"
                val amount =
                    "$sign ${currencyManager.formatMoney(transaction1.amount, transaction1.account!!.currencyIndex)}"
                block1.amount.text = amount
            } else {
                block1.isVisible = false
            }
        } else {
            stubView.isVisible = true
        }
    }

    private fun _ConstraintLayout.getTransactionBlock(withShadow: Boolean): Block {
        val space = space {
            id = View.generateViewId()
        }.lparams(matchConstraint, matchConstraint)

        val categoryText = textView {
            id = View.generateViewId()
            textSize = 14f
            textColorResource = R.color.smoke
            includeFontPadding = false
        }.lparams(wrapContent, wrapContent)

        val amountText = textView {
            id = View.generateViewId()
            textSize = 14f
            textColorResource = R.color.smoke
            includeFontPadding = false
        }.lparams(wrapContent, wrapContent)

        val line = dashedLineView {
            id = View.generateViewId()
        }.lparams(matchConstraint, dip(2)) {
            leftMargin = dip(8)
            rightMargin = dip(8)
        }

        val shadow = if (withShadow) {
            view {
                id = View.generateViewId()
                backgroundResource = R.drawable.short_transactions_list_shadow
            }.lparams(matchConstraint, matchConstraint)
        } else {
            space {
                id = View.generateViewId()
            }.lparams(matchConstraint, matchConstraint)
        }

        applyConstraintSet {
            connect(
                START of categoryText to START of space,
                TOP of categoryText to TOP of space,
                BOTTOM of categoryText to BOTTOM of space
            )

            connect(
                END of amountText to END of space,
                BASELINE of amountText to BASELINE of categoryText
            )

            connect(
                START of line to END of categoryText,
                END of line to START of amountText,
                BASELINE of line to BASELINE of categoryText
            )

            connect(
                START of shadow to START of space,
                END of shadow to END of space,
                TOP of shadow to TOP of space,
                BOTTOM of shadow to BOTTOM of space
            )
        }

        return Block(space, categoryText, amountText, line)
    }


    private class Block(val space: Space, val category: TextView, val amount: TextView, val line: View) {

        var isVisible: Boolean = true
            set(value) {
                field = value
                category.isVisible = isVisible
                amount.isVisible = isVisible
                line.isVisible = isVisible
            }

    }

}