package com.superh2.library.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*

/**
 * view工具类
 * Created by Noddy on 2018/4/23.
 */
object ViewUtils
{
    /**
     * 保持dialog不关闭
     * @param dialog
     */
    fun keepDialogOpen(dialog: AlertDialog)
    {
        try
        {
            val field = dialog.javaClass.superclass.getDeclaredField("mShowing")
            field.isAccessible = true
            field.set(dialog, false)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

    }

    /**
     * 关闭dialog
     * @param dialog
     */
    fun closeDialog(dialog: AlertDialog)
    {
        try
        {
            val field = dialog.javaClass.superclass.getDeclaredField("mShowing")
            field.isAccessible = true
            field.set(dialog, true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 改变弹出框字体大小
     * @param dialog
     */
    fun changeAlertdialogTextSize(dialog: AlertDialog)
    {
        val btn_positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btn_negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val btn_neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        try
        {
            val textView = dialog.findViewById(android.R.id.message) as TextView
            textView.textSize = 35f
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        try
        {
            btn_negative.textSize = 35f
            btn_neutral.textSize = 35f
            btn_positive.textSize = 35f
        }
        catch (e: Exception)
        {

        }
    }

    /***
     * 设置布局下的所有控件是否可以点击
     * @param viewGroup 父布局
     * @param clickable 是否可以点击
     */
    fun setSubControlsClickable(viewGroup: ViewGroup, clickable: Boolean)
    {
        for (i in 0 until viewGroup.childCount)
        {
            val v = viewGroup.getChildAt(i)
            if (v is ViewGroup)
            {
                if (v is ListView)
                {
                    v.setClickable(clickable)
                    v.setEnabled(clickable)
                }
                else
                    setSubControlsClickable(v, clickable)
            }
            else if (v is Button)
            {
                v.isEnabled = clickable
                v.isClickable = clickable
            }
            else if (v is EditText)
            {
                v.isEnabled = clickable
                v.isClickable = clickable
            }
            else if (v is Checkable)
            {
                v.isEnabled = clickable
                v.isClickable = clickable
            }
        }
    }

    /** 隐藏软键盘
     * @param mActivity 当前Activity
     */
    fun  hideSoftKeyboard(mActivity:Activity)
    {
        val view = mActivity.currentFocus
        if (view != null)
        {
            val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }
}
