package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.superh2.library.myEnum.EAngle
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myEnum.ESpreadingMode
import com.superh2.library.myInterface.ParamsDecimalSetListener
import com.superh2.library.myInterface.ParamsNumSetListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Decimal
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Number
import kotlinx.android.synthetic.main.fragment_method_parameters.*
import kotlinx.android.synthetic.main.fragment_method_parameters.btn_delay_time_after_sample_dropping
import kotlinx.android.synthetic.main.fragment_method_parameters.btn_delay_time_before_sample_dropping
import kotlinx.android.synthetic.main.fragment_method_parameters.btn_fixative_volumn_per_drop
import kotlinx.android.synthetic.main.fragment_method_parameters.btn_spray_liquid_addition_interval
import kotlinx.android.synthetic.main.fragment_method_parameters.btn_y_speed_when_spraying

/**
 *@Description 方法参数设置界面首页
 *@Author  Noddy
 */
class FragmentMethodParameters : FragmentMethodParametersBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMethodParameters()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_method_parameters, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        rg_spreading_mode.setOnCheckedChangeListener { group, checkedId ->
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

        switchbtn_barcode.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
            {
                selectedMethodParams.barcode = EOnOff.On
                layout_tube_quantity.visibility = View.INVISIBLE
            }
            else
            {
                selectedMethodParams.barcode = EOnOff.Off
                layout_tube_quantity.visibility = View.VISIBLE
            }
            saveAndRefresh()
        }

        switchbtn_slide_automatic.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked) selectedMethodParams.slideAutomatic = EOnOff.On
            else selectedMethodParams.slideAutomatic = EOnOff.Off
            saveAndRefresh()
        }

        rg_angle_of_slide.setOnCheckedChangeListener { group, checkedId ->
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

        btn_slide_quantity.setOnClickListener(this)
        btn_tube_quantity.setOnClickListener(this)
        btn_slide_per_tube.setOnClickListener(this)
        btn_spot_per_slide.setOnClickListener(this)

        btn_fixative_volumn_before_spray.setOnClickListener(this)
        btn_delay_time_after_spray.setOnClickListener(this)
        btn_fixative_volumn_after_spray.setOnClickListener(this)
        btn_fixative_dispense_pressure.setOnClickListener(this)
        btn_volumn_per_drop.setOnClickListener(this)
        btn_dispense_height.setOnClickListener(this)
        btn_dispense_speed.setOnClickListener(this)
        btn_next.setOnClickListener(this)

        btn_delay_time_before_sample_dropping.setOnClickListener(this)
        btn_delay_time_after_sample_dropping.setOnClickListener(this)
        btn_fixative_volumn_per_drop.setOnClickListener(this)
        btn_y_speed_when_spraying.setOnClickListener(this)
        btn_spray_liquid_addition_interval.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        if (selectedMethodParams.spreadingMode == ESpreadingMode.Dry)
            rbtn_dry_slide.isChecked = true
        else
            rbtn_wet_slide.isChecked = true

        switchbtn_barcode.isChecked = selectedMethodParams.barcode != EOnOff.Off

        if(selectedMethodParams.barcode == EOnOff.On)
        {
            layout_slide_quantity.visibility = View.VISIBLE
            tv_slide_quantity.text  = selectedMethodParams.slideQuantity.toString()
            layout_tube_quantity.visibility = View.INVISIBLE
        }
        else
        {
            layout_slide_quantity.visibility = View.INVISIBLE
            layout_tube_quantity.visibility = View.VISIBLE
            tv_tube_quantity.text = selectedMethodParams.tubeQuantity.toString()
        }

        switchbtn_slide_automatic.isChecked = selectedMethodParams.slideAutomatic == EOnOff.On

        tv_slide_per_tube.text = selectedMethodParams.slidePerTube.toString()
        tv_spot_per_slide.text = selectedMethodParams.spotPerSlide.toString()

        when (selectedMethodParams.angleOfSlide)
        {
            EAngle.Zero -> rbtn_angle_of_slide_zero.isChecked = true
            EAngle.Five -> rbtn_angle_of_slide_5.isChecked = true
            EAngle.Ten -> rbtn_angle_of_slide_10.isChecked = true
        }

        tv_fixative_volumn_before_spray.text = selectedMethodParams.paramsSlideMode.fixativeVolumnBeforeSpray.toString()
        tv_delay_time_after_spray.text = selectedMethodParams.paramsSlideMode.delayTimeAfterSpray.toString()
        tv_fixative_volumn_after_spray.text = selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray.toString()
        tv_fixative_dispense_pressure.text = selectedMethodParams.paramsSlideMode.fixativeDispensePressure.toString()
        tv_volumn_per_drop.text = selectedMethodParams.paramsSlideMode.volumnPerDrop.toString()
        tv_dispense_height.text = selectedMethodParams.paramsSlideMode.dispenseHeight.toString()
        tv_dispense_speed.text = selectedMethodParams.paramsSlideMode.dispenseSpeed.toString()

        tv_delay_time_before_sample_dropping.text =   selectedMethodParams.paramsSlideMode.delayTimeBeforeSampleDropping.toString()
        tv_delay_time_after_sample_dropping.text = selectedMethodParams.paramsSlideMode.delayTimeAfterSampleDropping.toString()
        tv_fixative_volumn_per_drop.text =
            selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop.toString()
        tv_y_speed_when_spraying.text = selectedMethodParams.paramsSlideMode.ySpeedWhenSpray.toString()
        tv_spray_liquid_addition_interval.text = selectedMethodParams.paramsSlideMode.sprayLiquidAdditionInterval.toString()
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
            R.id.btn_next -> replaceFragment(FragmentMethodParametersMore.newInstance(), "FragmentMethodParametersMore")
            R.id.btn_slide_quantity -> // 玻片数量
            {
                DialogFragment_Params_Setting_Number.newInstance().setDialogParams(getString(R.string.method_params_slide_quantity), 2, selectedMethodParams.slideQuantity, 1, 64, object : ParamsNumSetListener
                {
                    override fun valueSetCompleted(number: Int)
                    {
                        selectedMethodParams.slideQuantity = number
                        saveAndRefresh()
                    }
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                }).show(fragmentManager, null)
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
                        .show(fragmentManager, null)
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
                        .show(fragmentManager, null)
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
                        .show(fragmentManager, null)
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
                        .show(fragmentManager, null)
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
                        .show(fragmentManager, null)
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
                        .show(fragmentManager, null)
            }
        }
    }
}
