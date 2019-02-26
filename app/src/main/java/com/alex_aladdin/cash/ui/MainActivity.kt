package com.alex_aladdin.cash.ui

import android.arch.lifecycle.ViewModelProviders
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.constraint.ConstraintSet.PARENT_ID
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.dates.DatesAdapter
import com.alex_aladdin.cash.ui.dates.DatesLayoutManager
import com.alex_aladdin.cash.ui.dates.DatesSnapHelper
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.MainViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.recyclerview.v7.recyclerView

class MainActivity : AppCompatActivity() {

    private val dc = DisposableCache()

    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)


        constraintLayout {
            val datesRecyclerView = recyclerView {
                id = View.generateViewId()
                layoutManager = DatesLayoutManager(this@MainActivity)
                DatesSnapHelper().attachToRecyclerView(this)
                adapter = DatesAdapter(currentLocale()).also { datesAdapter ->
                    datesAdapter.dateObservable.subscribe(viewModel.dateConsumer).cache(dc)
                }
            }.lparams(matchConstraint, matchConstraint)

            val weekdayText = textView {
                id = View.generateViewId()
                textSize = 14f
                textColorResource = R.color.white
                includeFontPadding = false
                letterSpacing = 0.01f
                alpha = 0.8f
                viewModel.weekdayObservable.subscribeOnUi { weekday ->
                    text = if (weekday.isToday) "${weekday.name} (${getString(R.string.today)})" else weekday.name
                }.cache(dc)
            }.lparams(wrapContent, wrapContent) {
                topMargin = dip(12)
            }

            val datesSpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, dimen(R.dimen.date_height))

            val chartView = chartView {
                id = View.generateViewId()
                setZOrderOnTop(true)
                holder.setFormat(PixelFormat.TRANSPARENT)
                viewModel.chartDataObservable.subscribe(chartDataConsumer).cache(dc)
            }.lparams(matchConstraint, matchConstraint) {
                bottomMargin = dip(32)
            }

            val buttonGain = fancyButton {
                id = View.generateViewId()
                init(
                    ContextCompat.getColor(this@MainActivity, R.color.green),
                    ContextCompat.getColor(this@MainActivity, R.color.greenGradColor1),
                    ContextCompat.getColor(this@MainActivity, R.color.greenGradColor2)
                )
                setTextResource(R.string.gain)
            }.lparams(matchConstraint, dip(70)) {
                bottomMargin = dip(4)
                leftMargin = dip(4)
            }

            val buttonLoss = fancyButton {
                id = View.generateViewId()
                init(
                    ContextCompat.getColor(this@MainActivity, R.color.red),
                    ContextCompat.getColor(this@MainActivity, R.color.redGradColor1),
                    ContextCompat.getColor(this@MainActivity, R.color.redGradColor2)
                )
                setTextResource(R.string.loss)
            }.lparams(matchConstraint, dip(70)) {
                bottomMargin = dip(4)
                rightMargin = dip(4)
            }


            applyConstraintSet {
                connect(
                    START of datesRecyclerView to START of PARENT_ID,
                    END of datesRecyclerView to END of PARENT_ID,
                    TOP of datesRecyclerView to TOP of PARENT_ID,
                    BOTTOM of datesRecyclerView to BOTTOM of PARENT_ID
                )

                connect(
                    START of weekdayText to START of PARENT_ID,
                    END of weekdayText to END of PARENT_ID,
                    TOP of weekdayText to TOP of PARENT_ID
                )

                connect(
                    START of datesSpace to START of PARENT_ID,
                    END of datesSpace to END of PARENT_ID,
                    TOP of datesSpace to TOP of PARENT_ID
                )

                connect(
                    START of chartView to START of PARENT_ID,
                    END of chartView to END of PARENT_ID,
                    TOP of chartView to BOTTOM of datesSpace,
                    BOTTOM of chartView to TOP of buttonLoss
                )

                connect(
                    START of buttonGain to START of PARENT_ID,
                    END of buttonGain to START of buttonLoss,
                    BOTTOM of buttonGain to BOTTOM of PARENT_ID
                )

                connect(
                    START of buttonLoss to END of buttonGain,
                    END of buttonLoss to END of PARENT_ID,
                    BOTTOM of buttonLoss to BOTTOM of PARENT_ID
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dc.drain()
    }

}