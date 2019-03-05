package com.alex_aladdin.cash

import android.app.Application
import com.alex_aladdin.cash.utils.currentLocale
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.Calendar.*

class CashApp : Application() {

    val todayDate: Date by lazy {
        GregorianCalendar.getInstance(currentLocale())
            .apply {
                set(HOUR, 0)
                set(MINUTE, 0)
                set(SECOND, 0)
                set(MILLISECOND, 0)
            }
            .time
    }

    val currentDate by lazy {
        BehaviorSubject.createDefault(todayDate)
    }

}