package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R
import com.superh2.library.myInterface.IDispersancyIndexCallback
import com.superh2.library.utils.ParamsHelper


/**
 * @description 分散指数修改弹出框
 * @author liu_ja
 * @date 2023/11/27
 */
class DialogFragment_Dispersancy_Index : DialogFragment()
{
    protected lateinit var rootView: View // 根视图，即需要显示的界面

    private var mTitle: String? = null
    private var mSelectedIndex = 0
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mCallback: IDispersancyIndexCallback? = null
    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Dispersancy_Index()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        rootView = inflater.inflate(R.layout.dialog_fragment_dispersancy_index, null)

        var humiture = ParamsHelper.paramGeneralParams.DispersancyIndexList[mSelectedIndex]
        rootView.findViewById<EditText>(R.id.et_dispersancy_index_temp).setText(humiture.temp.toString())
        rootView.findViewById<EditText>(R.id.et_dispersancy_index_humi).setText(humiture.humi.toString())
        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(this.mTitle).setView(rootView).setCancelable(false)
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
                humiture.temp = rootView.findViewById<EditText>(R.id.et_dispersancy_index_temp).text.toString().toFloat()
                humiture.humi = rootView.findViewById<EditText>(R.id.et_dispersancy_index_humi).text.toString().toFloat()
                mCallback?.confirm(humiture)
                dismissDialog()
            }

            // 取消按钮
            val negButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
            negButton.setOnClickListener {
                mCallback?.cancel()
                dismissDialog()
            }
        }

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
     * @param selectedIndex 已选择的Index
     * @param positiveBtnStr 确定按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param dispersancyIndexCallback 监听接口
     */
    fun setDialogContent(title: String?, selectedIndex: Int,positiveBtnStr: String, negativeBtnStr: String, dispersancyIndexCallback: IDispersancyIndexCallback): DialogFragment_Dispersancy_Index
    {
        this.mTitle = title
        this.mSelectedIndex = selectedIndex
        this.mPositiveBtnStr = positiveBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mCallback = dispersancyIndexCallback

        return this
    }
}
