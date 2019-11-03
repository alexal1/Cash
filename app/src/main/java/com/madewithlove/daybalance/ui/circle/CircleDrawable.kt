/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.ui.circle

import android.content.Context
import android.graphics.*
import android.graphics.PixelFormat.TRANSLUCENT
import android.graphics.drawable.Drawable
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.utils.color
import org.jetbrains.anko.dip

class CircleDrawable(ctx: Context) : Drawable() {

    val thickness = ctx.dip(8)

    private val rect = RectF()

    private val backgroundPaint = Paint().apply {
        color = ctx.color(R.color.palladium)
        isAntiAlias = true
    }

    private val greenPaint = Paint().apply {
        color = ctx.color(R.color.green)
        isAntiAlias = true
    }

    private val redPaint = Paint().apply {
        color = ctx.color(R.color.red)
        isAntiAlias = true
    }

    private val centerPaint = Paint().apply {
        color = ctx.color(R.color.soft_dark)
        isAntiAlias = true
    }

    var circleBounds = Rect()

    var progress = 0f
        set(value) {
            field = value
            invalidateSelf()
        }


    override fun draw(canvas: Canvas) {
        // Draw background
        rect.set(circleBounds)
        canvas.drawOval(rect, backgroundPaint)

        // Draw progress
        val sweepAngle = 360f * progress
        val paint = if (progress > 0) greenPaint else redPaint
        canvas.drawArc(rect, 0f, sweepAngle, true, paint)

        // Draw center
        rect.left += thickness
        rect.top += thickness
        rect.right -= thickness
        rect.bottom -= thickness
        canvas.drawOval(rect, centerPaint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int {
        return TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

}