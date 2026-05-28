package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.superh2.library.myInterface.ParamsNumSetListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Number
import kotlinx.android.synthetic.main.fragment_method_parameters_more.*

/**
 *@Description 方法参数设置界面次页
 *@Author  Noddy
 */
class FragmentMethodParametersMore : FragmentMethodParametersBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMethodParametersMore()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_method_parameters_more, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        btn_spray_liquid_time_before_move.setOnClickListener(this)
        btn_last.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        tv_spray_liquid_time_before_move.text = selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove.toString()
    }

    private fun saveAndRefresh()
    {
        FileUtils.saveMethodParametersGroup(paramMethodParamsGroup, true)
        refreshControls()
    }

    override fun onClick(v: View)
    {
        when (v.id)
        {
            R.id.btn_last -> replaceFragment(FragmentMethodParameters.newInstance(), "FragmentMethodParameters")
            R.id.btn_spray_liquid_time_before_move -> // 喷雾移动前固定时间
            {
                DialogFragment_Params_Setting_Number.newInstance()
                        .setDialogParams(getString(R.string.method_params_spray_liquid_time_before_move), 1, selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove, 0, 3, object : ParamsNumSetListener
                        {
                            override fun valueSetCompleted(number: Int)
                            {
                                selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove = number
                                saveAndRefresh()
                            }
                        })
                        .show(fragmentManager, null)
            }
        }
    }

}
