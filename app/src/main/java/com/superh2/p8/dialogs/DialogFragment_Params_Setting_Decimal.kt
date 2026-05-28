package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.superh2.library.myInterface.ParamsDecimalSetListener
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R

/**
 * created by  Noddy 2018/05/30
 * 参数小数设置弹出框
 */
class DialogFragment_Params_Setting_Decimal : DialogFragment(), View.OnClickListener
{
    private var layout_hundred: RelativeLayout? = null
    private var layout_ten: RelativeLayout? = null

    private var btn_hundred_add: Button? = null
    private var btn_hundred_subtract: Button? = null
    private var tv_hundred: TextView? = null
    private var btn_ten_add: Button? = null
    private var btn_ten_subtract: Button? = null
    private var tv_ten: TextView? = null
    private var btn_single_add: Button? = null
    private var btn_single_subtract: Button? = null
    private var tv_single: TextView? = null
    private var btn_decimal_add: Button? = null
    private var btn_decimal_subtract: Button? = null
    private var tv_decimal: TextView? = null

    // 范围
    private var layout_range: LinearLayout? = null
    private var tv_range_from: TextView? = null
    private var tv_range_to: TextView? = null

    private var mTitle: String? = null // 标题
    private var mSubTitle: String? = null // 副标题
    private var mDigitNum: Int = 3
    private var mParamsDecimalSetListener: ParamsDecimalSetListener? = null
    private var mOldNum: Float = 0f // 原数值
    private var mMinNum: Float = 0f // 最小值
    private var mMaxNum: Float = 0f // 最大值

    companion object
    {
        fun newInstance() = DialogFragment_Params_Setting_Decimal()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_params_setting_decimal, null)

        // 范围
        layout_range = v.findViewById<View>(R.id.layout_range) as LinearLayout
        tv_range_from = v.findViewById<View>(R.id.tv_range_from) as TextView
        tv_range_to = v.findViewById<View>(R.id.tv_range_to) as TextView


        layout_hundred = v.findViewById<View>(R.id.layout_hundred) as RelativeLayout
        layout_ten = v.findViewById<View>(R.id.layout_ten) as RelativeLayout
        btn_hundred_add = v.findViewById<View>(R.id.btn_hundred_add) as Button
        btn_hundred_subtract = v.findViewById<View>(R.id.btn_hundred_subtract) as Button
        btn_ten_add = v.findViewById<View>(R.id.btn_ten_add) as Button
        btn_ten_subtract = v.findViewById<View>(R.id.btn_ten_subtract) as Button
        btn_single_add = v.findViewById<View>(R.id.btn_single_add) as Button
        btn_single_subtract = v.findViewById<View>(R.id.btn_single_subtract) as Button
        btn_decimal_add = v.findViewById<View>(R.id.btn_decimal_add) as Button
        btn_decimal_subtract = v.findViewById<View>(R.id.btn_decimal_subtract) as Button

        tv_hundred = v.findViewById<View>(R.id.textview_hundred) as TextView
        tv_ten = v.findViewById<View>(R.id.textview_ten) as TextView
        tv_single = v.findViewById<View>(R.id.textview_single) as TextView
        tv_decimal = v.findViewById<View>(R.id.textview_decimal) as TextView

        tv_hundred!!.text = ((mOldNum * 10).toInt() / 1000).toString()
        tv_ten!!.text = ((mOldNum * 10).toInt() % 1000 / 100).toString()
        tv_single!!.text = ((mOldNum * 10).toInt() % 1000 % 100 / 10).toString()
        tv_decimal!!.text = ((mOldNum * 10).toInt() % 10).toString()

        // 根据参数设置界面
        if (this.mDigitNum == 1)
        {
            layout_hundred!!.visibility = View.GONE
            layout_ten!!.visibility = View.GONE
        }
        else if (this.mDigitNum == 2) layout_hundred!!.visibility = View.GONE

        // 按键监听事件
        btn_hundred_add!!.setOnClickListener(this)
        btn_hundred_subtract!!.setOnClickListener(this)
        btn_ten_add!!.setOnClickListener(this)
        btn_ten_subtract!!.setOnClickListener(this)
        btn_single_add!!.setOnClickListener(this)
        btn_single_subtract!!.setOnClickListener(this)
        btn_decimal_add!!.setOnClickListener(this)
        btn_decimal_subtract!!.setOnClickListener(this)

        // 自定义标题
        val parent = LinearLayout(context)
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parent.orientation = LinearLayout.HORIZONTAL
        // 图标
        val myIcon = ImageView(context)
        myIcon.setBackgroundResource(android.R.drawable.ic_dialog_info)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER
        layoutParams.leftMargin = 15
        myIcon.layoutParams = layoutParams
        // 标题
        val myTitle = TextView(context)
        myTitle.text = this.mTitle
        myTitle.gravity = Gravity.LEFT
        myTitle.setPadding(15,15,15,15)
        myTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40.toFloat())
        myTitle.setTextColor(Color.BLACK)
        // 副标题
        val mySubTitle = TextView(context)
        if (this.mSubTitle != null)
        {
            mySubTitle.text = this.mSubTitle
            mySubTitle.gravity = Gravity.LEFT
            mySubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25.toFloat())
            mySubTitle.setTextColor(Color.BLACK)
        }

        parent.addView(myIcon)
        parent.addView(myTitle)
        if (this.mSubTitle != null) parent.addView(mySubTitle)

        builder.setView(v).setCustomTitle(parent).setCancelable(false).setPositiveButton(getString(R.string.confirm), null).setNegativeButton(getString(R.string.cancel)) { _, _ -> dismiss() }
        val alertDialog = builder.create()

        // 暂时FragmentDialog失去焦点，弹出FragmentDialog后，永久隐藏navigation bar. edited by liuja 2017-9-26
        alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        // 点击弹出框外部不消失
        dialog.setCanceledOnTouchOutside(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume()
    {
        super.onResume()
        // 禁止底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, false)

        // 确定按钮（如未超出范围，自动dimiss，否则提示范围）
        val d = dialog as AlertDialog
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hundred = Integer.parseInt(tv_hundred!!.text.toString())
            val ten = Integer.parseInt(tv_ten!!.text.toString())
            val single = Integer.parseInt(tv_single!!.text.toString())
            val decimal = Integer.parseInt(tv_decimal!!.text.toString())
            var outValue = (hundred * 100 + ten * 10 + single + decimal * 0.1).toFloat()

            // 判断是否超出范围
            if (outValue > this.mMaxNum || outValue < this.mMinNum)
            {
                tv_range_from!!.text = mMinNum.toString()
                tv_range_to!!.text = mMaxNum.toString()
                layout_range!!.visibility = View.VISIBLE
            }
            else
            {
                dismiss()
                mParamsDecimalSetListener?.valueSetCompleted(outValue)
            }
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        // 恢复底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, true)
    }

    /**
     * 设置参数
     *
     * @param title                标题
     * @param subTitle 副标题
     * @param digitNum 位数
     * @param oldNum  原数值
     * @param minNum 最小值
     * @param maxNum 最大值
     * @param paramsDecimalSetListener 设置值监听接口
     */
    fun setDialogParams(title: String, subTitle: String?, digitNum: Int, oldNum: Float, minNum: Float, maxNum: Float, paramsDecimalSetListener: ParamsDecimalSetListener): DialogFragment_Params_Setting_Decimal
    {
        this.mTitle = title
        this.mSubTitle = subTitle
        this.mDigitNum = digitNum
        this.mOldNum = oldNum
        this.mMinNum = minNum
        this.mMaxNum = maxNum
        this.mParamsDecimalSetListener = paramsDecimalSetListener

        return this
    }

    override fun onClick(v: View)
    {
        var hundred = Integer.parseInt(tv_hundred!!.text.toString())
        var ten = Integer.parseInt(tv_ten!!.text.toString())
        var single = Integer.parseInt(tv_single!!.text.toString())
        var decimal = Integer.parseInt(tv_decimal!!.text.toString())

        if (v === btn_hundred_add)
        {
            if (hundred < 9) tv_hundred!!.text = (++hundred).toString()
            else return
        }
        else if (v === btn_hundred_subtract)
        {
            if (hundred > 0) tv_hundred!!.text = (--hundred).toString()
            else if (hundred == 0 && ten == 0) tv_ten!!.text = 9.toString()
            else return
        }
        else if (v === btn_ten_add)
        {
            if (ten < 9) tv_ten!!.text = (++ten).toString()
            else return
        }
        else if (v === btn_ten_subtract)
        {
            if (ten > 0) tv_ten!!.text = (--ten).toString()
            else if (ten == 0 && single == 0) tv_single!!.text = 9.toString()
            else return
        }
        else if (v === btn_single_add)
        {
            if (single < 9) tv_single!!.text = (++single).toString()
            else return
        }
        else if (v === btn_single_subtract)
        {
            if (single > 0) tv_single!!.text = (--single).toString()
            else return
        }
        else if (v === btn_decimal_add)
        {
            when
            {
                decimal < 9 -> tv_decimal!!.text = (++decimal).toString()
                decimal == 9 ->
                {
                    tv_decimal!!.text = 0.toString()
                    if (single < 9) tv_single!!.text = (++single).toString()
                }
                else -> return
            }
        }
        else if (v === btn_decimal_subtract)
        {
            if (decimal > 0) tv_decimal!!.text = (--decimal).toString() + ""
            else return
        }
    }
}
