package com.madewithlove.daybalance.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.core.content.ContextCompat
import com.madewithlove.daybalance.R


class DashedLineView(context: Context) : View(context) {

    private val paint = Paint().apply {
        isAntiAlias = false
        color = ContextCompat.getColor(context, R.color.smoke)
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(4f, 16f), 0f)
    }

    private val path = Path()


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        paint.strokeWidth = measuredHeight.toFloat()
        path.moveTo(0f, measuredHeight / 2f)
        path.lineTo(measuredWidth.toFloat(), measuredHeight / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun getBaseline(): Int {
        return height
    }

}