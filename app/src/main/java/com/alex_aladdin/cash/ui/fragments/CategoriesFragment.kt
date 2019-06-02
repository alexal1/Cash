package com.alex_aladdin.cash.ui.fragments

import android.graphics.Color
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.solver.widgets.Guideline
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.CHAIN_PACKED
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.appCompatTextView
import com.alex_aladdin.cash.ui.categoryPicker
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.setOnClickListenerWithThrottle
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import com.alex_aladdin.cash.viewmodels.enums.Categories
import com.alex_aladdin.cash.viewmodels.enums.GainCategories
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.guideline
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CategoriesFragment : Fragment() {

    private val viewModel: NewTransactionViewModel by sharedViewModel()
    private val dc = DisposableCache()

    private var periodDialog: AlertDialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = container?.context?.constraintLayout {
        backgroundColorResource = R.color.soft_dark

        val categoryHighlight = view {
            id = View.generateViewId()
            backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.category_highlight)
        }.lparams(dip(100), dip(42))

        val categoryPicker = categoryPicker {
            id = R.id.category_picker
            backgroundColor = Color.TRANSPARENT

            val categories: List<Categories> = if (viewModel.type == NewTransactionViewModel.Type.LOSS) {
                LossCategories.values().toList().reversed()
            } else {
                GainCategories.values().toList().reversed()
            }

            val startPos = categories.indexOf(viewModel.getDefaultCategory())

            setData(categories, startPos)

            averageItemObservable.subscribeOnUi { (width, color) ->
                val lp = categoryHighlight.layoutParams
                lp.width = width.toInt()
                categoryHighlight.layoutParams = lp

                categoryHighlight.backgroundDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }.cache(dc)

            itemPickedObservable.subscribeOnUi(viewModel.categoryPickConsumer).cache(dc)
        }.lparams(matchConstraint, matchConstraint)

        val periodBg = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.steel_gray
        }.lparams(matchConstraint, matchConstraint)

        val periodLabelBg = view {
            id = View.generateViewId()
            backgroundResource = R.drawable.bg_period
        }.lparams(matchConstraint, dip(96))

        val periodLabelTail = view {
            id = View.generateViewId()
            backgroundResource = R.drawable.ic_tail
        }.lparams(dip(24), dip(84))

        val periodTitle = textView {
            id = View.generateViewId()
            textResource = if (viewModel.type == NewTransactionViewModel.Type.LOSS) R.string.period_title_loss else R.string.period_title_gain
            backgroundColor = Color.TRANSPARENT
            gravity = CENTER_HORIZONTAL
            textSize = 12f
            textColorResource = R.color.fog_white
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dip(8)
        }

        val periodText = appCompatTextView {
            id = View.generateViewId()
            backgroundColor = Color.TRANSPARENT
            textSize = 32f
            textColorResource = R.color.white_80
            maxLines = 1
            gravity = CENTER

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this@appCompatTextView,
                12,
                32,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )

            viewModel.periodObservable.subscribeOnUi { period ->
                text = context?.let(period::getApproximateString)
            }.cache(dc)
        }.lparams(matchConstraint, wrapContent) {
            margin = dip(4)
            verticalChainStyle = CHAIN_PACKED
        }

        val periodSettings = textView {
            id = View.generateViewId()
            textResource = R.string.period_modify
            backgroundColor = Color.TRANSPARENT
            textSize = 14f
            textColorResource = R.color.white_60
            compoundDrawablePadding = dip(4)
            paintFlags = paintFlags or UNDERLINE_TEXT_FLAG
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings, 0, 0, 0)

            setOnClickListenerWithThrottle {
                showPeriodManageDialog()
            }.cache(dc)
        }.lparams(wrapContent, wrapContent) {
            topMargin = dip(4)
            verticalChainStyle = CHAIN_PACKED
        }

        val guideline = guideline {
            id = View.generateViewId()
        }.lparams(wrapContent, matchConstraint) {
            orientation = Guideline.VERTICAL
            guidePercent = 0.4f
        }

        applyConstraintSet {
            connect(
                START of categoryHighlight to START of guideline,
                TOP of categoryHighlight to TOP of PARENT_ID,
                BOTTOM of categoryHighlight to BOTTOM of PARENT_ID
            )

            connect(
                START of categoryPicker to END of guideline,
                END of categoryPicker to END of PARENT_ID,
                TOP of categoryPicker to TOP of PARENT_ID,
                BOTTOM of categoryPicker to BOTTOM of PARENT_ID
            )

            connect(
                START of periodBg to START of PARENT_ID,
                END of periodBg to START of guideline,
                TOP of periodBg to TOP of PARENT_ID,
                BOTTOM of periodBg to BOTTOM of PARENT_ID
            )

            connect(
                START of periodLabelBg to START of PARENT_ID,
                END of periodLabelBg to START of periodLabelTail,
                TOP of periodLabelBg to TOP of PARENT_ID,
                BOTTOM of periodLabelBg to BOTTOM of PARENT_ID
            )

            connect(
                END of periodLabelTail to START of guideline,
                TOP of periodLabelTail to TOP of PARENT_ID,
                BOTTOM of periodLabelTail to BOTTOM of PARENT_ID
            )

            connect(
                START of periodTitle to START of PARENT_ID,
                END of periodTitle to START of guideline,
                TOP of periodTitle to TOP of PARENT_ID
            )

            connect(
                START of periodText to START of periodLabelBg,
                END of periodText to END of periodLabelBg,
                TOP of periodText to TOP of periodLabelBg,
                BOTTOM of periodText to TOP of periodSettings
            )

            connect(
                START of periodSettings to START of periodLabelBg,
                END of periodSettings to END of periodLabelBg,
                TOP of periodSettings to BOTTOM of periodText,
                BOTTOM of periodSettings to BOTTOM of periodLabelBg
            )

            connect(
                START of guideline to START of PARENT_ID
            )
        }
    }

    private fun showPeriodManageDialog() {
        var chosenIndex = viewModel.getCurrentPeriodIndex()

        periodDialog?.dismiss()
        periodDialog = AlertDialog.Builder(requireContext())
            .setTitle(viewModel.currentCategory.stringRes)
            .setSingleChoiceItems(
                viewModel.getAvailablePeriods().map { getString(it.fullString) }.toTypedArray(),
                chosenIndex
            ) { _, index -> chosenIndex = index }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                viewModel.setPeriodIndex(chosenIndex)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        dc.drain()
        periodDialog?.dismiss()
        super.onDestroyView()
    }

}