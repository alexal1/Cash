/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class CreateViewModel(application: Application) : AndroidViewModel(application) {

    enum class Type { GAIN, LOSS }

}