/**
 * Created by Alexander Mishchenko in 2020
 */

package com.madewithlove.daybalance.features.history

import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.main.MainFragment
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.RxErrorHandler
import com.madewithlove.daybalance.helpers.push.PushManager.Companion.OPENED_BY_PUSH
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.navigation.Navigator
import kotlinx.android.synthetic.main.activity_history.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.okButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryActivity : FragmentActivity(), Navigator {

    private val viewModel: HistoryViewModel by viewModel()
    private val analytics: Analytics by inject()
    private val errorHandler: RxErrorHandler by inject()
    private val dc = DisposableCache()

    private var confirmDeleteDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_history)

        requestedOrientation = if (CashApp.isDebugBuild) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }

        val isOpenedByPush = intent.getBooleanExtra(OPENED_BY_PUSH, false)
        if (isOpenedByPush) {
            analytics.clickOnPush()
        }

        errorHandler.errorMessageObservable.subscribeOnUi { errorMessage ->
            alert(Appcompat, errorMessage, string(R.string.error_title)) {
                okButton {}
            }.show()
        }.cache(dc)

        if (savedInstanceState == null) {
            setFragment(MainFragment.create()) {
                clicksBlockView.bringToFront()
            }
        }

        motionLayout.apply {
            setTransitionListener(object : TransitionAdapter() {
                override fun onTransitionChange(
                    ml: MotionLayout,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                    if (progress > 0f && !clicksBlockView.isVisible) {
                        clicksBlockView.isVisible = true
                    }
                }

                override fun onTransitionCompleted(ml: MotionLayout, currentId: Int) {
                    if (currentId == R.id.start) {
                        clicksBlockView.isVisible = false
                        transactionsList.stopScroll()
                        viewModel.dismissDeleteMode()
                    }

                    motionLayout.isSwitchedOff = true
                }
            })

            viewModel.openHistorySubject.subscribeOnUi {
                transitionToEnd()
            }.cache(dc)
        }

        transactionsList.apply {
            onScrollPositionChanged = { isTop ->
                motionLayout.isSwitchedOff = !isTop
            }

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

        emptyView.apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, screenSize().y)

            viewModel.historyStateObservable
                .map { it.showEmpty }
                .distinctUntilChanged()
                .subscribeOnUi { showEmpty ->
                    isVisible = showEmpty
                }
                .cache(dc)
        }

        floatingActionButton.apply {
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
                    onBackPressed()
                }
            }.cache(dc)
        }
    }

    override fun getNavigatorFragmentManager() = supportFragmentManager

    override fun getFragmentContainerId() = R.id.container

    override fun onBackPressed() {
        if (viewModel.historyState.deleteModeOn) {
            viewModel.dismissDeleteMode()
            return
        }

        if (motionLayout.currentState == R.id.end) {
            if (transactionsList.adapter?.itemCount.let { it != null && it > 0 }) {
                transactionsList.scrollToPosition(0)
            }
            motionLayout.transitionToStart()
            return
        }

        if (handleBackPress()) {
            return
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.dismissDeleteMode()
        dc.drain()
    }


    private fun openAreYouSureDialog(checkedItemsCount: Int) {
        confirmDeleteDialog?.dismiss()

        confirmDeleteDialog = AlertDialog.Builder(this)
            .setMessage(resources.getQuantityString(R.plurals.history_delete_confirm, checkedItemsCount, checkedItemsCount))
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