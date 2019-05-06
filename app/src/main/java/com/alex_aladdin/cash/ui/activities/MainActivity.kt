package com.alex_aladdin.cash.ui.activities

import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.core.graphics.contains
import androidx.lifecycle.ViewModelProviders
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.chart.ChartView
import com.alex_aladdin.cash.ui.dates.DatesAdapter
import com.alex_aladdin.cash.ui.dates.DatesLayoutManager
import com.alex_aladdin.cash.ui.dates.DatesSnapHelper
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.MainViewModel
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.recyclerview.v7.recyclerView
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private val dc = DisposableCache()
    private val maxClickRadius by lazy { dip(10).toFloat() }
    private val chartHitRect by lazy {
        val rect = Rect()
        chartView.getHitRect(rect)
        rect
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var chartView: ChartView

    private var touchStart: PointF? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)


        constraintLayout {
            val datesRecyclerView = recyclerView {
                id = R.id.dates_recycler_view
                layoutManager = DatesLayoutManager(this@MainActivity)
                DatesSnapHelper().attachToRecyclerView(this)
                setHasFixedSize(true)

                adapter = DatesAdapter(currentLocale(), viewModel.todayDate).also { datesAdapter ->
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

            chartView = chartView {
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

                setOnClickListener {
                    NewTransactionActivity.create(this@MainActivity, NewTransactionViewModel.Type.GAIN)
                }
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

                setOnClickListener {
                    NewTransactionActivity.create(this@MainActivity, NewTransactionViewModel.Type.LOSS)
                }
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (chartHitRect.contains(Point(ev.x.toInt(), ev.y.toInt()))) {
                    touchStart = PointF(ev.x, ev.y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchStart != null && touchStart!!.distanceTo(PointF(ev.x, ev.y)) > maxClickRadius) {
                    touchStart = null
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (touchStart != null) {
                    chartView.click(PointF(ev.x - chartView.left, ev.y - chartView.top))
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun PointF.distanceTo(point: PointF) = sqrt((x - point.x).pow(2) + (y - point.y).pow(2))

    override fun onDestroy() {
        super.onDestroy()
        dc.drain()
    }

}