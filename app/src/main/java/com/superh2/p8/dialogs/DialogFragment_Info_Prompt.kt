package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.superh2.library.myInterface.InfoPromptListener
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R


/**
 * @description 信息提示弹出框（单按钮）
 * @author Noddy
 * @date 2018/06/28
 */
class DialogFragment_Info_Prompt : DialogFragment()
{
    private var mTitle: String? = null
    private var mContent: String? = null
    private var mBtnStr: String? = null // 确定按钮文本
    private var mInfoPromptListener: InfoPromptListener? = null
    // 弹出框距离中心偏移
    private var mMarginCenterX = 0
    private var mMarginCenterY = 0

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Info_Prompt()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_prompt, null)

        val tv_info = v.findViewById<View>(R.id.tv_info) as TextView
        tv_info.text = mContent

        if (mBtnStr == null) builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(v).setCancelable(false).setPositiveButton(null) { _, _ ->
            dismissDialog()
            mInfoPromptListener!!.clicked()
        }
        else builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(v).setCancelable(false).setPositiveButton(this.mBtnStr) { _, _ ->
            dismissDialog()
            mInfoPromptListener!!.clicked()
        }

        val alertDialog = builder.create()
        // 设置按钮大小
        alertDialog.setOnShowListener {
            val btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE)
            //            btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, dimen(R.dimen.dialog_btn_param_textSize).toFloat())
        }

        // 暂时FragmentDialog失去焦点，弹出FragmentDialog后，永久隐藏navigation bar. edited by liuja 2017-10-07
        alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        // 弹出框在屏幕位置
        val wmlp = alertDialog.window!!.attributes
        wmlp.gravity = Gravity.CENTER
        wmlp.x = this.mMarginCenterX   // x position
        wmlp.y = this.mMarginCenterY  // y position

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

        isShow = true

        // 禁止底部控件点击事件
        val view = requireActivity().window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, false)
    }

    override fun onDestroy()
    {
        super.onDestroy()

        isShow = false

        // 恢复底部控件点击事件
        val view = requireActivity().window.decorView as FrameLayout
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
        val view = requireActivity().window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, true)
    }

    /**
     * 设置警告提示
     *
     * @param title 标题
     * @param content 内容
     * @param marginCenterX x距离中心偏移
     * @param marginCenterY y距离中心偏移
     * @param infoPromptListener 信息提示监听接口
     */
    fun setDialogContent(title: String?, content: String, btnStr: String?, marginCenterX: Int, marginCenterY: Int, infoPromptListener: InfoPromptListener): DialogFragment_Info_Prompt
    {
        this.mTitle = title
        this.mContent = content
        this.mBtnStr = btnStr
        this.mMarginCenterX = marginCenterX
        this.mMarginCenterY = marginCenterY
        this.mInfoPromptListener = infoPromptListener

        return this
    }
}
