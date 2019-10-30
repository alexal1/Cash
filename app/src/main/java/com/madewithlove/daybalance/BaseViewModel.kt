/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.subjects.PublishSubject

class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val openHistorySubject = PublishSubject.create<Unit>()

}