/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.history

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.repository.specifications.HistorySpecification
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.navigation.BackPressHandler
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class HistoryFragment : Fragment(), BackPressHandler {

    companion object {

        private const val FILTER = "filter"

        fun create(filter: HistorySpecification.Filter): HistoryFragment = HistoryFragment().apply {
            arguments = bundleOf(FILTER to filter)
        }

    }


    private val filter by lazy { arguments!!.getSerializable(FILTER) as HistorySpecification.Filter }
    private val viewModel: HistoryViewModel by viewModel { parametersOf(filter) }
    private val ui: HistoryUI get() = historyUI ?: HistoryUI().also { historyUI = it }
    private val dc = DisposableCache()

    private var historyUI: HistoryUI? = null
    private var confirmDeleteDialog: AlertDialog? = null


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

        ui.floatingActionButton.apply {
            viewModel.historyStateObservable
                .map { it.deleteModeOn }
                .distinctUntilChanged()
                .subscribeOnUi { deleteModeOn ->
                    if (deleteModeOn) {
                        backgroundTintList = ColorStateList.valueOf(color(R.color.arterial_blood))
                        setImageResource(R.drawable.ic_trash)
                    } else {
                        backgroundTintList = ColorStateList.valueOf(color(R.color.blue))
                        setImageResource(R.drawable.ic_double_arrow)
                    }
                }
                .cache(dc)

            setOnClickListenerWithThrottle {
                if (viewModel.historyState.deleteModeOn) {
                    openAreYouSureDialog(viewModel.historyState.checkedTransactions.count())
                } else {
                    act.onBackPressed()
                }
            }.cache(dc)
        }

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

    override fun handleBackPress(): Boolean {
        if (viewModel.historyState.deleteModeOn) {
            viewModel.dismissDeleteMode()
            return true
        }

        return false
    }

    override fun onDestroyView() {
        confirmDeleteDialog?.dismiss()
        dc.drain()
        historyUI = null
        super.onDestroyView()
    }


    private fun openAreYouSureDialog(checkedItemsCount: Int) {
        confirmDeleteDialog?.dismiss()

        confirmDeleteDialog = AlertDialog.Builder(ctx)
            .setMessage(ctx.resources.getQuantityString(R.plurals.history_delete_confirm, checkedItemsCount, checkedItemsCount))
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                viewModel.deleteCheckedItems()
                dialog.dismiss()
            }
            .show()
    }

}