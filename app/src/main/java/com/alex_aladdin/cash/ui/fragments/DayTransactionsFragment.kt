package com.alex_aladdin.cash.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.transactionsList
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent

class DayTransactionsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = container?.context?.frameLayout {
        transactionsList {
            id = R.id.transactions_list
        }.lparams(matchParent, matchParent)
    }

}