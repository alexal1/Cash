/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Typeface.*
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.core.KoinComponent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionsList(context: Context, attributeSet: AttributeSet) : RecyclerView(context, attributeSet), KoinComponent {

    val checkSubject = PublishSubject.create<Item.TransactionItem>()
    val uncheckSubject = PublishSubject.create<Item.TransactionItem>()

    var onScrollPositionChanged: ((isTop: Boolean) -> Unit)? = null


    init {
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        adapter = TransactionsAdapter(
            emptyList(),
            false,
            context.currentLocale(),
            onChecked = { transaction ->
                checkSubject.onNext(transaction)
            },
            onUnchecked = { transaction ->
                uncheckSubject.onNext(transaction)
            }
        )

        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }


    fun setData(data: List<Item>, deleteModeOn: Boolean) = post {
        val transactionsAdapter = adapter as TransactionsAdapter

        val oldData = transactionsAdapter.data.toList()
        val oldDeleteModeOn = transactionsAdapter.deleteModeOn
        transactionsAdapter.data = data
        transactionsAdapter.deleteModeOn = deleteModeOn

        val transactionsDiff = TransactionsDiff(oldData, oldDeleteModeOn, data, deleteModeOn)
        val diffResult = DiffUtil.calculateDiff(transactionsDiff)
        diffResult.dispatchUpdatesTo(transactionsAdapter)
    }

    override fun onScrolled(dx: Int, dy: Int) {
        val offset = computeVerticalScrollOffset()
        if (offset == 0) {
            stopScroll()
            onScrollPositionChanged?.invoke(true)
            return
        }

        onScrollPositionChanged?.invoke(false)
    }


    sealed class Item(val type: Int) {

        companion object {
            const val TRANSACTION_TYPE = 0
            const val DATE_TYPE = 1
        }

        data class TransactionItem(val transaction: Transaction) : Item(TRANSACTION_TYPE) {
            var isChecked = false
        }

        data class DateItem(val date: Date) : Item(DATE_TYPE)

    }


    private class TransactionsAdapter(
        var data: List<Item>,
        var deleteModeOn: Boolean,
        private val locale: Locale,
        private val onChecked: (Item.TransactionItem) -> Unit,
        private val onUnchecked: (Item.TransactionItem) -> Unit
    ) : RecyclerView.Adapter<ViewHolder>() {

        private val monthFormatter by lazy { SimpleDateFormat("LLLL", locale) }
        private val weekdayFormatter by lazy { SimpleDateFormat("E", locale) }
        private val mediumDateFormatter by lazy {
            DateFormat.getDateInstance(
                DateFormat.MEDIUM,
                locale
            )
        }


        override fun getItemViewType(position: Int): Int = data[position].type

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            when (viewType) {
                Item.DATE_TYPE -> {
                    val view = DateUI().createView(AnkoContext.create(parent.context, parent))
                    DateViewHolder(view)
                }

                Item.TRANSACTION_TYPE -> {
                    val view = TransactionUI().createView(AnkoContext.create(parent.context, parent))
                    val viewHolder = TransactionViewHolder(view)

                    view.setOnLongClickListener {
                        if (!deleteModeOn) {
                            viewHolder.checkBox.isChecked = true
                            return@setOnLongClickListener true
                        }

                        return@setOnLongClickListener false
                    }

                    view.setOnClickListener {
                        if (viewHolder.checkBox.isVisible) {
                            viewHolder.checkBox.isChecked = !viewHolder.checkBox.isChecked
                        }
                    }

                    viewHolder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            onChecked(view.tag as Item.TransactionItem)
                        } else {
                            onUnchecked(view.tag as Item.TransactionItem)
                        }
                    }

                    viewHolder
                }

                else -> throw IllegalArgumentException("Unexpected viewType $viewType")
            }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                Item.TRANSACTION_TYPE -> {
                    val transactionItem = data[position] as Item.TransactionItem
                    val transaction = transactionItem.transaction

                    holder as TransactionViewHolder
                    holder.apply {
                        itemView.tag = transactionItem

                        amountText.text = TextFormatter.formatMoney(transaction.getMoney(), withPositivePrefix = true)
                        amountText.updateLayoutParams<MarginLayoutParams> { leftMargin = if (deleteModeOn) 0 else itemView.dip(24) }
                        commentText.text = transaction.comment
                        checkBox.isVisible = deleteModeOn
                        checkBox.isChecked = transactionItem.isChecked

                        when (transaction.getType()) {
                            Transaction.Type.INSTANT -> {
                                typeText.isVisible = false
                                backgroundView.backgroundColorResource = R.color.transparent
                            }

                            Transaction.Type.MONTH -> {
                                typeText.isVisible = true
                                if (transaction.getMoney().isGain()) {
                                    val month = Date(transaction.actionTimestamp).toMonth()
                                    typeText.text = itemView.context.getString(R.string.history_type_gain, month)
                                    backgroundView.backgroundColorResource = R.color.green
                                } else {
                                    val month = Date(transaction.actionTimestamp).toMonth()
                                    typeText.text = itemView.context.getString(R.string.history_type_mandatory_expense, month)
                                    backgroundView.backgroundColorResource = R.color.red
                                }
                            }

                            Transaction.Type.INTO_MONEYBOX -> {
                                typeText.isVisible = true
                                typeText.textResource = R.string.history_type_moneybox
                                backgroundView.backgroundColorResource = R.color.blue
                            }
                        }

                        separator.isInvisible = (position == data.size - 1) || (data[position + 1].type == Item.DATE_TYPE)
                    }
                }

                Item.DATE_TYPE -> {
                    val dateItem = data[position] as Item.DateItem
                    val date = dateItem.date

                    holder as DateViewHolder
                    holder.apply {
                        val weekday = weekdayFormatter.format(date).toUpperCase(locale)
                        val mediumDate = mediumDateFormatter.format(date)
                        dateText.text = SpannableStringBuilder(itemView.string(R.string.history_date_template))
                                .replace(
                                    "{weekday}",
                                    weekday,
                                    StyleSpan(BOLD)
                                )
                                .replace(
                                    "{medium_date}",
                                    mediumDate
                                )
                    }
                }
            }
        }

        private fun Date.toMonth(): String {
            return monthFormatter.format(this)
        }

    }


    private class TransactionViewHolder(itemView: View) : ViewHolder(itemView) {

        val backgroundView: View = itemView.findViewById(R.id.transaction_background)
        val amountText: TextView = itemView.findViewById(R.id.transaction_text_amount)
        val typeText: TextView = itemView.findViewById(R.id.transaction_text_type)
        val commentText: TextView = itemView.findViewById(R.id.transaction_text_comment)
        val separator: View = itemView.findViewById(R.id.transaction_separator)
        val checkBox: CheckBox = itemView.findViewById(R.id.transaction_checkbox)

    }


    private class TransactionUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            constraintLayout {
                layoutParams = LayoutParams(matchParent, wrapContent)
                setSelectableBackground()

                val backgroundView = view {
                    id = R.id.transaction_background
                    alpha = 0.3f
                }.lparams(matchConstraint, matchConstraint)

                val checkBox = checkBox {
                    id = R.id.transaction_checkbox

                    rightPadding = dip(8)
                }.lparams(wrapContent, wrapContent) {
                    leftMargin = dip(16)
                }

                val amountText = appCompatTextView {
                    id = R.id.transaction_text_amount
                    gravity = CENTER_VERTICAL
                    maxLines = 1
                    typeface = DEFAULT_BOLD
                    textColorResource = R.color.white

                    TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                        this,
                        1,
                        16,
                        1,
                        TypedValue.COMPLEX_UNIT_SP
                    )
                }.lparams(matchConstraint, dip(32)) {
                    topMargin = dip(8)
                    rightMargin = dip(16)
                }

                val typeText = textView {
                    id = R.id.transaction_text_type
                    textSize = 12f
                    typeface = DEFAULT_BOLD
                    textColorResource = R.color.white
                }.lparams(wrapContent, wrapContent) {
                    rightMargin = dip(16)
                }

                val commentText = textView {
                    id = R.id.transaction_text_comment
                    textColorResource = R.color.white_80
                    textSize = 14f
                    gravity = CENTER_VERTICAL
                    minHeight = dip(16)

                    setTypeface(DEFAULT, ITALIC)
                    setLineSpacing(0f, 1.2f)
                }.lparams(matchConstraint, wrapContent) {
                    topMargin = dip(4)
                    leftMargin = dip(8)
                }

                val separator = view {
                    id = R.id.transaction_separator
                    backgroundColorResource = R.color.palladium
                }.lparams(matchConstraint, dip(1)) {
                    topMargin = dip(8)
                    marginStart = dip(24)
                }

                applyConstraintSet {
                    connect(
                        START of backgroundView to START of PARENT_ID,
                        END of backgroundView to END of PARENT_ID,
                        TOP of backgroundView to TOP of PARENT_ID,
                        BOTTOM of backgroundView to BOTTOM of PARENT_ID
                    )

                    connect(
                        START of checkBox to START of PARENT_ID,
                        TOP of checkBox to TOP of PARENT_ID,
                        BOTTOM of checkBox to BOTTOM of PARENT_ID
                    )

                    connect(
                        START of amountText to END of checkBox,
                        END of amountText to START of typeText,
                        TOP of amountText to TOP of PARENT_ID
                    )

                    connect(
                        TOP of typeText to TOP of amountText,
                        BOTTOM of typeText to BOTTOM of amountText,
                        END of typeText to END of PARENT_ID
                    )

                    connect(
                        START of commentText to START of amountText,
                        END of commentText to END of separator,
                        TOP of commentText to BOTTOM of amountText
                    )

                    connect(
                        START of separator to START of PARENT_ID,
                        END of separator to END of PARENT_ID,
                        TOP of separator to BOTTOM of commentText
                    )
                }
            }
        }

    }


    private class DateViewHolder(itemView: View) : ViewHolder(itemView) {

        val dateText: TextView = itemView.findViewById(R.id.transaction_date)

    }


    private class DateUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.transaction_date
                layoutParams = LayoutParams(matchParent, wrapContent)
                backgroundColorResource = R.color.palladium
                textColorResource = R.color.white_80
                textSize = 16f
                letterSpacing = 0.02f

                setPadding(dip(24), dip(2), dip(24), dip(2))
            }
        }

    }


    private class TransactionsDiff(
        private val oldData: List<Item>,
        private val oldDeleteModeOn: Boolean,
        private val newData: List<Item>,
        private val newDeleteModeOn: Boolean
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition] == newData[newItemPosition]
        }

        override fun getOldListSize(): Int = oldData.size

        override fun getNewListSize(): Int = newData.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldDeleteModeOn != newDeleteModeOn) {
                if (newData[newItemPosition].type == Item.TRANSACTION_TYPE) {
                    return false
                }
            }

            if (newDeleteModeOn) {
                val newTransactionItem = newData[newItemPosition] as? Item.TransactionItem
                if (newTransactionItem != null) {
                    val oldTransactionItem = oldData[oldItemPosition] as Item.TransactionItem
                    return oldTransactionItem.isChecked == newTransactionItem.isChecked
                }
            }

            return true
        }

    }

}