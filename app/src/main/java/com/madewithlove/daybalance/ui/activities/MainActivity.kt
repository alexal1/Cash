/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity.CENTER
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.contains
import com.madewithlove.daybalance.CashApp.Companion.PREFS_AUTO_SWITCH_CURRENCY
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.ui.DialogCheckBox
import com.madewithlove.daybalance.ui.TipsView
import com.madewithlove.daybalance.ui.chart.ChartView
import com.madewithlove.daybalance.ui.dates.DatesRecyclerView
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.anko.*
import com.madewithlove.daybalance.utils.spans.TypefaceSpan
import com.madewithlove.daybalance.viewmodels.MainViewModel
import com.madewithlove.daybalance.viewmodels.NewTransactionViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : BaseActivity() {

    companion object {

        const val NEW_TRANSACTION_REQUEST_CODE = 1


        fun start(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)

            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.fade_in,
                R.anim.fade_out
            ).toBundle()

            activity.startActivity(intent, options)
        }

    }


    private val viewModel: MainViewModel by viewModel()
    private val currencyManager: CurrencyManager by inject()
    private val sharedPreferences: SharedPreferences by inject()
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
    private lateinit var tipsView: TipsView

    private var touchStart: PointF? = null
    private var calendarDialog: DatePickerDialog? = null
    private var mismatchedCurrencyDialog: AlertDialog? = null
    private var chartViewMarginAnimator: Animator? = null
    private var tipVisibilityAnimator: Animator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.checkCurrencyMismatch()
        }

        viewModel.showMismatchedCurrencyDialogObservable.subscribeOnUi { currency ->
            showMismatchedCurrencyDialog(currency)
        }.cache(dc)

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
                setOnClickListenerWithThrottle {
                    SettingsActivity.start(this@MainActivity)
                }.cache(dc)
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

            tipsView = tipsView {
                id = View.generateViewId()
                alpha = 0f
                translationY = -dimen(R.dimen.tip_margin_bottom).toFloat()

                viewModel.tipsDataObservable.subscribeOnUi {
                    val tip = it.blockingGet()
                    if (tip != null) {
                        setData(tip)

                        val widthSpec = View.MeasureSpec.makeMeasureSpec(screenSize().x, EXACTLY)
                        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, UNSPECIFIED)
                        this@tipsView.measure(widthSpec, heightSpec)

                        animateChartViewMargin(this@tipsView.measuredHeight) {
                            animateTipVisibility(1f)
                        }
                    } else {
                        animateTipVisibility(0f) {
                            animateChartViewMargin(0)
                        }
                    }
                }.cache(dc)

                closeClick.subscribe(viewModel::closeTip).cache(dc)

                lossClick.subscribeOnUi {
                    NewTransactionActivity.start(this@MainActivity, NewTransactionViewModel.Type.LOSS)
                }.cache(dc)

                gainClick.subscribeOnUi {
                    NewTransactionActivity.start(this@MainActivity, NewTransactionViewModel.Type.GAIN)
                }.cache(dc)
            }.lparams(matchConstraint, wrapContent)

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
                bottomMargin = dip(8)
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
                bottomMargin = dip(8)
                rightMargin = dip(4)
            }

            val realBalanceBackground = view {
                id = View.generateViewId()
                backgroundResource = R.color.soft_dark
            }.lparams(matchConstraint, dip(32))

            val realBalanceText = textView {
                id = View.generateViewId()
                backgroundColor = Color.TRANSPARENT
                textColorResource = R.color.smoke
                textSize = 12f
                gravity = CENTER
                letterSpacing = 0.01f
                compoundDrawablePadding = dip(8)
                typeface = ResourcesCompat.getFont(context, R.font.currencies)

                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_coins_stack, 0, 0, 0)

                viewModel.realBalanceObservable.subscribeOnUi { realBalance ->
                    text = getString(R.string.real_balance, realBalance)
                }.cache(dc)
            }.lparams(wrapContent, wrapContent)


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
                    START of tipsView to START of PARENT_ID,
                    END of tipsView to END of PARENT_ID,
                    TOP of tipsView to BOTTOM of datesSpace
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
                    BOTTOM of shortTransactionsList to TOP of buttonLoss
                )

                connect(
                    START of buttonGain to START of PARENT_ID,
                    END of buttonGain to START of buttonLoss,
                    BOTTOM of buttonGain to TOP of realBalanceBackground
                )

                connect(
                    START of buttonLoss to END of buttonGain,
                    END of buttonLoss to END of PARENT_ID,
                    BOTTOM of buttonLoss to TOP of realBalanceBackground
                )

                connect(
                    START of realBalanceBackground to START of PARENT_ID,
                    END of realBalanceBackground to END of PARENT_ID,
                    BOTTOM of realBalanceBackground to BOTTOM of PARENT_ID
                )

                connect(
                    START of realBalanceText to START of realBalanceBackground,
                    END of realBalanceText to END of realBalanceBackground,
                    TOP of realBalanceText to TOP of realBalanceBackground,
                    BOTTOM of realBalanceText to BOTTOM of realBalanceBackground
                )
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (chartHitRect.contains(Point(ev.x.toInt(), ev.y.toInt()))) {
                    // Another check for chartView's topPadding
                    if (ev.y >= chartHitRect.top + chartView.topPadding) {
                        touchStart = PointF(ev.x, ev.y)
                    }
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

    private fun showMismatchedCurrencyDialog(transactionCurrencyIndex: Int) {
        val transactionCurrency = currencyManager.getCurrenciesList()[transactionCurrencyIndex]
        val checkBox = DialogCheckBox(this@MainActivity, R.string.mismatched_currency_remember_choice)

        val typeface = ResourcesCompat.getFont(this, R.font.currencies)!!
        val message = SpannableStringBuilder(
            getString(
                R.string.mismatched_currency_message,
                transactionCurrency,
                transactionCurrency
            )
        ).setSpanForAll(transactionCurrency) {
            TypefaceSpan(typeface)
        }

        mismatchedCurrencyDialog?.dismiss()
        mismatchedCurrencyDialog = AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setView(checkBox)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                viewModel.switchToCurrency(transactionCurrencyIndex)
                if (checkBox.isChecked) {
                    sharedPreferences.edit {
                        putInt(PREFS_AUTO_SWITCH_CURRENCY, 0)
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                if (checkBox.isChecked) {
                    sharedPreferences.edit {
                        putInt(PREFS_AUTO_SWITCH_CURRENCY, 1)
                    }
                }

                dialog.dismiss()
            }
            .show()
    }

    private fun animateChartViewMargin(newMargin: Int, onEnd: () -> Unit = {}) {
        var onEndCallback: (() -> Unit)? = onEnd

        chartViewMarginAnimator?.cancel()
        chartViewMarginAnimator = ValueAnimator.ofInt(chartView.topPadding, newMargin).apply {
            addUpdateListener {
                chartView.topPadding = it.animatedValue as Int
            }
            doOnCancel {
                onEndCallback = null
            }
            doOnEnd {
                onEndCallback?.invoke()
            }
            duration = 200
            start()
        }
    }

    private fun animateTipVisibility(newAlpha: Float, onEnd: () -> Unit = {}) {
        var onEndCallback: (() -> Unit)? = onEnd

        tipVisibilityAnimator?.cancel()
        tipVisibilityAnimator = ValueAnimator.ofFloat(tipsView.alpha, newAlpha).apply {
            addUpdateListener {
                tipsView.alpha = it.animatedValue as Float
            }
            doOnCancel {
                onEndCallback = null
            }
            doOnEnd {
                onEndCallback?.invoke()
            }
            duration = 200
            start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NEW_TRANSACTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.checkCurrencyMismatch()
        }
    }

    override fun onBackPressed() {
        if (!chartView.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dc.drain()
        calendarDialog?.dismiss()
        mismatchedCurrencyDialog?.dismiss()
        chartViewMarginAnimator?.cancel()
        tipVisibilityAnimator?.cancel()
    }

}