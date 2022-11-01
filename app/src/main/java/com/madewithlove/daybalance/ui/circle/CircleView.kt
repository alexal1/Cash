/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.circle

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.utils.TextFormatter
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import kotlin.math.abs
import kotlin.math.pow

class CircleView(context: Context) : ViewGroup(context) {

    companion object {

        private const val FULL_CIRCLE_ANIMATION_DURATION = 500
        private const val AMOUNT_TEXT_HEIGHT_RELATIVE_TO_RADIUS = 0.5f
        private const val LABEL_TEXT_HEIGHT_RELATIVE_TO_RADIUS = 0.25f
        private const val VERTICAL_OFFSET_RELATIVE_TO_RADIUS = 0.08f // hack to improve visual experience

    }


    private val circleDrawable = CircleDrawable(context)
    private val amountTextView = AppCompatTextView(context)
    private val labelTextView = AppCompatTextView(context)

    private var animator: Animator? = null


    init {
        backgroundDrawable = circleDrawable

        amountTextView.apply {
            textColorResource = R.color.white
            gravity = CENTER
            typeface = Typeface.MONOSPACE

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                1,
                128,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )

            addView(this)
        }

        labelTextView.apply {
            textResource = R.string.circle_text
            textColorResource = R.color.white_80
            gravity = CENTER

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                1,
                128,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )

            addView(this)
        }
    }


    fun setData(state: CircleState) {
        val (newMoney, newProgress, isPast) = state

        labelTextView.textResource = if (isPast) R.string.circle_text_past else R.string.circle_text

        if (newMoney == null) {
            circleDrawable.progress = newProgress
            amountTextView.text = ""
            return
        }

        val newAmount = newMoney.amount.toFloat()
        val prevAmount = Money.by(amountTextView.text.toString()).amount.toFloat()

        val prevProgress = circleDrawable.progress

        animator?.cancel()
        animator = ValueAnimator.ofFloat(prevProgress, newProgress).apply {
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val k = (progress - prevProgress) / (newProgress - prevProgress)
                val amount = prevAmount + k * (newAmount - prevAmount)

                circleDrawable.progress = progress
                amountTextView.text = TextFormatter.formatMoney(amount, withGrouping = false, withSpaceAfterSign = false)
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    amountTextView.text = TextFormatter.formatMoney(newMoney, withGrouping = false, withSpaceAfterSign = false)
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationStart(animation: Animator) {
                }
            })

            val diff = abs(newProgress - prevProgress)
            duration = (((diff - 1).pow(7) + 1) * FULL_CIRCLE_ANIMATION_DURATION).toLong()
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Set size
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = minOf(width, height)
        setMeasuredDimension(size, size)

        // Measure amount text
        val radius = measuredWidth / 2f
        val amountHeight = radius * AMOUNT_TEXT_HEIGHT_RELATIVE_TO_RADIUS
        val amountHeightSpec = MeasureSpec.makeMeasureSpec(amountHeight.toInt(), EXACTLY)
        val amountWidth = ((radius.pow(2) - amountHeight.pow(2)).pow(0.5f) - circleDrawable.thickness) * 2
        val amountWidthSpec = MeasureSpec.makeMeasureSpec(amountWidth.toInt(), EXACTLY)
        amountTextView.measure(amountWidthSpec, amountHeightSpec)

        // Measure label text
        val labelHeight = radius * LABEL_TEXT_HEIGHT_RELATIVE_TO_RADIUS
        val labelHeightSpec = MeasureSpec.makeMeasureSpec(labelHeight.toInt(), EXACTLY)
        val labelWidth = ((radius.pow(2) - labelHeight.pow(2)).pow(0.5f) - circleDrawable.thickness) * 2
        val labelWidthSpec = MeasureSpec.makeMeasureSpec(labelWidth.toInt(), EXACTLY)
        labelTextView.measure(labelWidthSpec, labelHeightSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // Set circle bounds
        val size = measuredWidth
        val circleLeft = (right - left - size) / 2
        val circleTop = (bottom - top - size) / 2
        val circleRight = circleLeft + size
        val circleBottom = circleTop + size
        circleDrawable.circleBounds.set(circleLeft, circleTop, circleRight, circleBottom)

        // Set amount text bounds
        val radius = measuredWidth / 2f
        val verticalOffset = (radius * VERTICAL_OFFSET_RELATIVE_TO_RADIUS).toInt()
        val amountHeight = radius * AMOUNT_TEXT_HEIGHT_RELATIVE_TO_RADIUS
        val amountHalfWidth = (radius.pow(2) - amountHeight.pow(2)).pow(0.5f) - circleDrawable.thickness
        val amountLeft = (radius - amountHalfWidth).toInt() + circleLeft
        val amountTop = (radius - amountHeight).toInt() + circleTop + verticalOffset
        val amountRight = (radius + amountHalfWidth).toInt() + circleLeft
        val amountBottom = radius.toInt() + circleTop + verticalOffset
        amountTextView.layout(amountLeft, amountTop, amountRight, amountBottom)

        // Set label text bounds
        val labelHeight = radius * LABEL_TEXT_HEIGHT_RELATIVE_TO_RADIUS
        val labelHalfWidth = (radius.pow(2) - labelHeight.pow(2)).pow(0.5f) - circleDrawable.thickness
        val labelLeft = (radius - labelHalfWidth).toInt() + circleLeft
        val labelTop = radius.toInt() + circleTop + verticalOffset
        val labelRight = (radius + labelHalfWidth).toInt() + circleLeft
        val labelBottom = (radius + labelHeight).toInt() + circleTop + verticalOffset
        labelTextView.layout(labelLeft, labelTop, labelRight, labelBottom)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        animator?.cancel()
    }


    data class CircleState(
        val amount: Money? = null,
        val progress: Float = 0f,
        val isPast: Boolean = false
    )

}