package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.repository.specification.DayGainSpecification
import com.alex_aladdin.cash.repository.specification.DayLossSpecification
import io.reactivex.Observable
import java.util.*

class DayTransactionsViewModel(application: Application) : AndroidViewModel(application) {

    val app = application as CashApp
    val repository = app.repository
    val dayLossTransactions = repository.query(DayLossSpecification(app.currentDate.value!!))
    val dayGainTransactions = repository.query(DayGainSpecification(app.currentDate.value!!))
    val currentDateObservable: Observable<Date> = app.currentDate

}