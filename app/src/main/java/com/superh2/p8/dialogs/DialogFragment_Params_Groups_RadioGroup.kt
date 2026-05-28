package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.superh2.library.myInterface.ParamsGroupSetListener
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R


/**
 * @description 参数组RadioButton选择框
 * @author Noddy
 * @date 2018/11/04
 */
class DialogFragment_Params_Groups_RadioGroup : DialogFragment()
{
    private var mTitle: String? = null
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mParamsGroupSetListener: ParamsGroupSetListener? = null

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Params_Groups_RadioGroup()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_params_groups_radiogroup, null)

        // 添加20组radiobutton
        val rg_params_groups = v.findViewById<View>(R.id.rg_params_groups) as RadioGroup
        for (i in 0 until paramMethodParamsGroup.methodParamsGroup.size)
        {
            var radioButton = RadioButton(context)
            var layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(5, 5, 5, 5)
            radioButton.layoutParams = layoutParams
            radioButton.text = (i + 1).toString() + "." + paramMethodParamsGroup.methodParamsGroup[i].groupName
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.content_title_textSize))
            radioButton.setPadding(10, 10, 10, 10)
            rg_params_groups.addView(radioButton)

            // 该radiobutton是否已经选中
            if (i == paramMethodParamsGroup.selectedGroupIndex)
                rg_params_groups.check(radioButton.id)
        }

        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(v).setCancelable(false)
                .setPositiveButton(this.mPositiveBtnStr) { _, _ ->
                    val rbtnIndex = rg_params_groups.indexOfChild(rg_params_groups.findViewById(rg_params_groups.checkedRadioButtonId))
                    mParamsGroupSetListener!!.clickedOk(rbtnIndex)
                }.setNegativeButton(this.mNegativeBtnStr) { _, _ ->
                    dismissDialog()
                }

        val alertDialog = builder.create()

        // 暂时FragmentDialog失去焦点，弹出FragmentDialog后，永久隐藏navigation bar. edited by liuja 2017-10-07
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

        isShow = true

        // 禁止底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, false)
    }

    override fun onDestroy()
    {
        super.onDestroy()

        isShow = false

        // 恢复底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, true)
    }

    /**
     * 取消Dialog
     */
    fun dismissDialog()
    {
        isShow = false
        dismiss()

        // 恢复底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, true)
    }

    /**
     * 设置内容
     *
     * @param title 标题
     * @param positiveBtnStr 确定按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param paramsGroupSetListener 监听接口
     */
    fun setDialogContent(title: String?, positiveBtnStr: String, negativeBtnStr: String, paramsGroupSetListener: ParamsGroupSetListener): DialogFragment_Params_Groups_RadioGroup
    {
        this.mTitle = title
        this.mPositiveBtnStr = positiveBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mParamsGroupSetListener = paramsGroupSetListener

        return this
    }
}
