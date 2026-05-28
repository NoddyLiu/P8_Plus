package com.superh2.p8

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import com.superh2.library.utils.ViewUtils
import com.superh2.p8.MainActivity.Companion.isManualPause
import com.superh2.p8.MainActivity.Companion.isManualStop
import com.superh2.p8.MainActivity.Companion.isProcedureRunning

/**
 * Fragment 父类
 * A simple [Fragment] subclass.
 */
abstract class FragmentBase : Fragment()
{
    protected var mActivity: Activity? = null
    protected var rootView: View? = null // 根视图，即需要显示的界面

    companion object
    {
        private val TAG = "FragmentBase"
    }

    // sdk < 23调用此方法
    override fun onAttach(activity: Activity)
    {
        super.onAttach(activity)
        mActivity = activity
    }

    // sdk >= 23调用此方法
    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        fullScreen()

        // 默认隐藏顶部右功能键
        val btnFunctionRight = mActivity?.findViewById(R.id.btnFunctionRight) as Button
        btnFunctionRight.visibility = View.GONE

        // 默认隐藏顶部参数组
        val layoutParamsGroup = mActivity?.findViewById(R.id.layout_params_group) as LinearLayout
        layoutParamsGroup.visibility = View.GONE

        initWidget()

        // 初始化运行属性，以防执行指令时导致没有捕获异常
        isProcedureRunning = false
        isManualPause = false
        isManualStop = false
    }

    /**
     * 全屏
     */
    fun fullScreen()
    {
        rootView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        mActivity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        ViewUtils.hideSoftKeyboard(activity)
    }

    /**
     * 初始化控件
     */
    protected abstract fun initWidget()

    /**
     * 切换Fragment界面
     * @param newFragment 新Fragment
     * @param newFragmentTag Fragment Tag
     */
    fun replaceFragment(newFragment: Fragment, newFragmentTag: String)
    {
        mActivity!!.fragmentManager.beginTransaction().replace(R.id.framelayout_container, newFragment, newFragmentTag).commit()
    }

    /**
     * 切换Fragment界面
     * @param containerId 容器id
     * @param newFragment 新Fragment
     * @param newFragmentTag Fragment Tag
     */
    fun replaceFragment(containerId: Int, newFragment: Fragment, newFragmentTag: String)
    {
        mActivity!!.fragmentManager.beginTransaction().replace(containerId, newFragment, newFragmentTag).commit()
    }
}
