package com.alex_aladdin.cash.ui.fragments

import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.graphics.contains
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.DisposableCache
import com.alex_aladdin.cash.utils.cache
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel.CalculatorAction
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel.CalculatorActionType.*
import com.jakewharton.rxbinding3.view.clicks
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint

class CalculatorFragment : Fragment() {

    private val dc = DisposableCache()

    private lateinit var viewModel: NewTransactionViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(NewTransactionViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        container?.context?.constraintLayout {
            backgroundColorResource = R.color.soft_dark

            val operatorsBg = view {
                id = View.generateViewId()
                backgroundColorResource = R.color.smoke
            }.lparams(matchConstraint, dip(40))

            val buttonPlus = getButton(CalculatorAction(PLUS))
            val buttonMinus = getButton(CalculatorAction(MINUS))
            val buttonMultiply = getButton(CalculatorAction(MULTIPLY))
            val buttonDivide = getButton(CalculatorAction(DIVIDE))
            val buttonEquals = getButton(CalculatorAction(EQUALS))

            val button1 = getButton(CalculatorAction(NUMBER, 1))
            val button2 = getButton(CalculatorAction(NUMBER, 2))
            val button3 = getButton(CalculatorAction(NUMBER, 3))
            val button4 = getButton(CalculatorAction(NUMBER, 4))
            val button5 = getButton(CalculatorAction(NUMBER, 5))
            val button6 = getButton(CalculatorAction(NUMBER, 6))
            val button7 = getButton(CalculatorAction(NUMBER, 7))
            val button8 = getButton(CalculatorAction(NUMBER, 8))
            val button9 = getButton(CalculatorAction(NUMBER, 9))
            val buttonDot = getButton(CalculatorAction(DOT))
            val button0 = getButton(CalculatorAction(NUMBER, 0))
            val buttonBackspace = getButton(CalculatorAction(BACKSPACE))

            val verticalSeparator1 = textView {
                id = View.generateViewId()
                backgroundColorResource = R.color.palladium
            }.lparams(dip(1), matchConstraint)

            val verticalSeparator2 = view {
                id = View.generateViewId()
                backgroundColorResource = R.color.palladium
            }.lparams(dip(1), matchConstraint)

            val horizontalSeparator1 = view {
                id = View.generateViewId()
                backgroundColorResource = R.color.palladium
            }.lparams(matchConstraint, dip(1))

            val horizontalSeparator2 = view {
                id = View.generateViewId()
                backgroundColorResource = R.color.palladium
            }.lparams(matchConstraint, dip(1))

            val horizontalSeparator3 = view {
                id = View.generateViewId()
                backgroundColorResource = R.color.palladium
            }.lparams(matchConstraint, dip(1))


            applyConstraintSet {
                connect(
                    START of operatorsBg to START of PARENT_ID,
                    END of operatorsBg to END of PARENT_ID,
                    TOP of operatorsBg to TOP of PARENT_ID
                )

                connect(
                    TOP of buttonPlus to TOP of operatorsBg,
                    BOTTOM of buttonPlus to BOTTOM of operatorsBg,
                    START of buttonPlus to START of operatorsBg,
                    END of buttonPlus to START of buttonMinus
                )

                connect(
                    TOP of buttonMinus to TOP of operatorsBg,
                    BOTTOM of buttonMinus to BOTTOM of operatorsBg,
                    START of buttonMinus to END of buttonPlus,
                    END of buttonMinus to START of buttonMultiply
                )

                connect(
                    TOP of buttonMultiply to TOP of operatorsBg,
                    BOTTOM of buttonMultiply to BOTTOM of operatorsBg,
                    START of buttonMultiply to END of buttonMinus,
                    END of buttonMultiply to START of buttonDivide
                )

                connect(
                    TOP of buttonDivide to TOP of operatorsBg,
                    BOTTOM of buttonDivide to BOTTOM of operatorsBg,
                    START of buttonDivide to END of buttonMultiply,
                    END of buttonDivide to START of buttonEquals
                )

                connect(
                    TOP of buttonEquals to TOP of operatorsBg,
                    BOTTOM of buttonEquals to BOTTOM of operatorsBg,
                    START of buttonEquals to END of buttonDivide,
                    END of buttonEquals to END of PARENT_ID
                )

                connect(
                    TOP of button1 to BOTTOM of operatorsBg,
                    BOTTOM of button1 to TOP of button4,
                    START of button1 to START of PARENT_ID,
                    END of button1 to START of button2
                )

                connect(
                    TOP of button2 to BOTTOM of operatorsBg,
                    BOTTOM of button2 to TOP of button4,
                    START of button2 to END of button1,
                    END of button2 to START of button3
                )

                connect(
                    TOP of button3 to BOTTOM of operatorsBg,
                    BOTTOM of button3 to TOP of button4,
                    START of button3 to END of button2,
                    END of button3 to END of PARENT_ID
                )

                connect(
                    TOP of button4 to BOTTOM of button1,
                    BOTTOM of button4 to TOP of button7,
                    START of button4 to START of PARENT_ID,
                    END of button4 to START of button5
                )

                connect(
                    TOP of button5 to BOTTOM of button1,
                    BOTTOM of button5 to TOP of button7,
                    START of button5 to END of button4,
                    END of button5 to START of button6
                )

                connect(
                    TOP of button6 to BOTTOM of button1,
                    BOTTOM of button6 to TOP of button7,
                    START of button6 to END of button5,
                    END of button6 to END of PARENT_ID
                )

                connect(
                    TOP of button7 to BOTTOM of button4,
                    BOTTOM of button7 to TOP of buttonDot,
                    START of button7 to START of PARENT_ID,
                    END of button7 to START of button8
                )

                connect(
                    TOP of button8 to BOTTOM of button4,
                    BOTTOM of button8 to TOP of buttonDot,
                    START of button8 to END of button7,
                    END of button8 to START of button9
                )

                connect(
                    TOP of button9 to BOTTOM of button4,
                    BOTTOM of button9 to TOP of buttonDot,
                    START of button9 to END of button8,
                    END of button9 to END of PARENT_ID
                )

                connect(
                    TOP of buttonDot to BOTTOM of button7,
                    BOTTOM of buttonDot to BOTTOM of PARENT_ID,
                    START of buttonDot to START of PARENT_ID,
                    END of buttonDot to START of button0
                )

                connect(
                    TOP of button0 to BOTTOM of button7,
                    BOTTOM of button0 to BOTTOM of PARENT_ID,
                    START of button0 to END of buttonDot,
                    END of button0 to START of buttonBackspace
                )

                connect(
                    TOP of buttonBackspace to BOTTOM of button7,
                    BOTTOM of buttonBackspace to BOTTOM of PARENT_ID,
                    START of buttonBackspace to END of button0,
                    END of buttonBackspace to END of PARENT_ID
                )

                connect(
                    TOP of verticalSeparator1 to BOTTOM of operatorsBg,
                    BOTTOM of verticalSeparator1 to BOTTOM of PARENT_ID,
                    START of verticalSeparator1 to END of button1
                )

                connect(
                    TOP of verticalSeparator2 to BOTTOM of operatorsBg,
                    BOTTOM of verticalSeparator2 to BOTTOM of PARENT_ID,
                    START of verticalSeparator2 to END of button2
                )

                connect(
                    TOP of horizontalSeparator1 to BOTTOM of button1,
                    START of horizontalSeparator1 to START of PARENT_ID,
                    END of horizontalSeparator1 to END of PARENT_ID
                )

                connect(
                    TOP of horizontalSeparator2 to BOTTOM of button4,
                    START of horizontalSeparator2 to START of PARENT_ID,
                    END of horizontalSeparator2 to END of PARENT_ID
                )

                connect(
                    TOP of horizontalSeparator3 to BOTTOM of button7,
                    START of horizontalSeparator3 to START of PARENT_ID,
                    END of horizontalSeparator3 to END of PARENT_ID
                )
            }
        }

    private fun _ConstraintLayout.getButton(action: CalculatorAction) = when (action.type) {
        NUMBER -> textView {
            id = View.generateViewId()
            gravity = CENTER
            isClickable = true
            textColorResource = R.color.white_80
            textSize = 24f
            text = action.payload.toString()
            backgroundColor = Color.TRANSPARENT
        }.lparams(matchConstraint, matchConstraint)

        DOT -> textView {
            id = View.generateViewId()
            gravity = CENTER
            isClickable = true
            textColorResource = R.color.white_80
            textSize = 24f
            text = "."
            backgroundColor = Color.TRANSPARENT
        }.lparams(matchConstraint, matchConstraint)

        BACKSPACE -> imageView {
            id = View.generateViewId()
            imageResource = R.drawable.ic_backspace
            scaleType = ImageView.ScaleType.CENTER
            isClickable = true
            backgroundColor = Color.TRANSPARENT
        }.lparams(matchConstraint, matchConstraint)

        PLUS, MINUS, MULTIPLY, DIVIDE, EQUALS -> textView {
            id = View.generateViewId()
            gravity = CENTER
            isClickable = true
            textColorResource = R.color.white
            textSize = 24f
            text = when (action.type) {
                PLUS -> "+"
                MINUS -> "-"
                MULTIPLY -> "×"
                DIVIDE -> "÷"
                EQUALS -> "="
                else -> throw IllegalArgumentException()
            }
            backgroundColor = Color.TRANSPARENT
            setPadding(0, 0, 0, dip(2))
        }.lparams(matchConstraint, matchConstraint)
    }.apply {
        clicks()
            .map { action }
            .subscribe(viewModel.calculatorActionConsumer)
            .cache(dc)

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    backgroundColorResource = when (action.type) {
                        NUMBER, DOT, BACKSPACE -> R.color.steel_gray
                        else -> R.color.fog_white
                    }

                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backgroundColor = Color.TRANSPARENT

                    if (getRect().contains(Point(event.x.toInt() + left, event.y.toInt() + top))) {
                        performClick()
                    }

                    true
                }

                else -> false
            }
        }
    }

    private fun View.getRect(): Rect {
        val rect = Rect()
        getHitRect(rect)
        return rect
    }

}