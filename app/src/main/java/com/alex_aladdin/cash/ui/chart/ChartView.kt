package com.alex_aladdin.cash.ui.chart

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log.e
import android.view.SurfaceHolder
import android.view.SurfaceView

class ChartView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {

        private const val TAG = "CashChartView"

    }


    private var drawThread: DrawThread? = null


    init {
        holder.addCallback(this)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        holder.setFormat(PixelFormat.TRANSLUCENT)

        drawThread = DrawThread(holder, context).apply {
            runFlag.set(true)
            drawQueue.add(ChartData())
            start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawThread?.apply {
            var retry = true
            runFlag.set(false)

            while (retry) {
                try {
                    join()
                    retry = false
                } catch (exception: InterruptedException) {
                    e(TAG, "Error when trying to stop draw thread", exception)
                }
            }
        }
    }

}