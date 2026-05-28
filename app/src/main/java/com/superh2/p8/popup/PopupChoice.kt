package com.superh2.p8.popup

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.superh2.library.myInterface.ISelectedListener
import com.superh2.p8.R
import razerdp.basepopup.BasePopupWindow


/**
 *@Description 选择弹出框
 *@Author liu_ja
 *@Time 2020/04/02 22:39
 */
class PopupChoice(context: Context?, info: String, callback: ISelectedListener?) : BasePopupWindow(context)
{
    private val mContext = context
    private val mPrompt = info
    private val mCallBack = callback

    init
    {
        setContentView(R.layout.popup_choice)
    }

    override fun onViewCreated(contentView: View)
    {
        super.onViewCreated(contentView)
        initWidget()
    }

    private fun initWidget()
    {
        // 点击外部不能关闭弹出框
        setOutSideDismiss(false)

        findViewById<TextView>(R.id.tv_prompt).text = mPrompt

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            this.dismiss()
            mCallBack?.cancel()
        }
        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            this.dismiss()
            mCallBack?.confirm()
        }
    }

}