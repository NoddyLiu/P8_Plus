package com.superh2.p8


import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.superh2.library.myEntityJson.GeneralParams
import com.superh2.library.myEntityJson.Humiture
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myInterface.IDispersancyIndexCallback
import com.superh2.library.myInterface.ISelectedListener
import com.superh2.library.myInterface.InfoPromptListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import com.superh2.p8.dialogs.DialogFragment_Dispersancy_Index
import com.superh2.p8.dialogs.DialogFragment_Info_Prompt
import com.superh2.p8.popup.PopupChoice
import com.superh2.p8.utils.CmdHelper
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.*
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_1
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_10
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_11
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_12
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_13
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_14
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_15
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_16
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_17
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_18
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_2
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_3
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_4
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_5
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_6
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_7
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_8
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_dispersancy_index_9
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_remote_maintenance
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_reset
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.btn_save
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.et_speed_air_flow
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters.switchbtn_alarm_door

/**
 *@Description 工程师界面1（通用参数设置界面1）
 *@Author  Noddy
 */
class FragmentMaintenanceGeneralParameters : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceGeneralParameters()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_general_parameters, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        btn_remote_maintenance.setOnClickListener(this)
        btn_reset.setOnClickListener(this)
        btn_save.setOnClickListener(this)

        switchbtn_tube_base_manual_check.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.tubeBaseManualCheck = EOnOff.On
            else
                paramGeneralParams.tubeBaseManualCheck = EOnOff.Off
            saveAndRefreshSwitches()
        }

        switchbtn_run_immediately_after_scanned.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.runImmAfterScanned = EOnOff.On
            else
                paramGeneralParams.runImmAfterScanned = EOnOff.Off
            saveAndRefreshSwitches()
        }

        switchbtn_alarm_tip_check.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.alarmTipCheck = EOnOff.On
            else
                paramGeneralParams.alarmTipCheck = EOnOff.Off
            saveAndRefreshSwitches()
        }

        switchbtn_alarm_door.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.alarmDoor = EOnOff.On
            else
                paramGeneralParams.alarmDoor = EOnOff.Off
            saveAndRefreshSwitches()
        }

        btn_dispersancy_index_1.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_2.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_3.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_4.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_5.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_6.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_7.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_8.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_9.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_10.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_11.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_12.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_13.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_14.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_15.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_16.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_17.setOnClickListener(dispersancyIndexOnClickListener)
        btn_dispersancy_index_18.setOnClickListener(dispersancyIndexOnClickListener)

        refreshControls()
    }

    private fun refreshControls()
    {
        et_additional_air_in_tip_before_absorb.setText(paramGeneralParams.additionalAirInTipBeforeAbsorb.toString())
        et_additional_air_in_tip_after_absorb.setText(paramGeneralParams.additionalAirInTip.toString())
        et_additional_air_in_tip_after_dispense.setText(paramGeneralParams.additionalAirInTipAfterDispense.toString())
        et_number_of_sample_stir.setText(paramGeneralParams.numberOfSampleStir.toString())
        et_volumn_of_sample_stir.setText(paramGeneralParams.volumnOfSampleStir.toString())
        et_speed_of_sample_stir.setText(paramGeneralParams.speedOfSampleStir.toString())

        refreshSwitches()

        et_pre_spray_time.setText(paramGeneralParams.preSprayTime.toString())
        et_pre_spray_liquid_addition.setText(paramGeneralParams.preSprayLiquidAddition.toString())
//        et_pre_spray_liquid_addition_reverse.setText(paramGeneralParams.preSprayLiquidAdditionReverse.toString())
        et_spray_liquid_addition_init_speed.setText(paramGeneralParams.sprayLiquidAdditionSpeedInit.toString())
        et_spray_liquid_addition_target_speed.setText(paramGeneralParams.sprayLiquidAdditionSpeedTarget.toString())
        et_spray_liquid_addition_accelerated_speed.setText(paramGeneralParams.sprayLiquidAdditionSpeedAccelerated.toString())

        et_dispense_speed_max_init_speed.setText(paramGeneralParams.dispenseSpeedMaxInit.toString())
        et_dispense_speed_max_target_speed.setText(paramGeneralParams.dispenseSpeedMaxTarget.toString())
        et_dispense_speed_max_accelerated_speed.setText(paramGeneralParams.dispenseSpeedMaxAccelerated.toString())

        et_y_speed_max_when_spraying_init_speed.setText(paramGeneralParams.ySpeedMaxWhenSprayingInit.toString())
        et_y_speed_max_when_spraying_target_speed.setText(paramGeneralParams.ySpeedMaxWhenSprayingTarget.toString())
        et_y_speed_max_when_spraying_accelerated_speed.setText(paramGeneralParams.ySpeedMaxWhenSprayingAccelerated.toString())

        et_qr_code_scan_length.setText(paramGeneralParams.qrCodeScanLength.toString())

        et_speed_air_flow.setText(4.toString())

        et_humiture_stability_threshold.setText(paramGeneralParams.humitureStabilityThreshold.toString())
    }

    private fun refreshSwitches()
    {
        switchbtn_tube_base_manual_check.isChecked = paramGeneralParams.tubeBaseManualCheck != EOnOff.Off
        switchbtn_run_immediately_after_scanned.isChecked = paramGeneralParams.runImmAfterScanned != EOnOff.Off
        switchbtn_alarm_tip_check.isChecked = paramGeneralParams.alarmTipCheck == EOnOff.On
        switchbtn_alarm_door.isChecked = paramGeneralParams.alarmDoor == EOnOff.On
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_next -> replaceFragment(FragmentMaintenanceGeneralParametersMore.newInstance(), "FragmentMaintenanceGeneralParametersMore")
            R.id.btn_remote_maintenance ->
            {
                // 提示远程维护中
                var infoPrompt = DialogFragment_Info_Prompt.newInstance()
                infoPrompt.setDialogContent(null, getString(R.string.info_remote_maintenance), null, 0, 0, object : InfoPromptListener
                {
                    override fun clicked()
                    {

                    }
                }).show(fragmentManager, null)

                val timer = object : CountDownTimer(5000, 1000)
                {
                    override fun onTick(millisUntilFinished: Long)
                    {
                    }

                    override fun onFinish()
                    {
                        infoPrompt.dismissDialog()
                    }
                }
                timer.start()
            }
            R.id.btn_save ->
            {
                paramGeneralParams.additionalAirInTipBeforeAbsorb = if (et_additional_air_in_tip_before_absorb.text.toString().isNullOrEmpty()) 0 else et_additional_air_in_tip_before_absorb.text.toString().toInt()
                paramGeneralParams.additionalAirInTip = if (et_additional_air_in_tip_after_absorb.text.toString().isNullOrEmpty()) 0 else et_additional_air_in_tip_after_absorb.text.toString().toInt()
                paramGeneralParams.additionalAirInTipAfterDispense = if (et_additional_air_in_tip_after_dispense.text.toString().isNullOrEmpty()) 0 else et_additional_air_in_tip_after_dispense.text.toString().toInt()
                paramGeneralParams.numberOfSampleStir = if (et_number_of_sample_stir.text.toString().isNullOrEmpty()) 0 else et_number_of_sample_stir.text.toString().toInt()
                paramGeneralParams.volumnOfSampleStir = if (et_volumn_of_sample_stir.text.toString().isNullOrEmpty()) 0 else et_volumn_of_sample_stir.text.toString().toInt()
                paramGeneralParams.speedOfSampleStir = if (et_speed_of_sample_stir.text.toString().isNullOrEmpty()) 0 else et_speed_of_sample_stir.text.toString().toInt()
                paramGeneralParams.preSprayTime = if (et_pre_spray_time.text.toString().isNullOrEmpty()) 0 else et_pre_spray_time.text.toString().toInt()
                paramGeneralParams.preSprayLiquidAddition = if (et_pre_spray_liquid_addition.text.toString().isNullOrEmpty()) 0 else et_pre_spray_liquid_addition.text.toString().toInt()
//                paramGeneralParams.preSprayLiquidAdditionReverse = if(et_pre_spray_liquid_addition_reverse.text.toString().isNullOrEmpty()) 0 else et_pre_spray_liquid_addition_reverse.text.toString().toInt()
                paramGeneralParams.sprayLiquidAdditionSpeedInit = if (et_spray_liquid_addition_init_speed.text.toString().isNullOrEmpty()) 0 else et_spray_liquid_addition_init_speed.text.toString().toInt()
                paramGeneralParams.sprayLiquidAdditionSpeedTarget = if (et_spray_liquid_addition_target_speed.text.toString().isNullOrEmpty()) 0 else et_spray_liquid_addition_target_speed.text.toString().toInt()
                paramGeneralParams.sprayLiquidAdditionSpeedAccelerated = if (et_spray_liquid_addition_accelerated_speed.text.toString().isNullOrEmpty()) 0 else et_spray_liquid_addition_accelerated_speed.text.toString().toInt()
                paramGeneralParams.dispenseSpeedMaxInit = if (et_dispense_speed_max_init_speed.text.toString().isNullOrEmpty()) 0 else et_dispense_speed_max_init_speed.text.toString().toInt()
                paramGeneralParams.dispenseSpeedMaxTarget = if (et_dispense_speed_max_target_speed.text.toString().isNullOrEmpty()) 0 else et_dispense_speed_max_target_speed.text.toString().toInt()
                paramGeneralParams.dispenseSpeedMaxAccelerated = if (et_dispense_speed_max_accelerated_speed.text.toString().isNullOrEmpty()) 0 else et_dispense_speed_max_accelerated_speed.text.toString().toInt()
                paramGeneralParams.ySpeedMaxWhenSprayingInit = if (et_y_speed_max_when_spraying_init_speed.text.toString().isNullOrEmpty()) 0 else et_y_speed_max_when_spraying_init_speed.text.toString().toInt()
                paramGeneralParams.ySpeedMaxWhenSprayingTarget = if (et_y_speed_max_when_spraying_target_speed.text.toString().isNullOrEmpty()) 0 else et_y_speed_max_when_spraying_target_speed.text.toString().toInt()
                paramGeneralParams.ySpeedMaxWhenSprayingAccelerated = if (et_y_speed_max_when_spraying_accelerated_speed.text.toString().isNullOrEmpty()) 0 else et_y_speed_max_when_spraying_accelerated_speed.text.toString().toInt()
                paramGeneralParams.qrCodeScanLength = if (et_qr_code_scan_length.text.toString().isNullOrEmpty()) 20 else et_qr_code_scan_length.text.toString().toInt()
                paramGeneralParams.humitureStabilityThreshold = if (et_humiture_stability_threshold.text.toString().isNullOrEmpty()) 0.5f else et_humiture_stability_threshold.text.toString().toFloat()
                if (FileUtils.saveGeneralParameters(paramGeneralParams, true))
                    Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
            R.id.btn_reset ->
            {
                PopupChoice(mActivity, getString(R.string.question_makesure_reset), object : ISelectedListener
                {
                    override fun confirm()
                    {
                        val generalParams = GeneralParams()
                        paramGeneralParams.additionalAirInTipBeforeAbsorb = generalParams.additionalAirInTipBeforeAbsorb
                        paramGeneralParams.additionalAirInTip = generalParams.additionalAirInTip
                        paramGeneralParams.additionalAirInTipAfterDispense = generalParams.additionalAirInTipAfterDispense
                        paramGeneralParams.numberOfSampleStir = generalParams.numberOfSampleStir
                        paramGeneralParams.volumnOfSampleStir = generalParams.volumnOfSampleStir
                        paramGeneralParams.speedOfSampleStir = generalParams.speedOfSampleStir
                        paramGeneralParams.preSprayTime = generalParams.preSprayTime
                        paramGeneralParams.preSprayLiquidAddition = generalParams.preSprayLiquidAddition
                        paramGeneralParams.preSprayLiquidAdditionReverse = generalParams.preSprayLiquidAdditionReverse
                        paramGeneralParams.sprayLiquidAdditionSpeedInit = generalParams.sprayLiquidAdditionSpeedInit
                        paramGeneralParams.sprayLiquidAdditionSpeedTarget = generalParams.sprayLiquidAdditionSpeedTarget
                        paramGeneralParams.sprayLiquidAdditionSpeedAccelerated = generalParams.sprayLiquidAdditionSpeedAccelerated
                        paramGeneralParams.dispenseSpeedMaxInit = generalParams.dispenseSpeedMaxInit
                        paramGeneralParams.dispenseSpeedMaxTarget = generalParams.dispenseSpeedMaxTarget
                        paramGeneralParams.dispenseSpeedMaxAccelerated = generalParams.dispenseSpeedMaxAccelerated
                        paramGeneralParams.ySpeedMaxWhenSprayingInit =generalParams.ySpeedMaxWhenSprayingInit
                        paramGeneralParams.ySpeedMaxWhenSprayingTarget = generalParams.ySpeedMaxWhenSprayingTarget
                        paramGeneralParams.ySpeedMaxWhenSprayingAccelerated = generalParams.ySpeedMaxWhenSprayingAccelerated
                        paramGeneralParams.qrCodeScanLength = generalParams.qrCodeScanLength
                        paramGeneralParams.humitureStabilityThreshold = generalParams.humitureStabilityThreshold

                        if (FileUtils.saveGeneralParameters(paramGeneralParams, true))
                        {
                            Toast.makeText(mActivity, getString(R.string.info_reset_successfully), Toast.LENGTH_SHORT).show()
                            refreshControls()

                            fullScreen()
                        }
                    }

                    override fun cancel()
                    {
                        fullScreen()
                    }
                }).showPopupWindow()
            }
        }
    }

    /**
     * 分散指数按钮监听器
     */
    val dispersancyIndexOnClickListener = View.OnClickListener { view ->

        var index = 0
        when (view.getId())
        {
            R.id.btn_dispersancy_index_1 -> index = 0
            R.id.btn_dispersancy_index_2 -> index = 1
            R.id.btn_dispersancy_index_3 -> index = 2
            R.id.btn_dispersancy_index_4 -> index = 3
            R.id.btn_dispersancy_index_5 -> index = 4
            R.id.btn_dispersancy_index_6 -> index = 5
            R.id.btn_dispersancy_index_7 -> index = 6
            R.id.btn_dispersancy_index_8 -> index = 7
            R.id.btn_dispersancy_index_9 -> index = 8
            R.id.btn_dispersancy_index_10 -> index = 9
            R.id.btn_dispersancy_index_11 -> index = 10
            R.id.btn_dispersancy_index_12 -> index = 11
            R.id.btn_dispersancy_index_13 -> index = 12
            R.id.btn_dispersancy_index_14 -> index = 13
            R.id.btn_dispersancy_index_15 -> index = 14
            R.id.btn_dispersancy_index_16 -> index = 15
            R.id.btn_dispersancy_index_17 -> index = 16
            R.id.btn_dispersancy_index_18 -> index = 17
        }

        DialogFragment_Dispersancy_Index.newInstance().setDialogContent(getString(R.string.general_params_dispersancy_index) + " " + (index + 1), index, getString(R.string.confirm), getString(R.string.cancel), object : IDispersancyIndexCallback
        {
            override fun confirm(humiture: Humiture)
            {
                paramGeneralParams.DispersancyIndexList[index].temp = humiture.temp
                paramGeneralParams.DispersancyIndexList[index].humi = humiture.humi
                FileUtils.saveGeneralParameters(paramGeneralParams, true)
                fullScreen()
            }

            override fun cancel()
            {
                fullScreen()
            }
        }).show(fragmentManager, null)
    }

    private fun saveAndRefreshSwitches()
    {
        FileUtils.saveGeneralParameters(paramGeneralParams, true)
        refreshSwitches()
    }
}
