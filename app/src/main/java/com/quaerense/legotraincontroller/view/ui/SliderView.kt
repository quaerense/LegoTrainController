package com.quaerense.legotraincontroller.view.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.quaerense.legotraincontroller.R
import kotlin.math.abs

class SliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()

    private var halfWidth = 0f

    private var minValue = 0
    private var maxValue = 100

    private var currentSliderHeight = 0f
    private var maxSliderHeight = 0f

    private var alphaAnimator: ValueAnimator? = null
    private var currentAlpha = 0

    private var angleAnimator: ValueAnimator? = null
    private var currentAngle = 0f

    private var isFirstLaunch = true

    var onChangeListener: ((Int) -> Unit)? = null

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.SliderView)) {
            minValue = getInteger(R.styleable.SliderView_android_min, 0)
            maxValue = getInteger(R.styleable.SliderView_android_max, 100)
            recycle()
        }
        paint.style = Paint.Style.FILL
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        halfWidth = width / 2f
        maxSliderHeight = height.toFloat() - width.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isFirstLaunch) {
            setProgress(maxValue / 2)
            isFirstLaunch = false
        }
        paint.color = Color.WHITE
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 64f, 64f, paint)
        paint.color = ContextCompat.getColor(context, R.color.light_blue)
        canvas.drawRoundRect(
            0f,
            currentSliderHeight,
            width.toFloat(),
            height.toFloat(),
            64f,
            64f,
            paint
        )
        drawArrow(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                alphaArrow(255)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                currentSliderHeight = event.y.coerceIn(0f, maxSliderHeight)
                val currentValue =
                    (maxValue * (1 - (currentSliderHeight / maxSliderHeight))).toInt()
                onChangeListener?.invoke(currentValue)
                if (angleAnimator == null || angleAnimator?.isRunning == false) {
                    rotateArrow(if (currentValue >= maxValue / 2) 0f else 180f)
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                alphaArrow(0)
                invalidate()
            }
        }

        return true
    }

    private fun drawArrow(canvas: Canvas) {
        val bitmap = (ContextCompat.getDrawable(
            context,
            R.drawable.arrow
        ) as VectorDrawable).toBitmap()

        paint.alpha = currentAlpha
        canvas.save()
        val scale =
            0.5f + (abs(currentSliderHeight - maxSliderHeight / 2f) * 2f / maxSliderHeight) / 2f
        val py = currentSliderHeight + halfWidth
        canvas.scale(scale, scale, halfWidth, py)
        canvas.rotate(currentAngle, halfWidth, py)
        canvas.drawBitmap(bitmap, halfWidth - bitmap.width / 2f, py - bitmap.height / 2f, paint)
        canvas.restore()
    }

    private fun alphaArrow(targetAlpha: Int) {
        alphaAnimator = ValueAnimator.ofInt(currentAlpha, targetAlpha).apply {
            addUpdateListener { animation ->
                currentAlpha = animation.animatedValue as Int
                invalidate()
            }
            duration = 100
            start()
        }
    }

    private fun rotateArrow(targetAngle: Float) {
        angleAnimator = ValueAnimator.ofFloat(currentAngle, targetAngle).apply {
            addUpdateListener { animation ->
                currentAngle = animation.animatedValue as Float
                invalidate()
            }
            duration = 100
            start()
        }
    }

    fun setProgress(value: Int) {
        onChangeListener?.invoke(value)
        currentSliderHeight = maxSliderHeight * (value.toFloat() / maxValue.toFloat())
        invalidate()
    }
}