package com.alex_aladdin.cash.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.repository.entities.Transaction
import org.jetbrains.anko.*

class TransactionsList(context: Context) : RecyclerView(context) {


    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }


    fun setData(data: List<Transaction>) = post {
        adapter = TransactionsAdapter(data)
    }


    private class TransactionsAdapter(private val data: List<Transaction>) : RecyclerView.Adapter<TransactionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
            val view = TransactionUI().createView(AnkoContext.create(parent.context, parent))
            return TransactionViewHolder(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
            holder.amountText.text = data[position].amount.toString()
        }


    }

    private class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amountText: TextView = itemView.findViewById(R.id.transaction_text_amount)

    }

    private class TransactionUI : AnkoComponent<ViewGroup> {

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            textView {
                id = R.id.transaction_text_amount
                layoutParams = RecyclerView.LayoutParams(wrapContent, wrapContent)
                textSize = 32f
                textColorResource = R.color.white
                includeFontPadding = false
                backgroundColor = Color.TRANSPARENT
            }
        }

    }

}