/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madewithlove.daybalance.BaseViewModel
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.features.create.CreateFragment
import com.madewithlove.daybalance.features.create.CreateViewModel
import com.madewithlove.daybalance.helpers.Analytics
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.utils.*
import com.madewithlove.daybalance.utils.navigation.FragmentNavigator
import io.reactivex.Observable
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textResource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class MainFragment : Fragment(), FragmentNavigator {
    override fun getNavigatorFragmentManager() = childFragmentManager

    override fun getFragmentContainerId() = R.id.main_container

    companion object {

        fun create(): MainFragment = MainFragment()

    }


    private val baseViewModel: BaseViewModel by sharedViewModel()
    private val viewModel: MainViewModel by viewModel()
    private val datesManager: DatesManager by inject()
    private val analytics: Analytics by inject()
    private val calendarDialog by lazy { createCalendarDialog() }
    private val calendar = GregorianCalendar.getInstance()
    private val ui = MainUI()
    private val dc = DisposableCache()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.datesRecyclerView.apply {
            init(ctx.currentLocale())
            centerItemClickObservable.subscribeOnUi { openCalendarDialog() }.cache(dc)

            val lastDate = AtomicLong()

            dateObservable.subscribe { scrolledDate ->
                lastDate.set(scrolledDate.time)
                datesManager.updateCurrentDate(scrolledDate)
            }.cache(dc)

            viewModel.mainStateObservable
                .map { it.currentDate }
                .distinctUntilChanged()
                .filter { currentDate -> currentDate.time != lastDate.get() }
                .subscribeOnUi { currentDate ->
                    setDate(currentDate)
                }
                .cache(dc)
        }

        ui.weekdayText.apply {
            viewModel.mainStateObservable
                .map { it.weekday to it.isToday }
                .distinctUntilChanged()
                .subscribeOnUi { (weekday, isToday) ->
                    text = if (isToday) {
                        "$weekday (${string(R.string.today)})"
                    } else {
                        weekday
                    }
                }
                .cache(dc)
        }

        ui.gainButton.apply {
            setOnClickListenerWithThrottle {
                val fragment = CreateFragment.create(CreateViewModel.Type.GAIN)
                addFragment(fragment)
            }.cache(dc)
        }

        ui.lossButton.apply {
            setOnClickListenerWithThrottle {
                val fragment = CreateFragment.create(CreateViewModel.Type.LOSS)
                addFragment(fragment)
            }.cache(dc)
        }

        ui.largeButtonBackground.apply {
            setOnClickListenerWithThrottle {
                when (viewModel.mainState.largeButtonType) {
                    MainViewModel.LargeButtonType.HISTORY -> {
                        baseViewModel.openHistorySubject.onNext(Unit)
                    }
                }
            }.cache(dc)
        }

        ui.largeButtonText.apply {
            viewModel.mainStateObservable
                .map { it.largeButtonType }
                .distinctUntilChanged()
                .subscribeOnUi { largeButtonType ->
                    when (largeButtonType!!) {
                        MainViewModel.LargeButtonType.HISTORY -> {
                            textResource = R.string.history_title
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0)
                        }
                    }
                }
                .cache(dc)
        }

        Observable.timer(1, TimeUnit.SECONDS).subscribeOnUi {
            calendarDialog // pre initialize
        }.cache(dc)
    }

    override fun onDestroyView() {
        dc.drain()
        super.onDestroyView()
    }


    private fun createCalendarDialog(): DatePickerDialog {
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            val date = calendar.time
            analytics.pickCalendarDate()
            datesManager.updateCurrentDate(date)
        }

        return DatePickerDialog(ctx, listener, 1970, 0, 1)
    }

    private fun openCalendarDialog() {
        calendar.time = datesManager.currentDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        calendarDialog.updateDate(year, month, day)
        calendarDialog.show()
    }

}