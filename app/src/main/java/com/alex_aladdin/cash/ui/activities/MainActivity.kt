package com.alex_aladdin.cash.ui.activities

import android.app.DatePickerDialog
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
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.chart.ChartView
import com.alex_aladdin.cash.ui.dates.DatesRecyclerView
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.utils.anko.chartView
import com.alex_aladdin.cash.utils.anko.datesRecyclerView
import com.alex_aladdin.cash.utils.anko.fancyButton
import com.alex_aladdin.cash.utils.anko.shortTransactionsList
import com.alex_aladdin.cash.viewmodels.MainViewModel
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val dc = DisposableCache()
    private val maxClickRadius by lazy { dip(10).toFloat() }
    private val chartHitRect by lazy {
        val rect = Rect()
        chartView.getHitRect(rect)
        rect
    }

    private val calendarListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        val date = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+0000"))
            .apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            .time

        datesRecyclerView.setDate(date)
    }

    private lateinit var datesRecyclerView: DatesRecyclerView
    private lateinit var chartView: ChartView

    private var touchStart: PointF? = null
    private var calendarDialog: DatePickerDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        constraintLayout {
            datesRecyclerView = datesRecyclerView {
                id = R.id.dates_recycler_view

                init(viewModel.todayDate, currentLocale())
                dateObservable.subscribe(viewModel.currentDate.onNextConsumer()).cache(dc)

                setDate(viewModel.currentDate.value!!)
            }.lparams(matchConstraint, matchConstraint)

            val settingsButton = imageButton {
                id = View.generateViewId()
                padding = dip(12)
                setImageResource(R.drawable.ic_settings_white)

                setSelectableBackground(true)
            }.lparams(wrapContent, wrapContent) {
                leftMargin = dip(2)
            }

            val calendarButton = imageButton {
                id = View.generateViewId()
                padding = dip(12)
                setImageResource(R.drawable.ic_calendar)

                setSelectableBackground(true)
                setOnClickListenerWithThrottle {
                    showCalendarDialog()
                }.cache(dc)
            }.lparams(wrapContent, wrapContent) {
                rightMargin = dip(2)
            }

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
                bottomMargin = dip(16)
            }

            val shortTransactionsList = shortTransactionsList {
                id = View.generateViewId()
                bottomPadding = dip(16)

                showAllClicks.subscribeOnUi {
                    DayTransactionsActivity.start(this@MainActivity)
                }.cache(dc)

                viewModel.shortTransactionsListObservable.subscribeOnUi { transactions ->
                    setData(transactions)
                }.cache(dc)
            }.lparams(matchConstraint, dip(72)) {
                leftMargin = dip(12)
                rightMargin = dip(12)
            }

            val buttonGain = fancyButton {
                id = View.generateViewId()
                init(
                    ContextCompat.getColor(this@MainActivity, R.color.green),
                    ContextCompat.getColor(this@MainActivity, R.color.greenGradColor1),
                    ContextCompat.getColor(this@MainActivity, R.color.greenGradColor2)
                )
                setTextResource(R.string.gain)

                setOnClickListenerWithThrottle {
                    NewTransactionActivity.start(this@MainActivity, NewTransactionViewModel.Type.GAIN)
                }.cache(dc)
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

                setOnClickListenerWithThrottle {
                    NewTransactionActivity.start(this@MainActivity, NewTransactionViewModel.Type.LOSS)
                }.cache(dc)
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
                    TOP of settingsButton to TOP of PARENT_ID,
                    START of settingsButton to START of PARENT_ID
                )

                connect(
                    TOP of calendarButton to TOP of PARENT_ID,
                    END of calendarButton to END of PARENT_ID
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
                    BOTTOM of chartView to TOP of shortTransactionsList
                )

                connect(
                    START of shortTransactionsList to START of PARENT_ID,
                    END of shortTransactionsList to END of PARENT_ID,
                    TOP of shortTransactionsList to BOTTOM of chartView,
                    BOTTOM of shortTransactionsList to TOP of buttonLoss
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

    private fun showCalendarDialog() {
        val calendar = GregorianCalendar.getInstance(currentLocale()).also { it.time = viewModel.currentDate.value!! }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        calendarDialog?.dismiss()
        calendarDialog = DatePickerDialog(this, calendarListener, year, month, day).also { it.show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        dc.drain()
        calendarDialog?.dismiss()
    }

}