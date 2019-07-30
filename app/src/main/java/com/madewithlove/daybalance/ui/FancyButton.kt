/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity.CENTER
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.graphics.contains
import com.madewithlove.daybalance.R
import org.jetbrains.anko.*

class FancyButton(context: Context) : _FrameLayout(context) {

    companion object {

        private const val DURATION = 200L
        private const val DEFAULT_ALPHA = 230

    }


    private val viewRect: Rect by lazy {
        val rect = Rect()
        getHitRect(rect)
        rect
    }

    private lateinit var textView: TextView


    fun init(glowColor: Int, gradStartColor: Int, gradEndColor: Int) {
        val glowDrawable = getGlowDrawable(glowColor)
        val fillDrawable = getFillDrawable(gradStartColor, gradEndColor)

        val fadeIn: AnimatorSet = AnimatorSet().apply {
            playTogether(
                ValueAnimator.ofInt(0, 255).apply {
                    duration = DURATION
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { glowDrawable.alpha = it.animatedValue as Int }
                },
                ValueAnimator.ofInt(DEFAULT_ALPHA, 255).apply {
                    duration = DURATION
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { fillDrawable.alpha = it.animatedValue as Int }
                }
            )
        }

        val fadeOut: AnimatorSet = AnimatorSet().apply {
            playTogether(
                ValueAnimator.ofInt(255, 0).apply {
                    duration = DURATION
                    interpolator = AccelerateInterpolator()
                    addUpdateListener { glowDrawable.alpha = it.animatedValue as Int }
                },
                ValueAnimator.ofInt(255, DEFAULT_ALPHA).apply {
                    duration = DURATION
                    interpolator = AccelerateInterpolator()
                    addUpdateListener { fillDrawable.alpha = it.animatedValue as Int }
                }
            )
        }

        background = glowDrawable

        textView = textView {
            background = fillDrawable
            gravity = CENTER
            textResource = R.string.loss
            textColorResource = R.color.white
            textSize = 14f
            letterSpacing = 0.02f
            typeface = Typeface.DEFAULT_BOLD
        }.lparams(matchParent, matchParent)

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    fadeOut.cancel()
                    fadeIn.start()

                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    fadeIn.cancel()
                    fadeOut.start()

                    if (viewRect.contains(Point(event.x.toInt() + left, event.y.toInt() + top))) {
                        performClick()
                    }

                    true
                }

                else -> false
            }
        }
    }

    private fun View.getGlowDrawable(glowColor: Int) = FancyDrawable(
        this,
        isGlow = true,
        glowSize = dimen(R.dimen.glow_size).toFloat(),
        glowColor = glowColor,
        gradientStartColor = null,
        gradienEndColor = null
    ).apply { alpha = 0 }

    private fun View.getFillDrawable(gradStartColor: Int, gradEndColor: Int) = FancyDrawable(
        this,
        isGlow = false,
        glowSize = dimen(R.dimen.glow_size).toFloat(),
        glowColor = null,
        gradientStartColor = gradStartColor,
        gradienEndColor = gradEndColor
    ).apply { alpha = DEFAULT_ALPHA }

    fun setTextResource(@StringRes textRes: Int) {
        textView.textResource = textRes
    }


    private class FancyDrawable(
        parent: View,
        private val isGlow: Boolean,
        private val glowSize: Float,
        private val glowColor: Int?,
        private val gradientStartColor: Int?,
        private val gradienEndColor: Int?
    ) : Drawable() {

        private val path = Path()

        private val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            if (isGlow) {
                setShadowLayer(glowSize, 0f, 0f, glowColor!!)
            }
            parent.setLayerType(LAYER_TYPE_SOFTWARE, this)
        }


        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
            super.setBounds(left, top, right, bottom)

            val paintLeft: Float = left.toFloat() + glowSize
            val paintTop: Float = top.toFloat() + glowSize
            val paintRight: Float = right.toFloat() - glowSize
            val paintBottom: Float = bottom.toFloat() - glowSize

            if (!isGlow) {
                paint.shader = LinearGradient(
                    paintLeft,
                    (paintBottom - paintTop) * 0.5f,
                    paintRight,
                    (paintBottom - paintTop) * 0.5f,
                    intArrayOf(gradientStartColor!!, gradienEndColor!!),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }

            val height = paintBottom - paintTop

            path.reset()
            path.moveTo(paintLeft + height / 2, paintTop)
            path.arcTo(paintLeft, paintTop, paintLeft + height, paintBottom, -90f, -180f, false)
            path.lineTo(paintRight - height / 2, paintBottom)
            path.arcTo(paintRight - height, paintTop, paintRight, paintBottom, 90f, -180f, false)
            path.lineTo(paintLeft + height / 2, paintTop)
        }

        override fun draw(canvas: Canvas) {
            canvas.drawPath(path, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
            invalidateSelf()
        }

        override fun getAlpha(): Int {
            return paint.alpha
        }

        override fun getOpacity() = PixelFormat.TRANSPARENT

        override fun setColorFilter(colorFilter: ColorFilter?) {
        }

    }

}