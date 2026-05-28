package com.superh2.library.myView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.superh2.library.R

/**
 * 圆孔
 * @author liu_ja
 */
class CircleView : View
{
    // 颜色
    private val COLOR_FILL: Int = Color.GREEN // 填充颜色
    private val COLOR_DEFAULT: Int = Color.WHITE // 默认颜色
    private val COLOR_HINT: Int = Color.RED // 提示颜色
    // 半径
    private var _radius: Float = 34f
    // 文字
    private var _text: String = ""
    // 文字大小
    private var _textSize = 25f
    // 是否填充
    private var _isFill = false
    // 是否闪烁提示
    private var _isBlink = false

    // 闪烁模式：0到255 或者 255到0
    private var _isAlpha0To255 = false
    // 当前alpha值
    private var _currentAlpha = 255

    private var paintBorder = Paint()
    private var paintInner = Paint()

    var radius: Float
        get() = _radius
        set(value)
        {
            _radius = value
            this.invalidate()
        }

    var text: String
        get() = _text
        set(value)
        {
            _text = value
            this.invalidate()
        }

    var textSize: Float
        get() = _textSize
        set(value)
        {
            _textSize = value
            this.invalidate()
        }

    var isFill: Boolean
        get() = _isFill
        set(value)
        {
            _isFill = value
            this.invalidate()
        }

    var isBlink: Boolean
        get() = _isBlink
        set(value)
        {
            _isBlink = value
            this.invalidate()
        }

    var currentAlpha: Int
        get() = _currentAlpha
        set(value)
        {
            _currentAlpha = value
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
        var width = _radius.toInt() * 2
        var height = _radius.toInt() * 2

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)

        val x = _radius
        val y = _radius

        // 内圆
        paintInner.style = Paint.Style.FILL
        paintInner.isAntiAlias = true
        if (isFill) // 已放置试管
        {
            paintInner.alpha = 255
            paintInner.color = COLOR_FILL
        }
        else
        {
            if (isBlink) // 提示放置试管
                blink()
            else
            {
                paintInner.alpha = 255
                paintInner.color = COLOR_DEFAULT
            }
        }
        canvas.drawCircle(x, y, _radius - 4, paintInner)
        // 外边框
//        paintBorder.color = Color.BLUE
        paintBorder.color = resources.getColor(R.color.fbutton_default_color)
        paintBorder.style = Paint.Style.STROKE
        paintBorder.strokeWidth = 3f
        paintBorder.isAntiAlias = true
        canvas.drawCircle(x, y, _radius - 4, paintBorder)

        // 文字
        var mTextPaint = Paint()
        mTextPaint.isAntiAlias = true
        mTextPaint.color = Color.BLACK
        mTextPaint.textSize = _textSize
        mTextPaint.textAlign = Paint.Align.CENTER
        val bounds = Rect()
        mTextPaint.getTextBounds(text, 0, text.length, bounds)
        val height = bounds.height()
        canvas.drawText(text, x, y + height / 2, mTextPaint)
    }

    /**
     * 闪烁效果
     */
    private fun blink()
    {
        if (_isAlpha0To255) // alpha从0到255
        {
            if (currentAlpha >= 255)
                _isAlpha0To255 = false
            else
                currentAlpha += 5
        }
        else // alpha从255到0
        {
            if (currentAlpha <= 0)
                _isAlpha0To255 = true
            else
                currentAlpha -= 5
        }

        paintInner.color = COLOR_HINT
        paintInner.alpha = currentAlpha
        this.invalidate()
    }
}
