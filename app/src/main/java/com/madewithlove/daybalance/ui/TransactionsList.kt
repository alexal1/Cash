package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.CHAIN_PACKED
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.helpers.CurrencyManager
import com.madewithlove.daybalance.repository.entities.Transaction
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.viewmodels.enums.Categories
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.core.KoinComponent
import org.koin.core.get
import java.text.DateFormat
import java.text.DateFormat.SHORT
import java.util.*

class TransactionsList(context: Context) : RecyclerView(context), KoinComponent {

    init {
        setPadding(0, dimen(R.dimen.day_transactions_list_padding), 0, dimen(R.dimen.day_transactions_list_padding))
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        adapter = EmptyAdapter()
    }


    fun setData(data: List<Transaction>, total: Double, onTransactionClick: (Transaction) -> Unit) = post {
        if (adapter is EmptyAdapter) {
            adapter = TransactionsAdapter(data, total, get(), onTransactionClick, context.currentLocale())
        } else {
            val transactionsAdapter = adapter as TransactionsAdapter

            val oldData = transactionsAdapter.data.toList()
            transactionsAdapter.data = data
            transactionsAdapter.total = total

            val transactionsDiff = TransactionsDiff(oldData, data)
            val diffResult = DiffUtil.calculateDiff(transactionsDiff)
            diffResult.dispatchUpdatesTo(adapter!!)
        }
    }


    private class TransactionsAdapter(
        var data: List<Transaction>,
        var total: Double,
        private val currencyManager: CurrencyManager,
        private val onTransactionClick: (Transaction) -> Unit,
        locale: Locale
    ) : RecyclerView.Adapter<ViewHolder>() {


        private val dateFormatter by lazy { DateFormat.getDateInstance(SHORT, locale) }


        override fun getItemViewType(position: Int): Int = if (data.isNotEmpty()) {
            if (position < data.size) {
                Type.TRANSACTION.ordinal
            } else {
                Type.TOTAL.ordinal
            }
        } else {
            Type.NO_DATA.ordinal
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when (viewType) {
            Type.TRANSACTION.ordinal -> {
                val view = TransactionUI().createView(AnkoContext.create(parent.context, parent))
                view.setOnClickListener {
                    onTransactionClick(it.tag as Transaction)
                }
                TransactionViewHolder(view)
            }

            Type.TOTAL.ordinal -> {
                val view = TotalUI().createView(AnkoContext.create(parent.context, parent))
                TotalViewHolder(view)
            }

            Type.NO_DATA.ordinal -> {
                val view = NoDataUI().createView(AnkoContext.create(parent.context, parent))
                NoDataViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unexpected viewType $viewType")
        }

        override fun getItemCount() = data.size + 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                Type.TRANSACTION.ordinal -> {
                    val transaction = data[position]

                    holder as TransactionViewHolder
                    holder.apply {
                        itemView.tag = transaction

                        amountText.text = currencyManager.formatMoney(
                            transaction.getAmountPerDay(),
                            transaction.account!!.currencyIndex
                        )

                        categoryText.textResource =
                            Categories.findById(transaction.categoryId, transaction.isGain()).stringRes

                        if (transaction.getDaysCount() == 1) {
                            periodText.textResource = if (transaction.isGain()) {
                                R.string.gain_transaction_period_one_day
                            } else {
                                R.string.loss_transaction_period_one_day
                            }
                        } else {
                            periodText.text = itemView.context.getPeriod(transaction)
                        }

                        separator.isInvisible = position == itemCount - 2
                    }
                }

                Type.TOTAL.ordinal -> {
                    holder as TotalViewHolder
                    holder.apply {
                        totalAmountText.text = currencyManager.formatMoney(total)
                    }
                }
            }
        }

        private fun Context.getPeriod(transaction: Transaction): SpannableStringBuilder {
            val dateReplacement = "{date}"
            val text = getString(
                if (transaction.isGain()) {
                    R.string.gain_transaction_period_couple_of_days
                } else {
                    R.string.loss_transaction_period_couple_of_days
                },
                dateReplacement,
                dateReplacement
            )
            val formattedDate1 = dateFormatter.format(transaction.startTimestamp)
            val formattedDate2 = dateFormatter.format(transaction.endTimestamp)

            return text
                .asSpannableBuilder()
                .replace(dateReplacement, formattedDate1, StyleSpan(Typeface.BOLD))
                .replace(dateReplacement, formattedDate2, StyleSpan(Typeface.BOLD))
        }


        enum class Type { TRANSACTION, TOTAL, NO_DATA }

    }

    private class TransactionViewHolder(itemView: View) : ViewHolder(itemView) {

        val amountText: TextView = itemView.findViewById(R.id.transaction_text_amount)
        val categoryText: TextView = itemView.findViewById(R.id.transaction_category)
        val periodText: TextView = itemView.findViewById(R.id.transaction_period)
        val separator: View = itemView.findViewById(R.id.transaction_separator)

    }

    private class TransactionUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            constraintLayout {
                layoutParams = LayoutParams(matchParent, dimen(R.dimen.day_transactions_item_height))
                setSelectableBackground()

                val arrow = imageView {
                    id = View.generateViewId()
                    setImageResource(R.drawable.ic_chevron_right)
                }.lparams(wrapContent, wrapContent)

                val amount = textView {
                    id = R.id.transaction_text_amount
                    textColorResource = R.color.white
                    textSize = 16f
                    typeface = ResourcesCompat.getFont(context, R.font.currencies)
                }.lparams(wrapContent, wrapContent) {
                    rightMargin = dip(8)
                }

                val category = textView {
                    id = R.id.transaction_category
                    textColorResource = R.color.fog_white
                    textSize = 16f
                    ellipsize = TextUtils.TruncateAt.END
                }.lparams(matchConstraint, wrapContent) {
                    verticalChainStyle = CHAIN_PACKED
                    rightMargin = dip(8)
                }

                val period = textView {
                    id = R.id.transaction_period
                    textColorResource = R.color.smoke
                    textSize = 14f
                }.lparams(wrapContent, wrapContent) {
                    topMargin = dip(2)
                    verticalChainStyle = CHAIN_PACKED
                }

                val separator = view {
                    id = R.id.transaction_separator
                    backgroundColorResource = R.color.palladium_80
                }.lparams(matchConstraint, dip(1)) {
                    marginStart = dip(24)
                }

                applyConstraintSet {
                    connect(
                        TOP of arrow to TOP of PARENT_ID,
                        BOTTOM of arrow to BOTTOM of PARENT_ID,
                        END of arrow to END of PARENT_ID
                    )

                    connect(
                        TOP of amount to TOP of category,
                        END of amount to START of arrow
                    )

                    connect(
                        TOP of category to TOP of PARENT_ID,
                        BOTTOM of category to TOP of period,
                        START of category to START of separator,
                        END of category to START of amount
                    )

                    connect(
                        TOP of period to BOTTOM of category,
                        BOTTOM of period to TOP of separator,
                        START of period to START of separator
                    )

                    connect(
                        START of separator to START of PARENT_ID,
                        END of separator to END of PARENT_ID,
                        BOTTOM of separator to BOTTOM of PARENT_ID
                    )
                }
            }
        }

    }

    private class TotalViewHolder(itemView: View) : ViewHolder(itemView) {

        val totalAmountText: TextView = itemView.findViewById(R.id.transaction_total_amount)

    }

    private class TotalUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            constraintLayout {
                layoutParams = LayoutParams(matchParent, dimen(R.dimen.day_transactions_item_height))

                val separator = view {
                    id = R.id.transaction_separator
                    backgroundColorResource = R.color.palladium_80
                }.lparams(matchConstraint, dip(1))

                val totalText = textView {
                    id = View.generateViewId()
                    textSize = 16f
                    textColorResource = R.color.white_80
                    allCaps = true
                    typeface = Typeface.DEFAULT_BOLD
                    textResource = R.string.day_transactions_total
                }.lparams(wrapContent, wrapContent) {
                    leftMargin = dip(12)
                    topMargin = dip(16)
                }

                val totalAmount = textView {
                    id = R.id.transaction_total_amount
                    textSize = 16f
                    textColorResource = R.color.white
                    typeface = ResourcesCompat.getFont(context, R.font.currencies)
                }.lparams(wrapContent, wrapContent) {
                    rightMargin = dip(24)
                    topMargin = dip(16)
                }

                applyConstraintSet {
                    connect(
                        START of separator to START of PARENT_ID,
                        END of separator to END of PARENT_ID,
                        TOP of separator to TOP of PARENT_ID
                    )

                    connect(
                        START of totalText to START of PARENT_ID,
                        TOP of totalText to TOP of PARENT_ID
                    )

                    connect(
                        END of totalAmount to END of PARENT_ID,
                        TOP of totalAmount to TOP of PARENT_ID
                    )
                }
            }
        }

    }

    private class NoDataViewHolder(itemView: View) : ViewHolder(itemView)

    private class NoDataUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            val availableHeight = ctx.screenSize().y -
                    dimen(R.dimen.day_transactions_app_bar_height) -
                    dimen(R.dimen.day_transactions_tab_layout_height) -
                    dimen(R.dimen.day_transactions_list_padding) * 2

            textView {
                layoutParams = LayoutParams(matchParent, maxOf(availableHeight, dimen(R.dimen.day_transactions_item_height)))
                gravity = CENTER
                textSize = 16f
                textColorResource = R.color.fog_white
                textResource = R.string.day_transactions_no_data
            }
        }

    }

    private class TransactionsDiff(
        private val oldData: List<Transaction>,
        private val newData: List<Transaction>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            if (oldData.isNotEmpty() && newData.isNotEmpty()) {
                when {
                    oldItemPosition < oldData.size && newItemPosition < newData.size -> {
                        oldData[oldItemPosition].id == newData[newItemPosition].id
                    }

                    oldItemPosition == oldData.size && newItemPosition == newData.size -> true

                    else -> false
                }
            } else {
                true
            }

        override fun getOldListSize(): Int = oldData.size + 1

        override fun getNewListSize(): Int = newData.size + 1

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = when {
            oldData.isNotEmpty() && newData.isNotEmpty() && newItemPosition >= newData.size - 1 -> false
            else -> true
        }

    }

    private class EmptyAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            throw NotImplementedError()
        }

        override fun getItemCount(): Int = 0

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            throw NotImplementedError()
        }

    }

}