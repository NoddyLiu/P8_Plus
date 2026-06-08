package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.superh2.library.myInterface.RenameListener
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.superh2.library.myView.FloatingKeyboardViewNotDragable


/**
 * @description 手动输入二维码弹出框
 * @author liu_ja
 * @date 2021/08/06
 */
class DialogFragment_Input_Barcode_Manually : DialogFragment()
{
    private var mTitle: String? = null
    private var mBarcodeIndex = 0
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mRenameListener: RenameListener? = null

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Input_Barcode_Manually()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_input_barcode_manually, null)

        var et_input_barcode_manually = v.findViewById<EditText>(R.id.et_input_barcode_manually)
        var tv_slide_pos = v.findViewById<TextView>(R.id.tv_slide_pos)
        tv_slide_pos.text = " " +(mBarcodeIndex + 1).toString() + " "

        // 键盘
        val mCustomKeyboard = v.findViewById<FloatingKeyboardViewNotDragable>(R.id.keyboardview)
        mCustomKeyboard.setKeyboard(R.xml.keyboard_lowercase) // 设置键盘
        mCustomKeyboard.isPreviewEnabled = false
        mCustomKeyboard.registerEditText(et_input_barcode_manually, this)

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
                mRenameListener!!.confirm(mBarcodeIndex, et_input_barcode_manually.text.trim().toString())
                dismissDialog()
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
     * 设置内容
     *
     * @param title 标题
     * @param barcodeIndex 二维码Index
     * @param positiveBtnStr 确定按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param paramsRenameListener 监听接口
     */
    fun setDialogContent(title: String?, barcodeIndex: Int, positiveBtnStr: String, negativeBtnStr: String, paramsRenameListener: RenameListener): DialogFragment_Input_Barcode_Manually
    {
        this.mTitle = title
        this.mBarcodeIndex = barcodeIndex
        this.mPositiveBtnStr = positiveBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mRenameListener = paramsRenameListener

        return this
    }
}
