package com.singularity.ipcaplus.drawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.singularity.ipcaplus.R
import android.graphics.RectF
import android.view.MotionEvent

class DateTimePicker : View {

    private var touchY = 0f
    private var _value = 0

    var value: Int
        get() = _value
        set(newValue) {
            _value = newValue

            touchY = height - (newValue / 100F * height)

            onValueChanged?.invoke(_value)
            invalidate()
        }

    private var onValueChanged: ((Int) -> Unit)? = null

    fun setOnValueChanged(callback: (Int) -> Unit) {
        onValueChanged = callback
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context,
        attrs,
        defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.DateTimePicker, defStyle, 0)

        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paintLabel = Paint()
        paintLabel.color = Color.BLACK
        paintLabel.textSize = 40.0f
        val paintNumber = Paint()
        paintNumber.color = Color.GRAY
        paintNumber.textSize = 40.0f
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.isAntiAlias = true


        canvas.drawText("Dia", 60f, 80f, paintLabel)

        canvas.drawRoundRect(RectF(30f, 220f, 140f, 310f), 2f, 2f, paint)

        canvas.drawText("14", 60f, 180f, paintNumber)
        canvas.drawText("15", 60f, touchY.toInt() + 280f, paintNumber)
        canvas.drawText("16", 60f, 380f, paintNumber)

        //canvas.drawText("Mês", 130f, 80f, paintText)
        //canvas.drawText("Hora", 40f, 80f, paintText)
        //canvas.drawText("Min.", 60f, 80f, paintText)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val y = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            -> {
                touchY = y ?: 0f
                _value = 100 - ((touchY / height.toFloat()) * 100F).toInt()

                onValueChanged?.invoke(_value)
                invalidate()
                return true
            }
        }
        return false
    }
}