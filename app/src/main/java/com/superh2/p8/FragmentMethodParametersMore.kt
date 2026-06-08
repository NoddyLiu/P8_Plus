package com.superh2.p8


import android.view.View
import com.superh2.library.myInterface.ParamsNumSetListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.databinding.FragmentMethodParametersMoreBinding
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Number

/**
 *@Description 方法参数设置界面次页
 *@Author  Noddy
 */
class FragmentMethodParametersMore : FragmentBase<FragmentMethodParametersMoreBinding>(FragmentMethodParametersMoreBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMethodParametersMore()
    }

    override fun initWidget()
    {
        binding.btnSprayLiquidTimeBeforeMove.setOnClickListener(this)
        binding.btnLast.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.tvSprayLiquidTimeBeforeMove.text = selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove.toString()
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
            R.id.btn_last -> replaceFragment(R.id.framelayout_method_parameters_container, FragmentMethodParameters.newInstance(), "FragmentMethodParameters")
            R.id.btn_spray_liquid_time_before_move -> // 喷雾移动前固定时间
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_spray_liquid_time_before_move), 1, selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove, 0, 3, object : ParamsNumSetListener
                        {
                            override fun valueSetCompleted(number: Int)
                            {
                                selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove = number
                                saveAndRefresh()
                            }
                        }).show(parentFragmentManager, null)
            }
        }
    }

}
