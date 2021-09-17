package com.sablab.speedometerview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.cos
import kotlin.math.sin

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @ColorInt
    private var miniCircleColor: Int = context.getColor(R.color.black)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var textColor: Int = context.getColor(R.color.red1)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var textCircleColor: Int = Color.rgb(127, 127, 127)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var speedColor: Int = context.getColor(R.color.initCircleColor)
        set(value) {
            field = value
            invalidate()
        }

    var maxSpeed: Float = defMaxSpeed
        set(value) {
            if (value >= 0 && value > speed) {
                field = value
                invalidate()
            }
        }
    var speed: Float = defCurrentSpeed
        set(value) {
            if (value in 0.0f..maxSpeed) {
                field = value
                invalidate()
            }
        }
    var speedStepSize: Float = defSpeedStep
        set(value) {
            if (value < maxSpeed) {
                field = value
                invalidate()
            }
        }

    private val circlePaint = Paint()
    private val miniCirclePaint = Paint()
    private val textPaint = TextPaint()

    private val tickPaint = Paint()
    private val needlePaint = Paint()
    private val maskPaint = Paint()
    private val backgroundPaint = Paint()
    private val backgroundInnerPaint = Paint()
    private val textCirclePaint = Paint()

    private var _mask: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.spot_mask)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpeedometerView)
        maxSpeed = typedArray.getFloat(R.styleable.SpeedometerView_maxSpeed, maxSpeed)
        speed = typedArray.getFloat(R.styleable.SpeedometerView_speed, speed)
        speedStepSize = typedArray.getFloat(R.styleable.SpeedometerView_speedStep, speedStepSize)
        speedColor = typedArray.getColor(R.styleable.SpeedometerView_stepColor, speedColor)
        textColor = typedArray.getColor(R.styleable.SpeedometerView_textColor, textColor)
        textCircleColor = typedArray.getColor(R.styleable.SpeedometerView_textCircleColor, textCircleColor)
        typedArray.recycle()

        _mask = Bitmap.createBitmap(_mask, 0, 0, _mask.width, _mask.height / 2)

        textPaint.color = textColor
        textPaint.textSize = CONST_TICK_LENGTH
        textPaint.textAlign = Paint.Align.CENTER

        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = speedColor
        circlePaint.strokeWidth = CONST_CENTER_MINI_CIRCLE_RADIUS

        miniCirclePaint.style = Paint.Style.FILL
        miniCirclePaint.color = miniCircleColor
        miniCirclePaint.strokeWidth = CONST_CENTER_MINI_CIRCLE_RADIUS

        tickPaint.style = Paint.Style.STROKE
        tickPaint.color = speedColor
        tickPaint.strokeWidth = CONST_CENTER_MINI_CIRCLE_RADIUS * 0.75f

        needlePaint.style = Paint.Style.FILL_AND_STROKE
        needlePaint.color = speedColor
        needlePaint.strokeWidth = 5f

        maskPaint.isDither = true

        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = Color.rgb(127, 127, 127)

        textCirclePaint.style = Paint.Style.FILL
        textCirclePaint.color = textCircleColor

        backgroundInnerPaint.style = Paint.Style.FILL
        backgroundInnerPaint.color = Color.rgb(150, 150, 150)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width: Int
        var height: Int

        //Measure Width
        width = if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            widthSize
        } else {
            -1
        }

        //Measure Height
        height = if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            heightSize
        } else {
            -1
        }
        if (height >= 0 && width >= 0) {
            width = height.coerceAtMost(width)
            height = width / 2
        } else if (width >= 0) {
            height = width / 2
        } else if (height >= 0) {
            width = height * 2
        } else {
            width = 0
            height = 0
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {

        drawCircle(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        drawBackground(canvas)
        drawNeedle(canvas)
        drawTicks(canvas)
    }

    private fun drawNeedle(canvas: Canvas) {
        val oval = getOval(canvas, 1f)
        val radius = oval.width() * 0.35f - 20
        val smallOval = getOval(canvas, 0.2f)

        val angle = speed * 180 / maxSpeed

        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(oval.centerX() - 30f, oval.centerY())
        path.lineTo(oval.centerX() + 30f, oval.centerY())
        path.lineTo(
            (oval.centerX() + cos((180 - angle) / 180 * Math.PI) * radius).toFloat(),
            (oval.centerY() - sin(angle / 180 * Math.PI) * radius).toFloat()
        )
        path.close()
        canvas.drawPath(path, needlePaint)

        canvas.drawArc(smallOval, 180f, 180f, true, textCirclePaint)
        val miniOval = getOval(canvas, 0.05f)
//        canvas.drawArc(miniOval, 180f, 180f, true, miniCirclePaint)

        textPaint.textSize = miniOval.height()
        canvas.drawText(speed.toInt().toString(), miniOval.centerX(), miniOval.centerY() - (miniOval.height() / 2 + 5f), textPaint)
    }

    private fun drawTicks(canvas: Canvas) {
        textPaint.textSize = CONST_TICK_LENGTH * 0.75f

        val availableAngle = 180
        val majorStep = (speedStepSize / maxSpeed * availableAngle)
        val oval = getOval(canvas, 1f)
        val textOval = getOval(canvas, 0.8f)
        val textOvalRadius = textOval.width() * 0.41f
        val radius = oval.width() * 0.41f
        var currentAngle = 0f

        for (i in 0 until (maxSpeed / speedStepSize + 1).toInt()) {
            canvas.drawLine(
                (oval.centerX() + cos((180 - currentAngle) / 180 * Math.PI) * (radius - CONST_TICK_LENGTH / 2)).toFloat(),
                (oval.centerY() - sin(currentAngle / 180 * Math.PI) * (radius - CONST_TICK_LENGTH / 2)).toFloat(),
                (oval.centerX() + cos((180 - currentAngle) / 180 * Math.PI) * (radius + CONST_TICK_LENGTH / 2)).toFloat(),
                (oval.centerY() - sin(currentAngle / 180 * Math.PI) * (radius + CONST_TICK_LENGTH / 2)).toFloat(),
                tickPaint
            )

            canvas.drawText(
                (i * speedStepSize).toInt().toString(),
                (textOval.centerX() + cos((180 - currentAngle) / 180 * Math.PI) * (textOvalRadius + CONST_TICK_LENGTH / 2)).toFloat(),
                (textOval.centerY() - sin(currentAngle / 180 * Math.PI) * (textOvalRadius + CONST_TICK_LENGTH / 2)).toFloat(),
                textPaint
            )
            currentAngle += majorStep
        }
    }

    private fun getOval(canvas: Canvas, factor: Float): RectF {
        val oval: RectF
        val canvasWidth = canvas.width - paddingLeft - paddingRight
        val canvasHeight = canvas.height - paddingTop - paddingBottom

        oval = if (canvasHeight * 2 >= canvasWidth) {
            RectF(0f, 0f, (canvasWidth * factor), (canvasWidth * factor))
        } else {
            RectF(0f, 0f, (canvasHeight * 2 * factor), (canvasHeight * 2 * factor))
        }
        oval.offset((canvasWidth - oval.width()) / 2, (canvasHeight * 2 - oval.height()) / 2)
        return oval
    }

    private fun drawBackground(canvas: Canvas) {
        try {
            val oval = getOval(canvas, 0.9f)
            canvas.drawArc(oval, 180f, 180f, true, backgroundPaint)

            val mask = Bitmap.createScaledBitmap(_mask, (oval.width() * 1.1).toInt(), (oval.height() * 1.1).toInt() / 2, true)
            canvas.drawBitmap(mask, oval.centerX() - oval.width() * 1.1f / 2, oval.centerY() - oval.width() * 1.1f / 2, maskPaint)

            val innerOval = getOval(canvas, 0.87f)
            canvas.drawArc(innerOval, 180f, 180f, false, circlePaint)
        } catch (e: Exception) {
            Log.e(TAG, "error=${e.message}")
        }
    }

    companion object {
        const val defMaxSpeed: Float = 180f
        const val defCurrentSpeed: Float = 5f
        const val defSpeedStep: Float = 20f

        const val CONST_CENTER_MINI_CIRCLE_RADIUS = 20f
        const val CONST_TICK_LENGTH = 40f

        const val TAG = "SpeedometerView"
    }
}