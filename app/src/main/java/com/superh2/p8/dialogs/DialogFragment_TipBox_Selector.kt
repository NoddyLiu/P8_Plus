package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.Toast
import com.superh2.library.myInterface.TipBoxSelectorListener
import com.superh2.library.myView.CircleView
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.R

/**
 * @description 枪头盒起始位置选择框
 * @author liu_ja
 * @date 2021/08/21
 */
class DialogFragment_TipBox_Selector : DialogFragment()
{
    // 96个枪头孔
    private var listTipBoxHole96 = ArrayList<CircleView>(96)

    private lateinit var gridLayoutTipBox: GridLayout

    private var mTipBoxSelectorListener: TipBoxSelectorListener? = null

    // 旧起始位置
    private var mStartIndexOld = 0

    // 所需枪头数目（null：不需要提示）
    private var mTipCountNeed: Int? = null

    // 新起始位置
    private var mStartIndexNew = 0

    companion object
    {
        fun newInstance() = DialogFragment_TipBox_Selector()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_tipbox_selector, null)

        gridLayoutTipBox = v.findViewById(R.id.gridLayout_tipBox)

        builder.setIcon(android.R.drawable.ic_dialog_info).setTitle(getString(R.string.info_pls_select_tipbox_start_pos)).setView(v).setCancelable(false).setPositiveButton(getString(R.string.confirm),null).setNegativeButton(getString(R.string.cancel),null)

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

        initWidget()

        // 禁止底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, false)

        // 确定按钮
        val d = dialog as AlertDialog
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // 选择的枪头起始位下，一共多少个枪头
            val tipCountRemain = (11 - mStartIndexNew / 8) * 8 + (8 - mStartIndexNew % 8)
            if (mTipCountNeed != null && mTipCountNeed!! > tipCountRemain)
            {
                Toast.makeText(activity, getString(R.string.info_pls_select_tip_count_enough), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dismissDialog()
            mTipBoxSelectorListener!!.clickedOk(mStartIndexNew)
        }
        // 取消按钮
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{
            dismissDialog()
            mTipBoxSelectorListener!!.clickedCancel()
        }
    }

    private fun initWidget()
    {
        // 枪头
        for (i in 0..95)
        {
            var tip = CircleView(context)
            tip.text = (i + 1).toString()
            tip.textSize = 23f
            tip.radius = 25f
            val row = i / 8
            val col = i % 8
            gridLayoutTipBox.addView(tip, GridLayout.LayoutParams(GridLayout.spec(row, GridLayout.CENTER), GridLayout.spec(col, GridLayout.CENTER)))

            // 点击事件
            tip.setOnClickListener {
                mStartIndexNew = i
                refreshStartIndex(mStartIndexNew)
            }

            listTipBoxHole96.add(tip)
        }

        // 刷新起始位置
        refreshStartIndex(mStartIndexOld)
    }

    /**
     * 刷新起始位置
     * @param startIndex  起始位置
     */
    private fun refreshStartIndex(startIndex: Int)
    {
        for (i in 0..95)
        {
            var tip = listTipBoxHole96[i]
            tip.isFill = i >= startIndex
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
     * 取消Dialog
     */
    private fun dismissDialog()
    {
        dismiss()

        // 恢复底部控件点击事件
        val view = activity.window.decorView as FrameLayout
        ViewUtils.setSubControlsClickable(view, true)
    }

    /**
     * 设置窗口回调
     * @param startIndexOld  旧起始位置
     * @param tipCountNeed 所需枪头数
     * @param tipBoxSelectorListener 监听接口
     */
    fun setDialogContent(startIndexOld: Int, tipCountNeed: Int?, tipBoxSelectorListener: TipBoxSelectorListener): DialogFragment_TipBox_Selector
    {
        this.mStartIndexOld = startIndexOld
        this.mStartIndexNew = this.mStartIndexOld
        this.mTipCountNeed = tipCountNeed
        this.mTipBoxSelectorListener = tipBoxSelectorListener
        return this
    }
}
