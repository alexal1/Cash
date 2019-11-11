/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.utils.DisposableCache
import com.madewithlove.daybalance.utils.cache
import com.madewithlove.daybalance.utils.setOnClickListenerWithThrottle
import com.madewithlove.daybalance.utils.subscribeOnUi
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment() {

    companion object {

        fun create(): HistoryFragment = HistoryFragment()

    }


    private val viewModel: HistoryViewModel by viewModel()
    private val ui: HistoryUI get() = historyUI ?: HistoryUI().also { historyUI = it }
    private val dc = DisposableCache()

    private var historyUI: HistoryUI? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.transactionsList.apply {
            checkSubject.subscribe(viewModel.checkConsumer).cache(dc)
            uncheckSubject.subscribe(viewModel.uncheckConsumer).cache(dc)

            viewModel.historyStateObservable
                .map { it.items to it.deleteModeOn }
                .distinctUntilChanged()
                .subscribeOnUi { (items, deleteModeOn) ->
                    setData(items, deleteModeOn)
                }
                .cache(dc)
        }

        ui.floatingActionButton.setOnClickListenerWithThrottle {
            act.onBackPressed()
        }.cache(dc)

        ui.emptyView.apply {
            viewModel.historyStateObservable
                .map { it.showEmpty }
                .distinctUntilChanged()
                .subscribeOnUi { showEmpty ->
                    isVisible = showEmpty
                }
                .cache(dc)
        }

        ui.loadingView.apply {
            viewModel.historyStateObservable
                .map { it.showLoading }
                .distinctUntilChanged()
                .subscribeOnUi { showLoading ->
                    isVisible = showLoading
                }
                .cache(dc)
        }

        view.post {
            startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        dc.drain()
        historyUI = null
        super.onDestroyView()
    }

}