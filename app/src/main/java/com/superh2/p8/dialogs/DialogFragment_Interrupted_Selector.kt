package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.superh2.library.myInterface.INewIndexSelectedListener


/**
 * @description 异常中断继续运行选择弹出框
 * @author liu_ja
 * @date 2025/09/16
 */
class DialogFragment_Interrupted_Selector : DialogFragment()
{
    private var mOldIndex = 0
    private var mOldItemStr: String? = null
    private var mNewItemStr: String? = null
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mNewIndexSelectedListener: INewIndexSelectedListener? = null

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Interrupted_Selector()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_interrupted_selector, null)

        var tv_old_item_name = v.findViewById<TextView>(R.id.tv_old_item_name)
        var tv_new_item_name = v.findViewById<TextView>(R.id.tv_new_item_name)
        var tv_pos_interrupted = v.findViewById<TextView>(R.id.tv_pos_interrupted)
        var spinner_pos_continued = v.findViewById<Spinner>(R.id.spinner_pos_continued)
        tv_old_item_name.text = this.mOldItemStr
        tv_new_item_name.text = this.mNewItemStr
        tv_pos_interrupted.text = (this.mOldIndex + 1).toString()
        // 设置 Spinner 的数据范围为 1 到 64
        val options = (1..64).map { it.toString() }
        val adapter = ArrayAdapter(activity, R.layout.spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_pos_continued.adapter = adapter
        spinner_pos_continued.setSelection(this.mOldIndex)

        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(getString(R.string.prompt)).setView(v).setCancelable(false)
                .setPositiveButton(this.mPositiveBtnStr, null)
                .setNegativeButton(this.mNegativeBtnStr, null)

        val alertDialog = builder.create()

        // 自定义确定按钮动作
        alertDialog.setOnShowListener {
            // 确定按钮
            val posButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            posButton.setOnClickListener {
                var newIndex = spinner_pos_continued.selectedItemPosition
                mNewIndexSelectedListener!!.confirm(newIndex)
                dismissDialog()
            }

            // 取消按钮
            val negButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
            negButton.setOnClickListener {
                mNewIndexSelectedListener!!.cancel()
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
     * @param oldIndex 旧Index
     * @param oldItemStr 旧项
     * @param newItemStr 新项
     * @param positiveBtnStr 确定按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param selectedListener 监听接口
     */
    fun setDialogContent(oldIndex: Int, oldItemStr: String, newItemStr: String, positiveBtnStr: String, negativeBtnStr: String, selectedListener: INewIndexSelectedListener): DialogFragment_Interrupted_Selector
    {
        this.mOldIndex = oldIndex
        this.mOldItemStr = oldItemStr
        this.mNewItemStr = newItemStr
        this.mPositiveBtnStr = positiveBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mNewIndexSelectedListener = selectedListener

        return this
    }
}
