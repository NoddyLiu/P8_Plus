package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.superh2.library.myInterface.WarnPromptListener
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R

/**
 * @description 警告提示弹出框（两按钮）
 * @author Noddy
 * @date 2018/03/07
 */
class DialogFragment_Warn_Prompt : DialogFragment()
{
    private var mTitle: String? = null
    private var mContent: String? = null
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mIsExistNegativeBtn = true // 是否存在取消按钮
    private var mIsAboveRunningDialog = false // 是否存在于运行弹出框顶端
    private var mWarnPromptListener: WarnPromptListener? = null

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Warn_Prompt()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_prompt, null)

        val tv_info = v.findViewById<View>(R.id.tv_info) as TextView
        tv_info.text = mContent

        // 是否需要取消按钮
        if (this.mIsExistNegativeBtn)
            builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(v).setCancelable(false).setPositiveButton(this.mPositiveBtnStr) { _, _ -> mWarnPromptListener!!.clickedOk() }.setNegativeButton(this.mNegativeBtnStr) { _, _ ->
                dismissDialog()
                mWarnPromptListener!!.clickedCancel()
            }
        else
            builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(v).setCancelable(false).setPositiveButton(this.mPositiveBtnStr) { _, _ -> mWarnPromptListener!!.clickedOk() }

        val alertDialog = builder.create()
        // 设置按钮大小
        alertDialog.setOnShowListener {
            val btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE)
//            btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, dimen(R.dimen.dialog_btn_param_textSize).toFloat())

            val btnNegative = alertDialog.getButton(Dialog.BUTTON_NEGATIVE)
//            btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, dimen(R.dimen.dialog_btn_param_textSize).toFloat())
        }

        // 暂时FragmentDialog失去焦点，弹出FragmentDialog后，永久隐藏navigation bar. edited by liuja 2017-10-07
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

        isShow = true

        if(!this.mIsAboveRunningDialog)
        {
            // 禁止底部控件点击事件
            val view = requireActivity().window.decorView as FrameLayout
            ViewUtils.setSubControlsClickable(view, false)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()

        isShow = false

        if(!this.mIsAboveRunningDialog)
        {
            // 恢复底部控件点击事件
            val view = requireActivity().window.decorView as FrameLayout
            ViewUtils.setSubControlsClickable(view, true)
        }
    }

    /**
     * 取消Dialog
     */
    fun dismissDialog()
    {
        isShow = false
        dismiss()

        if(!this.mIsAboveRunningDialog)
        {
            // 恢复底部控件点击事件
            val view = requireActivity().window.decorView as FrameLayout
            ViewUtils.setSubControlsClickable(view, true)
        }
    }

    /**
     * 设置警告提示
     *
     * @param title 标题
     * @param content 内容
     * @param positiveBtnStr 确定按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param isExistNegativeBtn 是否存在取消按钮
     * @param isAboveRunningDialog 是否存在于运行弹出框顶端
     * @param warnPromptListener 警告提示监听接口
     */
    fun setDialogContent(title: String?, content: String, positiveBtnStr: String, negativeBtnStr: String, isExistNegativeBtn: Boolean, isAboveRunningDialog:Boolean,warnPromptListener: WarnPromptListener): DialogFragment_Warn_Prompt
    {
        this.mTitle = title
        this.mContent = content
        this.mPositiveBtnStr = positiveBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mIsExistNegativeBtn = isExistNegativeBtn
        this.mIsAboveRunningDialog = isAboveRunningDialog
        this.mWarnPromptListener = warnPromptListener

        return this
    }
}
