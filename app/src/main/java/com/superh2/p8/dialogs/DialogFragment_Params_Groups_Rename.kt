package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.superh2.library.myInterface.RenameListener
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R
import android.view.WindowManager
import com.superh2.library.myView.FloatingKeyboardViewNotDragable


/**
 * @description 参数组修改名称弹出框
 * @author Noddy
 * @date 2018/11/04
 */
class DialogFragment_Params_Groups_Rename : DialogFragment()
{
    private var mTitle: String? = null
    private var mSelectedIndex = 0
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mRenameListener: RenameListener? = null

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Params_Groups_Rename()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_params_groups_rename, null)

        // 原组名
        var et_params_group_rename = v.findViewById<EditText>(R.id.et_params_group_rename)
        var tv_params_group_rename_error = v.findViewById<TextView>(R.id.tv_params_group_rename_error)
        et_params_group_rename.hint = paramMethodParamsGroup.methodParamsGroup[this.mSelectedIndex].groupName

        // 键盘
        val mCustomKeyboard = v.findViewById<FloatingKeyboardViewNotDragable>(R.id.keyboardview)
        mCustomKeyboard.setKeyboard(R.xml.keyboard_lowercase) // 设置键盘
        mCustomKeyboard.isPreviewEnabled = false
        mCustomKeyboard.registerEditText(et_params_group_rename, this)

        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(v).setCancelable(false)
                .setPositiveButton(this.mPositiveBtnStr, null)
                .setNegativeButton(this.mNegativeBtnStr) { _, _ ->
                    dismissDialog()
                }

        val alertDialog = builder.create()

        // 自定义确定按钮动作
        alertDialog.setOnShowListener {
            // 确定按钮
            val posButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            posButton.setOnClickListener {
                val text = et_params_group_rename.text.trim()
                when
                {
                    text.isNullOrEmpty() -> tv_params_group_rename_error.text = getString(R.string.info_group_name_cannot_empty)
                    text.length > 15 -> tv_params_group_rename_error.text = getString(R.string.info_group_name_is_too_long)
                    else ->
                    {
                        mRenameListener!!.confirm(mSelectedIndex, et_params_group_rename.text.toString())
                        dismissDialog()
                    }
                }
            }

            // 取消按钮
            val negButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
            negButton.setOnClickListener {
                mRenameListener!!.cancel()
                dismissDialog()
            }
        }

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
     * @param selectedIndex 已选择的Index
     * @param positiveBtnStr 确定按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param paramsRenameListener 监听接口
     */
    fun setDialogContent(title: String?, selectedIndex: Int, positiveBtnStr: String, negativeBtnStr: String, paramsRenameListener: RenameListener): DialogFragment_Params_Groups_Rename
    {
        this.mTitle = title
        this.mSelectedIndex = selectedIndex
        this.mPositiveBtnStr = positiveBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mRenameListener = paramsRenameListener

        return this
    }
}
