package com.superh2.p8

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

/**
 * 参数界面 父类
 * A simple [Fragment] subclass.
 */
abstract class FragmentMethodParametersBase : Fragment()
{
    protected var mActivity: Activity? = null
    protected var rootView: View? = null // 根视图，即需要显示的界面

    companion object
    {
        private val TAG = "FragmentMethodParametersBase"
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        initWidget()
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
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.framelayout_method_parameters_container, newFragment, newFragmentTag).commit()
    }
}
