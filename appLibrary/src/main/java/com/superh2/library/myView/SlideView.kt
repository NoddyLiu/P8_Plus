package com.superh2.library.myView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.superh2.library.R
import com.superh2.library.myEnum.ESlideStatus

/**
 * 玻片
 * @author Noddy
 */
class SlideView : View
{
    // 颜色
    private val COLOR_DEFAULT: Int = Color.WHITE // 默认颜色
    private val COLOR_SCANNED: Int = resources.getColor(R.color.lightGreen) // 扫码完成颜色
    private val COLOR_WRONG: Int = resources.getColor(R.color.red) // 扫码错误颜色
    private val COLOR_FINISHED: Int = resources.getColor(R.color.green) // 已完成滴片颜色

    // 长度
    private val SLIDE_WIDTH: Float = 29f
    private val SLIDE_LENGTH: Float = 92f

    // Padding
    public var padding: Float = 0f

    // 文字
    private var _text: String = ""

    // 玻片状态
    private var _slideStatus = ESlideStatus.Default

    // 扫码内容
    private var _scanContentVisible = false
    private var _scanContent: String = ""

    private var paintBorder = Paint()
    private var paintInner = Paint()

    var text: String
        get() = _text
        set(value)
        {
            _text = value
            this.invalidate()
        }

    var slideStatus: ESlideStatus
        get() = _slideStatus
        set(value)
        {
            _slideStatus = value
            this.invalidate()
        }

    var scanContentVisible: Boolean
        get() = _scanContentVisible
        set(value)
        {
            _scanContentVisible = value
//            this.invalidate()
        }
    var scanContent: String
        get() = _scanContent
        set(value)
        {
            _scanContent = value
            this.invalidate()
        }

    constructor(context: Context) : super(context)
    {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int)
    {
        //        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleView, defStyle, 0)
        //        _fillColor = a.getColor(R.styleable.CircleView_fillColor, fillColor)
        //        _radius = a.getDimension(R.styleable.CircleView_radius, radius)
        //        a.recycle()
    }

    /**
     * 必须包含onMeasure，用于计算图像大小，如果没有，多个图像就显示不正确
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = SLIDE_WIDTH.toInt() * 1.3 + padding
        var height = SLIDE_LENGTH.toInt() * 0.9

        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)

        var paddingX = padding
        val barcodeX = SLIDE_WIDTH
        val barcodeY = (SLIDE_LENGTH * 0.3).toFloat()
        val slideX = SLIDE_WIDTH
        val slideY = (SLIDE_LENGTH * 0.7).toFloat()

        // 二维码矩形
        paintInner.style = Paint.Style.FILL
        paintInner.isAntiAlias = true
        paintInner.color = COLOR_DEFAULT
        canvas.drawRect(paddingX, 0f, barcodeX + paddingX, barcodeY, paintInner)
        if (slideStatus != ESlideStatus.Default) // 设置图案
        {
            // 线条
            paintInner.color = Color.BLACK
            paintInner.strokeWidth = 2f
            val yStep = 3f
            var totalY = 0f
            while (totalY < barcodeY)
            {
                canvas.drawLine(paddingX, totalY, barcodeX+ paddingX, totalY, paintInner)
                totalY += yStep
            }
        }
        // 二维码外边框
        paintBorder.color = resources.getColor(R.color.fbutton_default_color)
        paintBorder.style = Paint.Style.STROKE
        paintBorder.strokeWidth = 2f
        paintBorder.isAntiAlias = true
        canvas.drawRect(paddingX, 0f, barcodeX+ paddingX , barcodeY, paintBorder)

        // 玻片矩形
        paintInner.style = Paint.Style.FILL
        paintInner.isAntiAlias = true
        // 显示颜色
        paintInner.color = when (slideStatus)
        {
            ESlideStatus.Scanned -> COLOR_SCANNED
            ESlideStatus.Wrong -> COLOR_WRONG
            ESlideStatus.Finished -> COLOR_FINISHED
            else -> COLOR_DEFAULT
        }
        canvas.drawRect(paddingX, barcodeY - 2, slideX + paddingX, slideY, paintInner)
        // 玻片外边框
        paintBorder.color = resources.getColor(R.color.fbutton_default_color)
        paintBorder.style = Paint.Style.STROKE
        paintBorder.strokeWidth = 2f
        paintBorder.isAntiAlias = true
        canvas.drawRect(paddingX, barcodeY - 2, slideX + paddingX, slideY, paintBorder)

        // 文字
        var mTextPaint = Paint()
        mTextPaint.isAntiAlias = true
        mTextPaint.color = Color.BLACK
        mTextPaint.textSize = 18f
        mTextPaint.textAlign = Paint.Align.CENTER
        val bounds = Rect()
        mTextPaint.getTextBounds(text, 0, text.length, bounds)
        val height = bounds.height()
        canvas.drawText(text, slideX / 2 + paddingX , barcodeY + slideY / 2 - height / 3, mTextPaint)

        // 扫码内容
        if(scanContentVisible)
        {
            mTextPaint = Paint()
            mTextPaint.isAntiAlias = true
            mTextPaint.color = Color.BLACK
            mTextPaint.textSize = 12f
            mTextPaint.textAlign = Paint.Align.CENTER
            val bounds = Rect()
            mTextPaint.getTextBounds(scanContent, 0, scanContent.length, bounds)
            val height = bounds.height()
            val width = bounds.width()

            // 确保文字不会被遮挡
            val adjustedX = Math.max(slideX / 2, width / 2f)
//            canvas.drawText(scanContent, adjustedX, slideY + height + 3, mTextPaint)
            canvas.drawText(scanContent, slideX / 2 + paddingX, slideY + height + 3, mTextPaint)
        }
    }
}
