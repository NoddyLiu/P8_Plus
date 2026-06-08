package com.superh2.p8.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import com.superh2.library.myInterface.SlideFrameConfirmListener
import com.superh2.library.myView.SlideView
import com.superh2.p8.R
import android.widget.TextView
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.DialogFragment
import com.superh2.library.myEntityCommon.Barcode
import com.superh2.library.myEnum.ESlideStatus
import com.superh2.library.myInterface.RenameListener


/**
 * @description 扫码完成确认框
 * @author Noddy
 * @date 2019/02/25
 */
class DialogFragment_Slide_Frame : DialogFragment()
{
    private lateinit var mContext: Context
    private var mTitle: String? = null
    private var mPositiveBtnStr: String? = null // 确定按钮文本
    private var mNeutralBtnStr: String? = null // 重试按钮文本
    private var mNegativeBtnStr: String? = null // 取消按钮文本
    private var mScannedSlideList: MutableList<Barcode> = mutableListOf() // 已扫描玻片List
    private var mSlideFrameConfirmListener: SlideFrameConfirmListener? = null

    private lateinit var mGridLayout_slide: GridLayout

    // 64个玻片
    var listSlide64 = ArrayList<SlideView>(64)

    var isShow: Boolean = false

    companion object
    {
        fun newInstance() = DialogFragment_Slide_Frame()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val v = inflater.inflate(R.layout.dialog_fragment_slide_frame, null)

        mGridLayout_slide = v.findViewById(R.id.gridLayout_slide) as GridLayout

        // 标题字体大小
        val title = TextView(context)
        title.text = this.mTitle
        title.height = 50
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35f)
        title.setTextColor(Color.BLACK)
        title.gravity = Gravity.CENTER
        builder.setCustomTitle(title)
                .setView(v)
                .setCancelable(false)
                .setPositiveButton(this.mPositiveBtnStr) { _, _ ->
                    dismissDialog()
                    mSlideFrameConfirmListener!!.clickedOk()
                }
                .setNeutralButton(this.mNeutralBtnStr) { _, _ ->
                    dismissDialog()
                    mSlideFrameConfirmListener!!.clickedRescan()
                }
                .setNegativeButton(this.mNegativeBtnStr) { _, _ ->
                    dismissDialog()
                    mSlideFrameConfirmListener!!.clickedCancel()
                }

        val alertDialog = builder.create()
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

        // 玻片架
        for (i in 0..63)
        {
            var slide = SlideView(mContext)
            slide.text = (i + 1).toString()
            slide.scanContentVisible = true
            slide.padding = 30f
            slide.scanContent =  this.mScannedSlideList.find { it.posIndex == i }!!.barcode
            // 长按编辑
            slide.setOnLongClickListener {
                DialogFragment_Edit_Barcode.newInstance().setDialogContent(getString(R.string.info_input_barcode_new), i, slide.scanContent, getString(R.string.confirm), getString(R.string.cancel), object : RenameListener
                {
                    override fun confirm(index: Int, newValue: String)
                    {
                        var barcode = mScannedSlideList.first { it.posIndex == index }
                        barcode.barcode = newValue.trim()
                        slide.scanContent = newValue.trim()
                    }

                    override fun cancel()
                    {
                    }
                }).show(parentFragmentManager, null)
                true
            }

            val row = i / 16
            val col = i % 16

            val layoutParams = GridLayout.LayoutParams(GridLayout.spec(row, GridLayout.CENTER), GridLayout.spec(col, GridLayout.CENTER))
            layoutParams.setMargins(0, 0, 0, 10)
            mGridLayout_slide.addView(slide, layoutParams)

            listSlide64.add(slide)
        }

        this.mScannedSlideList.forEach {
            listSlide64[it.posIndex].slideStatus = ESlideStatus.Finished
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()

        isShow = false
    }

    /**
     * 取消Dialog
     */
    fun dismissDialog()
    {
        isShow = false
        dismiss()
    }

    /**
     * 设置警告提示
     *
     * @param context
     * @param title 标题
     * @param positiveBtnStr 确定按钮文本
     * @param neutralBtnStr 重试按钮文本
     * @param negativeBtnStr 取消按钮文本
     * @param scannedSlideList 已扫描玻片List
     * @param slideFrameConfirmListener 警告提示监听接口
     */
    fun setDialogContent(context: Context, title: String?, positiveBtnStr: String, neutralBtnStr: String, negativeBtnStr: String, scannedSlideList: MutableList<Barcode>, slideFrameConfirmListener: SlideFrameConfirmListener): DialogFragment_Slide_Frame
    {
        this.mContext = context
        this.mTitle = title
        this.mPositiveBtnStr = positiveBtnStr
        this.mNeutralBtnStr = neutralBtnStr
        this.mNegativeBtnStr = negativeBtnStr
        this.mScannedSlideList = scannedSlideList
        this.mSlideFrameConfirmListener = slideFrameConfirmListener

        return this
    }
}
