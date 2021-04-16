package com.rohit.encrypto.custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText


class LinedEditText(context: Context?, attrs: AttributeSet?) : AppCompatEditText(context!!, attrs) {
    private val mRect: Rect = Rect()
    private val mPaint: Paint = Paint()
    override fun onDraw(canvas: Canvas) {
        val height = height
        val lineHeight: Int = 200
        var count = height / lineHeight
        if (lineCount > count) count = lineCount
        val r: Rect = mRect
        val paint: Paint = mPaint
        var baseline: Int = getLineBounds(0, r)
        for (i in 0 until count) {
            canvas.drawLine(
                r.left.toFloat(), (baseline + 2).toFloat(),
                r.right.toFloat(), (baseline + 2).toFloat(), paint
            )
            baseline += lineHeight
        }
        super.onDraw(canvas)
    }

    // we need this constructor for LayoutInflater
    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = -0x7fffff01
    }
}