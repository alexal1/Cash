package com.alex_aladdin.cash

import android.app.Application
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class CashApp : Application() {

    val todayDate = Date()
    val currentDate = BehaviorSubject.createDefault(todayDate)

}