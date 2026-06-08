package com.superh2.p8

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.superh2.p8.MainActivity.Companion.isManualPause
import com.superh2.p8.MainActivity.Companion.isManualStop
import com.superh2.p8.MainActivity.Companion.isProcedureRunning
import com.superh2.p8.utils.ViewUtils.fullScreen

/**
 * Fragment 父类
 * A simple [Fragment] subclass.
 */
abstract class FragmentBase<VB : ViewBinding>(private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB) : Fragment()
{
    protected var mActivity: Activity? = null

    private var _binding: VB? = null
    protected val binding get() = _binding!!

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        fullScreen(mActivity)

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
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.framelayout_container, newFragment, newFragmentTag).commit()
    }

    /**
     * 切换Fragment界面
     * @param newFragment 新Fragment
     * @param newFragmentTag Fragment Tag
     */
    fun replaceFragment(newFragment: Fragment, newFragmentTag: String, bundle: Bundle)
    {
        newFragment.arguments=bundle
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.framelayout_container, newFragment, newFragmentTag).commit()
    }

    /**
     * 切换Fragment界面
     * @param containerId 容器id
     * @param newFragment 新Fragment
     * @param newFragmentTag Fragment Tag
     */
    fun replaceFragment(containerId: Int, newFragment: Fragment, newFragmentTag: String)
    {
        requireActivity().supportFragmentManager.beginTransaction().replace(containerId, newFragment, newFragmentTag).commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
