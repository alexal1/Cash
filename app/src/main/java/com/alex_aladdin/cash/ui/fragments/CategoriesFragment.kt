package com.alex_aladdin.cash.ui.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.solver.widgets.Guideline
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.categoryPicker
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.guideline
import org.jetbrains.anko.constraint.layout.matchConstraint

class CategoriesFragment : Fragment() {

    private val dc = DisposableCache()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = container?.context?.constraintLayout {
        backgroundColorResource = R.color.soft_dark

        val categoryHighlight = view {
            id = View.generateViewId()
            backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.category_highlight)
        }.lparams(dip(100), dip(36))

        val categoryPicker = categoryPicker {
            id = View.generateViewId()
            setData(LossCategories.values().toList().reversed(), 2)
            backgroundColor = Color.TRANSPARENT

            averageItemObservable.subscribeOnUi { (width, color) ->
                val lp = categoryHighlight.layoutParams
                lp.width = width.toInt()
                categoryHighlight.layoutParams = lp

                categoryHighlight.backgroundDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }.cache(dc)

            itemPickedObservable.subscribeOnUi {
                // TODO
            }.cache(dc)
        }.lparams(matchConstraint, matchConstraint)

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
                START of guideline to START of PARENT_ID
            )
        }
    }

    override fun onDestroyView() {
        dc.drain()
        super.onDestroyView()
    }

}