package com.alex_aladdin.cash.ui.fragments

import android.os.Bundle
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.repository.entities.Transaction
import com.alex_aladdin.cash.ui.activities.DetailedTransactionActivity
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.anko.transactionsList
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.screenSize
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.DayTransactionsViewModel
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.jetbrains.anko.dimen
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.progressBar
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
        val progressBar = progressBar().lparams(
            dimen(R.dimen.day_transactions_progress_bar_size),
            dimen(R.dimen.day_transactions_progress_bar_size)
        ) {
            gravity = CENTER_HORIZONTAL

            val availableHeight = requireContext().screenSize().y -
                    dimen(R.dimen.day_transactions_app_bar_height) -
                    dimen(R.dimen.day_transactions_tab_layout_height)

            topMargin = (availableHeight - dimen(R.dimen.day_transactions_progress_bar_size)) / 2
        }

        transactionsList {
            id = R.id.transactions_list

            val transactionsObservable = if (type == Type.LOSS) {
                viewModel.dayLossTransactionsObservable
            } else {
                viewModel.dayGainTransactionsObservable
            }

            val transactionsTotal = if (type == Type.LOSS) {
                viewModel.dayLossTotalObservable
            } else {
                viewModel.dayGainTotalObservable
            }

            Observable
                .zip(
                    transactionsObservable,
                    transactionsTotal,
                    BiFunction<List<Transaction>, Double, Pair<List<Transaction>, Double>> { transactions, total ->
                        transactions to total
                    }
                )
                .subscribeOnUi { (transactions, total) ->
                    progressBar.isVisible = false
                    setData(transactions, total) { transaction ->
                        activity?.let {
                            DetailedTransactionActivity.start(it, transaction)
                        }
                    }
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