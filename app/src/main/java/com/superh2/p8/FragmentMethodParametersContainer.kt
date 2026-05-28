package com.superh2.p8


import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.superh2.library.myInterface.RenameListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.dialogs.DialogFragment_Params_Groups_Rename
import kotlinx.android.synthetic.main.fragment_method_parameters_container.*


/**
 *@Description 方法参数设置容器界面
 *@Author  Noddy
 */
class FragmentMethodParametersContainer : FragmentBase(), View.OnClickListener, View.OnLongClickListener
{
    private var imgLogo: ImageView? = null

    // 当前选择参数组Index
    var selectedMethodParamsIndex = 0

    companion object
    {
        fun newInstance() = FragmentMethodParametersContainer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_method_parameters_container, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        selectedMethodParams = paramMethodParamsGroup.methodParamsGroup[0]
        selectedMethodParamsIndex = 0
        this.replaceFragment(R.id.framelayout_method_parameters_container, FragmentMethodParameters.newInstance(), "FragmentMethodParameters")

        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        imgLogo = mActivity?.findViewById(R.id.img_logo) as ImageView
        imgLogo?.setOnLongClickListener(this)

        // TabLayout
        for (i in 1..20)
        {
            val selectedIndex = i - 1
            val parent = LayoutInflater.from(context).inflate(R.layout.custom_tab_title, null)
            val tv = parent.findViewById<TextView>(R.id.tv_tab_title)
            tv.text = "$i." + paramMethodParamsGroup.methodParamsGroup[selectedIndex].groupName
            tabLayout.addTab(tabLayout.newTab().setCustomView(parent))

            // 长按事件
            parent.setOnLongClickListener {
                DialogFragment_Params_Groups_Rename.newInstance().setDialogContent(getString(R.string.parameter_group_rename), selectedIndex, getString(R.string.confirm), getString(R.string.cancel), object : RenameListener
                {
                    override fun confirm(index: Int, newValue: String)
                    {
                        paramMethodParamsGroup.methodParamsGroup[index].groupName = newValue
                        FileUtils.saveMethodParametersGroup(paramMethodParamsGroup, true)
                        // 刷新界面
                        tabLayout.getTabAt(index)?.customView?.findViewById<TextView>(R.id.tv_tab_title)?.text = "$i.$newValue"
                    }

                    override fun cancel()
                    {
                    }
                }).show(fragmentManager, null)
                true
            }
            // 点击事件
            parent.setOnClickListener {
                val tab = tabLayout.getTabAt(selectedIndex)
                tab?.select()

                if (selectedIndex != selectedMethodParamsIndex)
                {
                    selectedMethodParams = paramMethodParamsGroup.methodParamsGroup[selectedIndex]
                    selectedMethodParamsIndex = selectedIndex
                    replaceFragment(R.id.framelayout_method_parameters_container, FragmentMethodParameters.newInstance(), "FragmentMethodParameters")
                }
            }
        }

        val btnNext = mActivity?.findViewById(R.id.btnFunctionRight) as Button
        btnNext.visibility = View.GONE
    }

    override fun onDestroy()
    {
        super.onDestroy()

        // 退出该页面后，长按logo不进入工程师模式
        imgLogo?.setOnLongClickListener(null)
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMain.newInstance(), "FragmentMain")
        }
    }

    override fun onLongClick(p0: View): Boolean
    {
        // 长按logo进入工程师模式
        if (p0.id == R.id.img_logo)
        {
            replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
        }
        return true
    }
}
