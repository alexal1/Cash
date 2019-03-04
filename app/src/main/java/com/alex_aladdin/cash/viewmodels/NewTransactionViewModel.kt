package com.alex_aladdin.cash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alex_aladdin.cash.CashApp
import io.reactivex.Observable
import java.util.*

class NewTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CashApp

    val currentDateObservable: Observable<Date> = app.currentDate


    enum class Type { GAIN, LOSS }

}