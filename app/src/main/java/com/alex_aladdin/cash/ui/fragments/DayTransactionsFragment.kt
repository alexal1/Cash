package com.alex_aladdin.cash.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.transactionsList
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.utils.subscribeOnUi
import com.alex_aladdin.cash.viewmodels.DayTransactionsViewModel
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent

class DayTransactionsFragment : Fragment() {

    companion object {

        private const val TYPE = "type"

        fun create(type: Type) = DayTransactionsFragment().apply {
            arguments = bundleOf(TYPE to type)
        }

    }


    private val dc = DisposableCache()
    private val type by lazy { arguments!!.getSerializable(TYPE) as Type }

    private lateinit var viewModel: DayTransactionsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity()).get(DayTransactionsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = container?.context?.frameLayout {
        transactionsList {
            id = R.id.transactions_list

            val transactionsSingle = if (type == Type.LOSS) {
                viewModel.dayLossTransactions
            } else {
                viewModel.dayGainTransactions
            }

            transactionsSingle.subscribeOnUi { transactions ->
                setData(transactions)
            }.cache(dc)
        }.lparams(matchParent, matchParent)
    }

    override fun onDestroyView() {
        dc.drain()
        super.onDestroyView()
    }


    enum class Type { GAIN, LOSS }

}