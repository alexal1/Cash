package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.specification.DayGainSpecification
import com.alex_aladdin.cash.repository.specification.DayLossSpecification
import io.reactivex.Observable
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class DayTransactionsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    val app = application as CashApp
    val repository: TransactionsRepository by inject()
    val dayLossTransactions = repository.query(DayLossSpecification(app.currentDate.value!!))
    val dayGainTransactions = repository.query(DayGainSpecification(app.currentDate.value!!))
    val currentDateObservable: Observable<Date> = app.currentDate

}