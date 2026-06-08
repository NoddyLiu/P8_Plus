package com.superh2.p8


import android.view.View
import com.superh2.library.myEnum.EAngle
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myEnum.ESpreadingMode
import com.superh2.library.myInterface.ParamsDecimalSetListener
import com.superh2.library.myInterface.ParamsNumSetListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.databinding.FragmentMethodParametersBinding
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Decimal
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Number

/**
 *@Description 方法参数设置界面首页
 *@Author  Noddy
 */
class FragmentMethodParameters : FragmentBase<FragmentMethodParametersBinding>(FragmentMethodParametersBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMethodParameters()
    }

    override fun initWidget()
    {
        binding.rgSpreadingMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId)
            {
                R.id.rbtn_dry_slide ->
                {
                    selectedMethodParams.spreadingMode = ESpreadingMode.Dry
                    saveAndRefresh()
                }
                R.id.rbtn_wet_slide ->
                {
                    selectedMethodParams.spreadingMode = ESpreadingMode.Wet
                    saveAndRefresh()
                }
            }
        }

        binding.switchbtnBarcode.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
            {
                selectedMethodParams.barcode = EOnOff.On
                binding.layoutTubeQuantity.visibility = View.INVISIBLE
            }
            else
            {
                selectedMethodParams.barcode = EOnOff.Off
                binding.layoutTubeQuantity.visibility = View.VISIBLE
            }
            saveAndRefresh()
        }

        binding.switchbtnSlideAutomatic.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked) selectedMethodParams.slideAutomatic = EOnOff.On
            else selectedMethodParams.slideAutomatic = EOnOff.Off
            saveAndRefresh()
        }

        binding.rgAngleOfSlide.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId)
            {
                R.id.rbtn_angle_of_slide_zero ->
                {
                    selectedMethodParams.angleOfSlide = EAngle.Zero
                    saveAndRefresh()
                }
                R.id.rbtn_angle_of_slide_5 ->
                {
                    selectedMethodParams.angleOfSlide = EAngle.Five
                    saveAndRefresh()
                }
                R.id.rbtn_angle_of_slide_10 ->
                {
                    selectedMethodParams.angleOfSlide = EAngle.Ten
                    saveAndRefresh()
                }
            }
        }

        binding.btnSlideQuantity.setOnClickListener(this)
        binding.btnTubeQuantity.setOnClickListener(this)
        binding.btnSlidePerTube.setOnClickListener(this)
        binding.btnSpotPerSlide.setOnClickListener(this)

        binding.btnFixativeVolumnBeforeSpray.setOnClickListener(this)
        binding.btnDelayTimeAfterSpray.setOnClickListener(this)
        binding.btnFixativeVolumnAfterSpray.setOnClickListener(this)
        binding.btnFixativeDispensePressure.setOnClickListener(this)
        binding.btnVolumnPerDrop.setOnClickListener(this)
        binding.btnDispenseHeight.setOnClickListener(this)
        binding.btnDispenseSpeed.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)

        binding.btnDelayTimeBeforeSampleDropping.setOnClickListener(this)
        binding.btnDelayTimeAfterSampleDropping.setOnClickListener(this)
        binding.btnFixativeVolumnPerDrop.setOnClickListener(this)
        binding.btnYSpeedWhenSpraying.setOnClickListener(this)
        binding.btnSprayLiquidAdditionInterval.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        if (selectedMethodParams.spreadingMode == ESpreadingMode.Dry)
            binding.rbtnDrySlide.isChecked = true
        else
            binding.rbtnWetSlide.isChecked = true

        binding.switchbtnBarcode.isChecked = selectedMethodParams.barcode != EOnOff.Off

        if(selectedMethodParams.barcode == EOnOff.On)
        {
            binding.layoutSlideQuantity.visibility = View.VISIBLE
            binding.tvSlideQuantity.text  = selectedMethodParams.slideQuantity.toString()
            binding.layoutTubeQuantity.visibility = View.INVISIBLE
        }
        else
        {
            binding.layoutSlideQuantity.visibility = View.INVISIBLE
            binding.layoutTubeQuantity.visibility = View.VISIBLE
            binding.tvTubeQuantity.text = selectedMethodParams.tubeQuantity.toString()
        }

        binding.switchbtnSlideAutomatic.isChecked = selectedMethodParams.slideAutomatic == EOnOff.On

        binding.tvSlidePerTube.text = selectedMethodParams.slidePerTube.toString()
        binding.tvSpotPerSlide.text = selectedMethodParams.spotPerSlide.toString()

        when (selectedMethodParams.angleOfSlide)
        {
            EAngle.Zero -> binding.rbtnAngleOfSlideZero.isChecked = true
            EAngle.Five -> binding.rbtnAngleOfSlide5.isChecked = true
            EAngle.Ten -> binding.rbtnAngleOfSlide10.isChecked = true
        }

        binding.tvFixativeVolumnBeforeSpray.text = selectedMethodParams.paramsSlideMode.fixativeVolumnBeforeSpray.toString()
        binding.tvDelayTimeAfterSpray.text = selectedMethodParams.paramsSlideMode.delayTimeAfterSpray.toString()
        binding.tvFixativeVolumnAfterSpray.text = selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray.toString()
        binding.tvFixativeDispensePressure.text = selectedMethodParams.paramsSlideMode.fixativeDispensePressure.toString()
        binding.tvVolumnPerDrop.text = selectedMethodParams.paramsSlideMode.volumnPerDrop.toString()
        binding.tvDispenseHeight.text = selectedMethodParams.paramsSlideMode.dispenseHeight.toString()
        binding.tvDispenseSpeed.text = selectedMethodParams.paramsSlideMode.dispenseSpeed.toString()

        binding.tvDelayTimeBeforeSampleDropping.text =   selectedMethodParams.paramsSlideMode.delayTimeBeforeSampleDropping.toString()
        binding.tvDelayTimeAfterSampleDropping.text = selectedMethodParams.paramsSlideMode.delayTimeAfterSampleDropping.toString()
        binding.tvFixativeVolumnPerDrop.text =
            selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop.toString()
        binding.tvYSpeedWhenSpraying.text = selectedMethodParams.paramsSlideMode.ySpeedWhenSpray.toString()
        binding.tvSprayLiquidAdditionInterval.text = selectedMethodParams.paramsSlideMode.sprayLiquidAdditionInterval.toString()
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
            R.id.btn_next -> this.replaceFragment(R.id.framelayout_method_parameters_container, FragmentMethodParametersMore.newInstance(), "FragmentMethodParametersMore")
            R.id.btn_slide_quantity -> // 玻片数量
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_slide_quantity), 2, selectedMethodParams.slideQuantity, 1, 64, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.slideQuantity = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_tube_quantity -> // 试管数量
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_tube_quantity), 2, selectedMethodParams.tubeQuantity, 1, 64, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.tubeQuantity = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_slide_per_tube -> // 滴片数量
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_slide_per_tube), 2, selectedMethodParams.slidePerTube, 1, 10, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.slidePerTube = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_spot_per_slide -> // 滴片点数
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_spot_per_slide), 1, selectedMethodParams.spotPerSlide, 1, 3, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.spotPerSlide = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_fixative_volumn_before_spray -> // 雾化前固定
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_fixative_volumn_before_spray), 3, selectedMethodParams.paramsSlideMode.fixativeVolumnBeforeSpray, 0, 100, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.paramsSlideMode.fixativeVolumnBeforeSpray = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_delay_time_after_spray -> // 雾化后等待
            {
                DialogFragment_Params_Setting_Decimal.newInstance().setDialogParams(getString(R.string.method_params_delay_time_after_spray), null, 1, selectedMethodParams.paramsSlideMode.delayTimeAfterSpray, 0f, 3f, object : ParamsDecimalSetListener
                {
                    override fun valueSetCompleted(number: Float)
                    {
                        selectedMethodParams.paramsSlideMode.delayTimeAfterSpray = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_fixative_volumn_after_spray -> // 雾化后固定
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_fixative_volumn_after_spray), 3, selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray, 0, 100, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_fixative_dispense_pressure -> // 固定液压力
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_fixative_dispense_pressure), 2, selectedMethodParams.paramsSlideMode.fixativeDispensePressure, 1, 12, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.paramsSlideMode.fixativeDispensePressure = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_volumn_per_drop -> // 滴样体积
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_volumn_per_drop), 3, selectedMethodParams.paramsSlideMode.volumnPerDrop, 10, 100, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.paramsSlideMode.volumnPerDrop = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_dispense_height -> // 滴液高度
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.dispense_height), 3, selectedMethodParams.paramsSlideMode.dispenseHeight, 1, 100, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.paramsSlideMode.dispenseHeight = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }
            R.id.btn_dispense_speed -> // 滴液速度
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_dispense_speed), 2, selectedMethodParams.paramsSlideMode.dispenseSpeed, 1, 12, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.paramsSlideMode.dispenseSpeed = number
                        saveAndRefresh()
                    }
                }).show(parentFragmentManager, null)
            }

            R.id.btn_delay_time_before_sample_dropping -> // 滴样前等待时间
            {
                DialogFragment_Params_Setting_Decimal.newInstance()
                        .setDialogParams(getString(R.string.method_params_delay_time_before_sample_dropping), null, 1, selectedMethodParams.paramsSlideMode.delayTimeBeforeSampleDropping, 0f, 5f, object : ParamsDecimalSetListener
                        {
                            override fun valueSetCompleted(number: Float)
                            {
                                selectedMethodParams.paramsSlideMode.delayTimeBeforeSampleDropping =
                                    number
                                saveAndRefresh()
                            }
                        })
                        .show(parentFragmentManager, null)
            }
            R.id.btn_delay_time_after_sample_dropping -> // 滴样后等待时间
            {
                DialogFragment_Params_Setting_Decimal.newInstance()
                        .setDialogParams(getString(R.string.method_params_delay_time_after_sample_dropping), null, 1, selectedMethodParams.paramsSlideMode.delayTimeAfterSampleDropping, 0f, 3f, object : ParamsDecimalSetListener
                        {
                            override fun valueSetCompleted(number: Float)
                            {
                                selectedMethodParams.paramsSlideMode.delayTimeAfterSampleDropping =
                                    number
                                saveAndRefresh()
                            }
                        })
                        .show(parentFragmentManager, null)
            }
            R.id.btn_fixative_volumn_per_drop -> // 追加固定
            {
                DialogFragment_Params_Setting_Number.newInstance()
                        .setDialogParams(getString(R.string.method_params_fixative_volumn_per_drop), 3, selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop, 0, 100, object : ParamsNumSetListener
                        {
                            override fun valueSetCompleted(number: Int)
                            {
                                selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop = number
                                saveAndRefresh()
                            }
                        })
                        .show(parentFragmentManager, null)
            }
            R.id.btn_y_speed_when_spraying -> // 喷雾时Y轴速度
            {
                DialogFragment_Params_Setting_Number.newInstance()
                        .setDialogParams(getString(R.string.method_params_y_speed_when_spraying), 2, selectedMethodParams.paramsSlideMode.ySpeedWhenSpray, 1, 10, object : ParamsNumSetListener
                        {
                            override fun valueSetCompleted(number: Int)
                            {
                                selectedMethodParams.paramsSlideMode.ySpeedWhenSpray = number
                                saveAndRefresh()
                            }
                        })
                        .show(parentFragmentManager, null)
            }
            R.id.btn_spray_liquid_addition_interval -> // 喷雾加液间隔
            {
                DialogFragment_Params_Setting_Number.newInstance()
                        .setDialogParams(getString(R.string.method_params_spray_liquid_addition_interval), 2, selectedMethodParams.paramsSlideMode.sprayLiquidAdditionInterval, 1, 64, object : ParamsNumSetListener
                        {
                            override fun valueSetCompleted(number: Int)
                            {
                                selectedMethodParams.paramsSlideMode.sprayLiquidAdditionInterval = number
                                saveAndRefresh()
                            }
                        })
                        .show(parentFragmentManager, null)
            }
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
                        .show(parentFragmentManager, null)
            }
        }
    }
}
