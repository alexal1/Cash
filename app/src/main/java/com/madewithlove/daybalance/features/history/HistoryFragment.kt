/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.BaseViewModel
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.setOnClickListenerWithThrottle
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment() {

    companion object {

        fun create(): HistoryFragment = HistoryFragment()

    }


    private val baseViewModel: BaseViewModel by sharedViewModel()
    private val viewModel: HistoryViewModel by viewModel()
    private val ui = HistoryUI()
    private val dc = DisposableCache()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.floatingActionButton.setOnClickListenerWithThrottle {
            act.onBackPressed()
        }.cache(dc)
    }

    override fun onDestroyView() {
        dc.drain()
        super.onDestroyView()
    }

}