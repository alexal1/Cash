/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.string
import com.madewithlove.daybalance.utils.subscribeOnUi
import com.madewithlove.daybalance.viewmodels.StatisticsViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatisticsActivity : BaseActivity() {

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, StatisticsActivity::class.java)

            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.slide_in_up,
                R.anim.slide_out_up
            ).toBundle()

            activity.startActivity(intent, options)
        }

    }


    private val viewModel: StatisticsViewModel by viewModel()
    private val dc = DisposableCache()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        constraintLayout {
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
                    textResource = R.string.statistics_title
                    backgroundColor = Color.TRANSPARENT
                    gravity = Gravity.CENTER_VERTICAL
                    includeFontPadding = false
                }.lparams(wrapContent, matchParent)

                spinner {
                    id = View.generateViewId()
                    alpha = 0.8f

                    adapter = ArrayAdapter(
                        this@StatisticsActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        StatisticsViewModel.Interval.values().map { it.toStringValue() }
                    )

                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val interval = StatisticsViewModel.Interval.values()[position]
                            viewModel.intervalSubject.onNext(interval)
                        }
                    }

                    viewModel.intervalSubject.take(1).subscribeOnUi { interval ->
                        val position = StatisticsViewModel.Interval.values().indexOf(interval)
                        setSelection(position)
                    }.cache(dc)
                }.lparams(wrapContent, wrapContent, Gravity.END)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
    }

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }


    private fun StatisticsViewModel.Interval.toStringValue(): String = when (this) {
        StatisticsViewModel.Interval.THIS_MONTH -> string(R.string.statistics_interval_this_month)
        StatisticsViewModel.Interval.THIS_YEAR -> string(R.string.statistics_interval_this_year)
        StatisticsViewModel.Interval.ALL_TIME -> string(R.string.statistics_interval_all_time)
    }

}