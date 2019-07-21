package com.madewithlove.daybalance.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Space
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.CHAIN_PACKED
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.helpers.enums.Periods
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.ui.activities.DayTransactionsActivity.Companion.DETAILED_TRANSACTION_EXTRA_ID
import com.madewithlove.daybalance.ui.activities.DayTransactionsActivity.Companion.DETAILED_TRANSACTION_REQUEST_CODE
import com.madewithlove.daybalance.ui.activities.DayTransactionsActivity.Companion.DETAILED_TRANSACTION_RESULT_DELETE
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import com.madewithlove.daybalance.utils.spans.VerticalOffsetSpan
import com.madewithlove.daybalance.viewmodels.enums.Categories
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.android.ext.android.inject
import java.text.DateFormat
import kotlin.math.abs

class DetailedTransactionActivity : BaseActivity() {

    companion object {

        private const val TRANSACTION_EXTRA = "type"

        fun start(activity: Activity, transaction: Transaction) {
            val intent = Intent(activity, DetailedTransactionActivity::class.java).apply {
                putExtra(TRANSACTION_EXTRA, transaction)
            }

            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.slide_in_left,
                R.anim.slide_out_left
            ).toBundle()

            activity.startActivityForResult(intent, DETAILED_TRANSACTION_REQUEST_CODE, options)
        }

    }


    private val currencyManager: CurrencyManager by inject()
    private val dateFormatter by lazy { DateFormat.getDateInstance(DateFormat.SHORT, currentLocale()) }
    private val transaction by lazy { intent.getSerializableExtra(TRANSACTION_EXTRA) as Transaction }

    private var deleteDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (transaction.getDaysCount() > 1) {
            createMultipleDaysTransactionUI()
        } else {
            createSingleDayTransactionUI()
        }
    }

    private fun Activity.createMultipleDaysTransactionUI() = constraintLayout {
        val backButton = createBackButton()

        val block1 = createBlock(
            currencyManager.formatMoney(abs(transaction.amount), transaction.account!!.currencyIndex),
            getAmountSubtitle()
        ).lparams(matchConstraint, matchConstraint) {
            topMargin = dip(32)
        }

        val block2 = createBlock(
            getString(Periods.getByName(transaction.period).fullString),
            getPeriodSubtitle()
        )

        val block3 = createBlock(
            getFormulaTitle(),
            getFormulaSubtitle()
        ).lparams(matchConstraint, matchConstraint) {
            bottomMargin = dip(32)
        }

        val deleteButton = createDeleteButton()

        applyConstraintSet {
            connect(
                START of backButton to START of PARENT_ID,
                TOP of backButton to TOP of PARENT_ID
            )

            connect(
                START of block1 to START of PARENT_ID,
                END of block1 to END of PARENT_ID,
                TOP of block1 to TOP of PARENT_ID,
                BOTTOM of block1 to TOP of block2
            )

            connect(
                START of block2 to START of PARENT_ID,
                END of block2 to END of PARENT_ID,
                TOP of block2 to BOTTOM of block1,
                BOTTOM of block2 to TOP of block3
            )

            connect(
                START of block3 to START of PARENT_ID,
                END of block3 to END of PARENT_ID,
                TOP of block3 to BOTTOM of block2,
                BOTTOM of block3 to TOP of deleteButton
            )

            connect(
                START of deleteButton to START of PARENT_ID,
                END of deleteButton to END of PARENT_ID,
                BOTTOM of deleteButton to BOTTOM of PARENT_ID
            )
        }
    }

    private fun Activity.createSingleDayTransactionUI() = constraintLayout {
        val backButton = createBackButton()

        val block1 = createBlock(
            currencyManager.formatMoney(abs(transaction.amount), transaction.account!!.currencyIndex),
            getAmountSubtitle()
        ).lparams(matchConstraint, matchConstraint) {
            topMargin = dip(32)
        }

        val block2 = createBlock(
            getString(Periods.getByName(transaction.period).fullString),
            getPeriodSubtitle()
        ).lparams(matchConstraint, matchConstraint) {
            bottomMargin = dip(32)
        }

        val deleteButton = createDeleteButton()

        applyConstraintSet {
            connect(
                START of backButton to START of PARENT_ID,
                TOP of backButton to TOP of PARENT_ID
            )

            connect(
                START of block1 to START of PARENT_ID,
                END of block1 to END of PARENT_ID,
                TOP of block1 to TOP of PARENT_ID,
                BOTTOM of block1 to TOP of block2
            )

            connect(
                START of block2 to START of PARENT_ID,
                END of block2 to END of PARENT_ID,
                TOP of block2 to BOTTOM of block1,
                BOTTOM of block2 to TOP of deleteButton
            )

            connect(
                START of deleteButton to START of PARENT_ID,
                END of deleteButton to END of PARENT_ID,
                BOTTOM of deleteButton to BOTTOM of PARENT_ID
            )
        }
    }

    private fun _ConstraintLayout.createBlock(title: CharSequence, subtitle: CharSequence): Space {
        val space = space {
            id = View.generateViewId()
        }.lparams(matchConstraint, matchConstraint)

        val titleText = appCompatTextView {
            id = View.generateViewId()
            textColorResource = R.color.white
            textSize = 32f
            text = title
            gravity = Gravity.CENTER
            maxLines = 1
            includeFontPadding = false
            typeface = ResourcesCompat.getFont(context, R.font.currencies)

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this@appCompatTextView,
                12,
                32,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }.lparams(matchConstraint, dip(48f)) {
            verticalChainStyle = CHAIN_PACKED
            leftMargin = dip(16)
            rightMargin = dip(16)
        }

        val subtitleText = textView {
            id = View.generateViewId()
            textColorResource = R.color.fog_white
            textSize = 14f
            text = subtitle
            gravity = Gravity.CENTER_HORIZONTAL
        }.lparams(matchConstraint, wrapContent) {
            leftMargin = dip(16)
            rightMargin = dip(16)
            verticalChainStyle = CHAIN_PACKED
        }

        applyConstraintSet {
            connect(
                START of titleText to START of space,
                END of titleText to END of space,
                TOP of titleText to TOP of space,
                BOTTOM of titleText to TOP of subtitleText
            )

            connect(
                START of subtitleText to START of space,
                END of subtitleText to END of space,
                TOP of subtitleText to BOTTOM of titleText,
                BOTTOM of subtitleText to BOTTOM of space
            )
        }

        return space
    }

    private fun _ConstraintLayout.createBackButton(): View = imageButton {
        id = View.generateViewId()
        setImageResource(R.drawable.ic_arrow_back)

        setSelectableBackground(isBorderless = true)
        expandHitArea(4f)

        setOnClickListener {
            onBackPressed()
        }
    }.lparams(wrapContent, wrapContent) {
        topMargin = dip(8)
        leftMargin = dip(16)
    }

    private fun _ConstraintLayout.createDeleteButton(): View = button {
        id = View.generateViewId()
        textResource = R.string.transaction_delete
        backgroundResource = R.drawable.bg_delete_button

        setOnClickListener {
            deleteDialog?.dismiss()
            deleteDialog = AlertDialog.Builder(this@DetailedTransactionActivity)
                .setMessage(R.string.transaction_delete_dialog_title)
                .setPositiveButton(R.string.yes) { _, _ ->
                    val intent = Intent().apply {
                        putExtra(DETAILED_TRANSACTION_EXTRA_ID, transaction.id)
                    }

                    setResult(DETAILED_TRANSACTION_RESULT_DELETE, intent)
                    onBackPressed()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }.lparams(matchConstraint, wrapContent) {
        bottomMargin = dip(24)
        leftMargin = dip(32)
        rightMargin = dip(32)
    }

    private fun getAmountSubtitle(): SpannableStringBuilder {
        val dateReplacement = "{date}"
        val categoryReplacement = "{category}"
        val text = getString(
            if (transaction.isGain()) {
                R.string.transaction_amount_subtitle_gain
            } else {
                R.string.transaction_amount_subtitle_loss
            },
            dateReplacement,
            categoryReplacement
        )
        val formattedDate = dateFormatter.format(transaction.startTimestamp)
        val category = getString(Categories.findById(transaction.categoryId, transaction.isGain()).stringRes)

        return text
            .asSpannableBuilder()
            .replace(dateReplacement, formattedDate, StyleSpan(BOLD))
            .replace(categoryReplacement, category, StyleSpan(BOLD))
    }

    private fun getPeriodSubtitle(): String = if (transaction.isGain()) {
        getString(R.string.transaction_period_subtitle_gain)
    } else {
        getString(R.string.transaction_period_subtitle_loss)
    }

    private fun getFormulaTitle(): SpannableStringBuilder {
        val fullAmount = currencyManager.formatMoney(abs(transaction.amount), transaction.account!!.currencyIndex)
        val period = getString(Periods.getByName(transaction.period).shortString)
        val dayAmount = currencyManager.formatMoney(transaction.getAmountPerDay(), transaction.account!!.currencyIndex)

        return SpannableStringBuilder(fullAmount)
            .add(" / ",0, RelativeSizeSpan(1.5f), VerticalOffsetSpan(10f))
            .add(period, 0)
            .add(" = ",0)
            .add(dayAmount, 0, StyleSpan(BOLD))
            .add(getString(R.string.transaction_formula_tail), 0, StyleSpan(BOLD))
    }

    private fun getFormulaSubtitle(): String = if (transaction.isGain()) {
        getString(R.string.transaction_formula_subtitle_gain)
    } else {
        getString(R.string.transaction_formula_subtitle_loss)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        deleteDialog?.dismiss()
        super.onDestroy()
    }
}