package com.alex_aladdin.cash.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.solver.widgets.Guideline.VERTICAL
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.appCompatTextView
import com.alex_aladdin.cash.ui.currencyPicker
import com.alex_aladdin.cash.ui.fragments.CalculatorFragment
import com.alex_aladdin.cash.ui.fragments.CategoriesFragment
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.guideline
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.support.v4.viewPager
import java.text.SimpleDateFormat
import java.util.*

class NewTransactionActivity : AppCompatActivity() {

    companion object {

        private const val TYPE_EXTRA = "type"

        fun start(activity: Activity, type: NewTransactionViewModel.Type) {
            val intent = Intent(activity, NewTransactionActivity::class.java).apply {
                putExtra(TYPE_EXTRA, type)
            }
            activity.startActivity(intent)
        }

    }


    private val dc = DisposableCache()
    private val dateFormatter by lazy { SimpleDateFormat("d MMM yyyy", currentLocale()) }
    private val type by lazy { intent.getSerializableExtra(TYPE_EXTRA) as NewTransactionViewModel.Type }

    private lateinit var viewModel: NewTransactionViewModel
    private lateinit var viewPager: ViewPager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(NewTransactionViewModel::class.java)

        constraintLayout {
            setOnClickListener {
                viewPager.currentItem = 0
            }

            val guidelineInputStart = guideline {
                id = View.generateViewId()
            }.lparams(0, matchConstraint) {
                orientation = VERTICAL
                guidePercent = 0.15f
            }

            val guidelineInputEnd = guideline {
                id = View.generateViewId()
            }.lparams(wrapContent, matchConstraint) {
                orientation = VERTICAL
                guidePercent = 0.85f
            }

            val amountText = appCompatTextView {
                id = View.generateViewId()
                textColorResource = R.color.white
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                maxLines = 1
                includeFontPadding = false
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this@appCompatTextView,
                    12,
                    44,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )

                viewModel.amountObservable.subscribeOnUi { amount ->
                    text = amount
                }.cache(dc)
            }.lparams(matchConstraint, matchConstraint) {
                rightMargin = dip(16)
            }

            val currencyPicker = currencyPicker {
                id = View.generateViewId()
                setData(viewModel.currenciesList, viewModel.currencyIndex)

                itemPickedObservable.subscribe { pos ->
                    viewModel.currencyIndex = pos
                }.cache(dc)
            }.lparams(wrapContent, wrapContent)

            viewPager = viewPager {
                id = R.id.view_pager
                adapter = ViewPagerAdapter(supportFragmentManager)
            }.lparams(matchConstraint, minOf(dip(320), (screenSize().y * 0.5f).toInt()))

            val largeButton = button {
                id = View.generateViewId()
                backgroundResource = R.drawable.bg_large_button
                textColorResource = R.color.blue
                textSize = 14f
                letterSpacing = 0.02f
                typeface = Typeface.DEFAULT_BOLD
                allCaps = false
                text = "Check category"
            }.lparams(matchConstraint, dip(54))

            val toolbar = toolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_cross
                backgroundColorResource = R.color.deepDark

                setNavigationOnClickListener {
                    onBackPressed()
                }

                textView {
                    id = View.generateViewId()
                    textColorResource = R.color.white
                    textSize = 16f
                    backgroundColor = Color.TRANSPARENT
                    gravity = Gravity.CENTER_VERTICAL
                    includeFontPadding = false

                    viewModel.currentDateObservable.subscribeOnUi { date ->
                        text = getTitle(type, date)
                    }.cache(dc)
                }.lparams(wrapContent, matchParent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of guidelineInputStart to START of PARENT_ID
                )

                connect(
                    END of guidelineInputEnd to END of PARENT_ID
                )

                connect(
                    START of amountText to END of guidelineInputStart,
                    END of amountText to START of currencyPicker,
                    TOP of amountText to BOTTOM of toolbar,
                    BOTTOM of amountText to TOP of viewPager
                )

                connect(
                    END of currencyPicker to START of guidelineInputEnd,
                    TOP of currencyPicker to BOTTOM of toolbar,
                    BOTTOM of currencyPicker to TOP of viewPager
                )

                connect(
                    START of viewPager to START of PARENT_ID,
                    END of viewPager to END of PARENT_ID,
                    BOTTOM of viewPager to TOP of largeButton
                )

                connect(
                    START of largeButton to START of PARENT_ID,
                    END of largeButton to END of PARENT_ID,
                    BOTTOM of largeButton to BOTTOM of PARENT_ID
                )
            }
        }
    }

    private fun getTitle(type: NewTransactionViewModel.Type, date: Date): SpannableStringBuilder {
        val dateReplacement = "{date}"
        val transactionType = getString(if (type == NewTransactionViewModel.Type.GAIN) R.string.new_gain_on else R.string.new_loss_on)
        val formattedDate = dateFormatter.format(date)
        val text = "$transactionType $dateReplacement"

        return text
            .asSpannableBuilder()
            .replace(dateReplacement, formattedDate, StyleSpan(Typeface.BOLD))
    }

    override fun onDestroy() {
        super.onDestroy()
        dc.drain()
    }


    private class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val calculatorFragment = CalculatorFragment()
        private val categoriesFragment = CategoriesFragment()

        override fun getItem(position: Int) = when (position) {
            0 -> calculatorFragment
            1 -> categoriesFragment
            else -> throw IllegalArgumentException()
        }

        override fun getCount() = 2

    }

}