package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.superh2.library.myInterface.ParamsNumSetListener
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R
import android.view.Gravity
import android.widget.TextView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment


/**
 * created by  Noddy 2017/07/16
 * 参数数字设置弹出框
 */
class DialogFragment_Params_Setting_Number : DialogFragment(), View.OnClickListener
{
    private var layout_hundred: RelativeLayout? = null
    private var layout_ten: RelativeLayout? = null
    private var layout_single: RelativeLayout? = null
    private var btn_hundred_add: Button? = null
    private var btn_hundred_subtract: Button? = null
    private var btn_ten_add: Button? = null
    private var btn_ten_subtract: Button? = null
    private var btn_single_add: Button? = null
    private var btn_single_subtract: Button? = null
    private var tv_hundred: TextView? = null
    private var tv_ten: TextView? = null
    private var tv_single: TextView? = null

    // 范围
    private var layout_range: LinearLayout? = null
    private var tv_range_from: TextView? = null
    private var tv_range_to: TextView? = null

    private var mTitle: String? = null // 标题
    private var mDigitNum: Int = 0 // 多少位数
    private var mParamsNumSetListener: ParamsNumSetListener? = null
    private var mOldNum: Int = 0 // 原数值
    private var mMinNum: Int = 0 // 最小值
    private var mMaxNum: Int = 0 // 最大值

    // 个位数允许0~10
    private var mIsSingleTenMode: Boolean = false
    private var mSingleTenModeMinValue: Int = 0

    companion object
    {
        fun newInstance() = DialogFragment_Params_Setting_Number()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_params_setting_number, null)

        layout_hundred = v.findViewById<View>(R.id.layout_hundred) as RelativeLayout
        layout_ten = v.findViewById<View>(R.id.layout_ten) as RelativeLayout
        layout_single = v.findViewById<View>(R.id.layout_single) as RelativeLayout

        // 范围
        layout_range = v.findViewById<View>(R.id.layout_range) as LinearLayout
        tv_range_from = v.findViewById<View>(R.id.tv_range_from) as TextView
        tv_range_to = v.findViewById<View>(R.id.tv_range_to) as TextView

        btn_hundred_add = v.findViewById<View>(R.id.btn_hundred_add) as Button
        btn_hundred_subtract = v.findViewById<View>(R.id.btn_hundred_subtract) as Button
        btn_ten_add = v.findViewById<View>(R.id.btn_ten_add) as Button
        btn_ten_subtract = v.findViewById<View>(R.id.btn_ten_subtract) as Button
        btn_single_add = v.findViewById<View>(R.id.btn_single_add) as Button
        btn_single_subtract = v.findViewById<View>(R.id.btn_single_subtract) as Button

        tv_hundred = v.findViewById<View>(R.id.textview_hundred) as TextView
        tv_ten = v.findViewById<View>(R.id.textview_ten) as TextView
        tv_single = v.findViewById<View>(R.id.textview_single) as TextView


        tv_hundred!!.text = (mOldNum / 100 % 10).toString() + ""
        tv_ten!!.text = (mOldNum / 10 % 10).toString() + ""
        tv_single!!.text = (mOldNum % 10).toString() + ""

        if (mIsSingleTenMode) tv_single!!.text = mOldNum.toString() + ""


        // 根据参数设置界面
        if (this.mDigitNum == 1)
        {
            layout_hundred!!.visibility = View.GONE
            layout_ten!!.visibility = View.GONE
        }
        else if (this.mDigitNum == 2)
            layout_hundred!!.visibility = View.GONE

        // 按键监听事件
        btn_hundred_add!!.setOnClickListener(this)
        btn_hundred_subtract!!.setOnClickListener(this)
        btn_ten_add!!.setOnClickListener(this)
        btn_ten_subtract!!.setOnClickListener(this)
        btn_single_add!!.setOnClickListener(this)
        btn_single_subtract!!.setOnClickListener(this)

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
        parent.addView(myIcon)
        parent.addView(myTitle)

        builder.setView(v).setCustomTitle(parent).setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), null)
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> dismiss() }
        val alertDialog = builder.create()

        // 暂时FragmentDialog失去焦点，弹出FragmentDialog后，永久隐藏navigation bar. edited by liuja 2017-9-26
        alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        // 点击弹出框外部不消失
        requireDialog().setCanceledOnTouchOutside(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume()
    {
        super.onResume()
        // 禁止底部控件点击事件
        val view = requireActivity().window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, false)

        // 确定按钮（如未超出范围，自动dimiss，否则提示范围）
        val d = dialog as AlertDialog
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hundred = Integer.parseInt(tv_hundred!!.text.toString())
            val ten = Integer.parseInt(tv_ten!!.text.toString())
            val single = Integer.parseInt(tv_single!!.text.toString())
            var outValue = hundred * 100 + ten * 10 + single

            if (mIsSingleTenMode) outValue = single

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
                mParamsNumSetListener?.valueSetCompleted(outValue)
            }
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        // 恢复底部控件点击事件
        val view = requireActivity().window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, true)
    }

    /**
     * 设置参数
     *
     * @param title                标题
     * @param digitNum             数字位数：个位1；十位2；百位3
     * @param oldNum  原数值
     * @param minNum 最小值
     * @param maxNum 最大值
     * @param paramsNumSetListener 设置值监听接口
     */
    fun setDialogParams(title: String, digitNum: Int, oldNum: Int, minNum: Int, maxNum: Int, paramsNumSetListener: ParamsNumSetListener): DialogFragment_Params_Setting_Number
    {
        this.mTitle = title
        this.mDigitNum = digitNum
        this.mOldNum = oldNum
        this.mMinNum = minNum
        this.mMaxNum = maxNum
        this.mParamsNumSetListener = paramsNumSetListener

        return this
    }

    /**
     * 设置参数
     * @param title 标题
     * @param digitNum 数字位数：个位1；十位2；百位3
     * @param oldNum 原数值
     * @param isSingleTenMode 个位数设置允许0~10
     * @param singleTenModeMinValue 个位数最小值
     * @param paramsNumSetListener 设置值监听接口
     */
    fun setDialogParams(title: String, digitNum: Int, oldNum: Int, isSingleTenMode: Boolean, singleTenModeMinValue: Int, paramsNumSetListener: ParamsNumSetListener): DialogFragment_Params_Setting_Number
    {
        this.mTitle = title
        this.mDigitNum = digitNum
        this.mOldNum = oldNum
        this.mIsSingleTenMode = isSingleTenMode
        this.mSingleTenModeMinValue = singleTenModeMinValue
        this.mParamsNumSetListener = paramsNumSetListener

        return this
    }

    override fun onClick(v: View)
    {
        var hundred = Integer.parseInt(tv_hundred!!.text.toString())
        var ten = Integer.parseInt(tv_ten!!.text.toString())
        var single = Integer.parseInt(tv_single!!.text.toString())

        if (v === btn_hundred_add)
        {
            if (hundred < 9)
                tv_hundred!!.text = (++hundred).toString()
            else
                return
        }
        else if (v === btn_hundred_subtract)
        {
            if (hundred > 0)
            {
                tv_hundred!!.text = (--hundred).toString()
                if (hundred == 0 && ten == 0 && single == 0)
                {
                    ten = 9
                    tv_ten!!.text = ten.toString()
                    single = 9
                    tv_single!!.text = single.toString()
                }
            }
            else
                return
        }
        else if (v === btn_ten_add)
        {
            if (ten < 9)
                tv_ten!!.text = (++ten).toString()
            else
            {
                tv_ten!!.text = 0.toString()
                if (ten < 9)
                    tv_hundred!!.text = (++hundred).toString()
            }
        }
        else if (v === btn_ten_subtract)
        {
            if (ten > 0)
            {
                tv_ten!!.text = (--ten).toString()
                if (ten == 0 && mDigitNum == 2 && single == 0)
                {
                    single = 9
                    tv_single!!.text = single.toString()
                }
            }
            else
            {
                return
            }
        }
        else if (v === btn_single_add)
        {
            if (single == 9 && mIsSingleTenMode)
            {
                tv_single!!.text = (++single).toString()
            }
            else if (single == 9)
            {
                tv_single!!.text = 0.toString()
                if (ten < 9)
                    tv_ten!!.text = (++ten).toString()
            }
            else if (single < 9)
            {
                tv_single!!.text = (++single).toString()
            }
            else
                return
        }
        else if (v === btn_single_subtract)
        {
            if (single == mSingleTenModeMinValue && mIsSingleTenMode)
            {
                return
            }
            else if (single > 0)
                tv_single!!.text = (--single).toString()
            else
                return
        }
    }
}
