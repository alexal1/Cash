/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.widget.TextViewCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.ui.KeypadView
import com.madewithlove.daybalance.utils.anko.appCompatTextView
import com.madewithlove.daybalance.utils.anko.keypadView
import com.madewithlove.daybalance.utils.expandHitArea
import com.madewithlove.daybalance.utils.screenSize
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class CreateUI : AnkoComponent<CreateFragment> {

    lateinit var toolbar: Toolbar
    lateinit var miniTextView: TextView
    lateinit var inputTextView: TextView
    lateinit var commentEditText: EditText
    lateinit var keypadView: KeypadView


    override fun createView(ui: AnkoContext<CreateFragment>): View = with(ui) {
        constraintLayout {
            backgroundColorResource = R.color.deepDark
            isClickable = true
            isFocusable = true

            toolbar = toolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_arrow_back
                backgroundColorResource = R.color.soft_dark
            }.lparams(matchConstraint, dimen(R.dimen.toolbar_height))

            val keypadHeight = minOf(dip(320), (ctx.screenSize().y * 0.5f).toInt())

            val inputIcon = imageView {
                id = View.generateViewId()
                scaleType = ImageView.ScaleType.FIT_XY

                setImageResource(R.drawable.ic_input)
            }.lparams(dip(32), dimen(R.dimen.input_icon_height)) {
                leftMargin = dip(32)
                topMargin = (ctx.screenSize().y - dimen(R.dimen.toolbar_height) - dimen(R.dimen.input_icon_height) - keypadHeight - dimen(R.dimen.large_button_height)) / 2
            }

            miniTextView = textView {
                id = R.id.mini_text_view
                textColorResource = R.color.white_80
                textSize = 12f
                includeFontPadding = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END

                expandHitArea(3f)
            }.lparams(matchConstraint, wrapContent) {
                leftMargin = dip(18)
                rightMargin = dip(16)
            }

            inputTextView = appCompatTextView {
                id = R.id.input_text_view
                textColorResource = R.color.white
                gravity = CENTER_VERTICAL
                maxLines = 1

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this,
                    1,
                    128,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
                )
            }.lparams(matchConstraint, dimen(R.dimen.input_icon_height)) {
                leftMargin = dip(16)
                rightMargin = dip(16)
            }

            commentEditText = editText {
                id = R.id.comment_edit_text
                textColorResource = R.color.white
                textSize = 32f
                gravity = CENTER_VERTICAL
                imeOptions = EditorInfo.IME_ACTION_DONE
                singleLine = true
                background = null
            }.lparams(matchConstraint, dimen(R.dimen.input_icon_height)) {
                leftMargin = dip(16)
                rightMargin = dip(16)
            }

            val keypadSpace = space {
                id = View.generateViewId()
            }.lparams(matchConstraint, keypadHeight)

            keypadView = keypadView {
                id = R.id.keypad_view
            }.lparams(matchConstraint, keypadHeight)


            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of inputIcon to START of PARENT_ID,
                    TOP of inputIcon to BOTTOM of toolbar
                )

                connect(
                    START of miniTextView to END of inputIcon,
                    END of miniTextView to END of PARENT_ID,
                    BOTTOM of miniTextView to TOP of inputIcon
                )

                connect(
                    START of inputTextView to END of inputIcon,
                    END of inputTextView to END of PARENT_ID,
                    TOP of inputTextView to TOP of inputIcon,
                    BOTTOM of inputTextView to BOTTOM of inputIcon
                )

                connect(
                    START of commentEditText to END of inputIcon,
                    END of commentEditText to END of PARENT_ID,
                    TOP of commentEditText to TOP of inputIcon,
                    BOTTOM of commentEditText to BOTTOM of inputIcon
                )

                connect(
                    START of keypadSpace to START of PARENT_ID,
                    END of keypadSpace to END of PARENT_ID,
                    BOTTOM of keypadSpace to BOTTOM of PARENT_ID
                )

                connect(
                    START of keypadView to START of PARENT_ID,
                    END of keypadView to END of PARENT_ID,
                    TOP of keypadView to TOP of keypadSpace
                )
            }
        }
    }

}