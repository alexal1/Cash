/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.color
import com.madewithlove.daybalance.utils.drawable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp

class KeypadView(context: Context) : View(context) {

    val actionObservable: Observable<Action>

    private val actionSubject = PublishSubject.create<Action>()
    private val backgroundColor = context.color(R.color.soft_dark)
    private val symbolBounds = Rect()

    private val backgroundPaint = Paint().apply {
        isAntiAlias = false
    }

    private val symbolPaint = Paint().apply {
        isAntiAlias = true
        color = context.color(R.color.white)
        textSize = context.sp(24).toFloat()
    }

    private val separatorPaint = Paint().apply {
        isAntiAlias = false
        color = context.color(R.color.palladium)
        strokeWidth = context.dip(1).toFloat()
    }

    private val button0 = Button(Action(Type.NUMBER, 0), null, context.color(R.color.palladium), Rect(0, 3, 2, 4), Rect(0, 3, 1, 4), symbol = "0")
    private val button1 = Button(Action(Type.NUMBER, 1), null, context.color(R.color.palladium), Rect(0, 2, 1, 3), symbol = "1")
    private val button2 = Button(Action(Type.NUMBER, 2), null, context.color(R.color.palladium), Rect(1, 2, 2, 3), symbol = "2")
    private val button3 = Button(Action(Type.NUMBER, 3), null, context.color(R.color.palladium), Rect(2, 2, 3, 3), symbol = "3")
    private val button4 = Button(Action(Type.NUMBER, 4), null, context.color(R.color.palladium), Rect(0, 1, 1, 2), symbol = "4")
    private val button5 = Button(Action(Type.NUMBER, 5), null, context.color(R.color.palladium), Rect(1, 1, 2, 2), symbol = "5")
    private val button6 = Button(Action(Type.NUMBER, 6), null, context.color(R.color.palladium), Rect(2, 1, 3, 2), symbol = "6")
    private val button7 = Button(Action(Type.NUMBER, 7), null, context.color(R.color.palladium), Rect(0, 0, 1, 1), symbol = "7")
    private val button8 = Button(Action(Type.NUMBER, 8), null, context.color(R.color.palladium), Rect(1, 0, 2, 1), symbol = "8")
    private val button9 = Button(Action(Type.NUMBER, 9), null, context.color(R.color.palladium), Rect(2, 0, 3, 1), symbol = "9")
    private val buttonDot = Button(Action(Type.DOT), null, context.color(R.color.palladium), Rect(2, 3, 3, 4), symbol = ".")
    private val buttonBackspace = Button(Action(Type.BACKSPACE), context.color(R.color.fog_white_80), context.color(R.color.fog_white), Rect(3, 0, 4, 1), icon = context.drawable(R.drawable.ic_backspace))
    private val buttonEnter = Button(Action(Type.ENTER), context.color(R.color.blue_80), context.color(R.color.blue), Rect(3, 1, 4, 4), Rect(3, 3, 4, 4), icon = context.drawable(R.drawable.ic_enter))

    private val buttons = listOf(
        button0,
        button1,
        button2,
        button3,
        button4,
        button5,
        button6,
        button7,
        button8,
        button9,
        buttonDot,
        buttonBackspace,
        buttonEnter
    )

    private var buttonWidth = 0f
    private var buttonHeight = 0f
    private var selectedButton: Button? = null


    init {
        actionObservable = actionSubject
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        buttonWidth = measuredWidth / 4f
        buttonHeight = measuredHeight / 4f
    }


    override fun onDraw(canvas: Canvas) {
        canvas.apply {
            drawColor(backgroundColor)

            buttons.forEach { button ->
                // Draw background
                if (selectedButton == button) {
                    backgroundPaint.color = button.selectedColor
                    drawRect(button.bounds.left * buttonWidth, button.bounds.top * buttonHeight, button.bounds.right * buttonWidth, button.bounds.bottom * buttonHeight, backgroundPaint)
                } else if (button.backgroundColor != null) {
                    backgroundPaint.color = button.backgroundColor
                    drawRect(button.bounds.left * buttonWidth, button.bounds.top * buttonHeight, button.bounds.right * buttonWidth, button.bounds.bottom * buttonHeight, backgroundPaint)
                }

                // Draw symbol
                button.symbol?.let { symbol ->
                    symbolPaint.getTextBounds(symbol, 0, 1, symbolBounds)
                    val x = (button.boundsForSymbol.left * buttonWidth + button.boundsForSymbol.right * buttonWidth - symbolBounds.width()) / 2f
                    val y = (button.boundsForSymbol.top * buttonHeight + button.boundsForSymbol.bottom * buttonHeight + symbolBounds.height()) / 2f
                    drawText(symbol, x, y, symbolPaint)
                }

                // Draw icon
                button.icon?.let { drawable ->
                    val x = (button.boundsForSymbol.left * buttonWidth + button.boundsForSymbol.right * buttonWidth - drawable.intrinsicWidth) / 2f
                    val y = (button.boundsForSymbol.top * buttonHeight + button.boundsForSymbol.bottom * buttonHeight - drawable.intrinsicHeight) / 2f
                    drawable.bounds.set(x.toInt(), y.toInt(), x.toInt() + drawable.intrinsicWidth, y.toInt() + drawable.intrinsicHeight)
                    drawable.draw(canvas)
                }
            }

            drawSeparator(1, 0, 1, 3)
            drawSeparator(2, 0, 2, 4)
            drawSeparator(3, 0, 3, 4)
            drawSeparator(0, 1, 3, 1)
            drawSeparator(0, 2, 3, 2)
            drawSeparator(0, 3, 3, 3)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (button in buttons) {
                    if (event.x > button.bounds.left * buttonWidth && event.x < button.bounds.right * buttonWidth && event.y > button.bounds.top * buttonHeight && event.y < button.bounds.bottom * buttonHeight) {
                        selectedButton = button
                        invalidate()
                        break
                    }
                }

                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectedButton?.let { button ->
                    if (event.x > button.bounds.left * buttonWidth && event.x < button.bounds.right * buttonWidth && event.y > button.bounds.top * buttonHeight && event.y < button.bounds.bottom * buttonHeight) {
                        actionSubject.onNext(button.action)
                    }
                }

                selectedButton = null
                invalidate()
            }
        }

        return super.onTouchEvent(event)
    }


    private fun Canvas.drawSeparator(x1: Int, y1: Int, x2: Int, y2: Int) {
        drawLine(x1 * buttonWidth, y1 * buttonHeight, x2 * buttonWidth, y2 * buttonHeight, separatorPaint)
    }


    private data class Button(
        val action: Action,
        val backgroundColor: Int?,
        val selectedColor: Int,
        val bounds: Rect,
        val boundsForSymbol: Rect = bounds,
        val symbol: String? = null,
        val icon: Drawable? = null
    )


    enum class Type { NUMBER, DOT, BACKSPACE, ENTER }


    data class Action(val type: Type, val payload: Int? = null)

}