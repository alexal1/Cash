/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.view.View
import com.madewithlove.daybalance.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.constraint.layout.constraintLayout

class CreateUI : AnkoComponent<CreateFragment> {

    override fun createView(ui: AnkoContext<CreateFragment>): View = with(ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark
            isClickable = true
            isFocusable = true
        }
    }

}