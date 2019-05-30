package com.alex_aladdin.cash.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.ui.transactionsList
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.DayTransactionsViewModel
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DayTransactionsFragment : Fragment() {

    companion object {

        private const val TYPE = "type"

        fun create(type: Type) = DayTransactionsFragment().apply {
            arguments = bundleOf(TYPE to type)
        }

    }


    private val viewModel: DayTransactionsViewModel by sharedViewModel()
    private val dc = DisposableCache()
    private val type by lazy { arguments!!.getSerializable(TYPE) as Type }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = container?.context?.frameLayout {
        transactionsList {
            id = R.id.transactions_list

            val transactionsSingle = if (type == Type.LOSS) {
                viewModel.dayLossTransactions
            } else {
                viewModel.dayGainTransactions
            }

            val transactionsTotal = if (type == Type.LOSS) {
                viewModel.dayLossTotal
            } else {
                viewModel.dayGainTotal
            }

            Single
                .zip(
                    transactionsSingle,
                    transactionsTotal,
                    BiFunction<List<Transaction>, Double, Pair<List<Transaction>, Double>> { transactions, total ->
                        transactions to total
                    }
                )
                .subscribeOnUi { (transactions, total) ->
                    setData(transactions, total)
                }
                .cache(dc)
        }.lparams(matchParent, matchParent)
    }

    override fun onDestroyView() {
        dc.drain()
        super.onDestroyView()
    }


    enum class Type { GAIN, LOSS }

}