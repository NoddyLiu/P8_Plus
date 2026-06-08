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
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import com.superh2.p8.databinding.FragmentMaintenanceGeneralParametersBinding
import com.superh2.p8.dialogs.DialogFragment_Dispersancy_Index
import com.superh2.p8.dialogs.DialogFragment_Info_Prompt
import com.superh2.p8.popup.PopupChoice
import com.superh2.p8.utils.ViewUtils.fullScreen

/**
 *@Description 工程师界面1（通用参数设置界面1）
 *@Author  Noddy
 */
class FragmentMaintenanceGeneralParameters : FragmentBase<FragmentMaintenanceGeneralParametersBinding>(FragmentMaintenanceGeneralParametersBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceGeneralParameters()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.btnRemoteMaintenance.setOnClickListener(this)
        binding.btnReset.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        binding.switchbtnTubeBaseManualCheck.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.tubeBaseManualCheck = EOnOff.On
            else
                paramGeneralParams.tubeBaseManualCheck = EOnOff.Off
            saveAndRefreshSwitches()
        }

        binding.switchbtnRunImmediatelyAfterScanned.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.runImmAfterScanned = EOnOff.On
            else
                paramGeneralParams.runImmAfterScanned = EOnOff.Off
            saveAndRefreshSwitches()
        }

        binding.switchbtnAlarmTipCheck.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.alarmTipCheck = EOnOff.On
            else
                paramGeneralParams.alarmTipCheck = EOnOff.Off
            saveAndRefreshSwitches()
        }

        binding.switchbtnAlarmDoor.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.alarmDoor = EOnOff.On
            else
                paramGeneralParams.alarmDoor = EOnOff.Off
            saveAndRefreshSwitches()
        }

        binding.btnDispersancyIndex1.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex2.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex3.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex4.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex5.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex6.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex7.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex8.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex9.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex10.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex11.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex12.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex13.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex14.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex15.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex16.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex17.setOnClickListener(dispersancyIndexOnClickListener)
        binding.btnDispersancyIndex18.setOnClickListener(dispersancyIndexOnClickListener)

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.etAdditionalAirInTipBeforeAbsorb.setText(paramGeneralParams.additionalAirInTipBeforeAbsorb.toString())
        binding.etAdditionalAirInTipAfterAbsorb.setText(paramGeneralParams.additionalAirInTip.toString())
        binding.etAdditionalAirInTipAfterDispense.setText(paramGeneralParams.additionalAirInTipAfterDispense.toString())
        binding.etNumberOfSampleStir.setText(paramGeneralParams.numberOfSampleStir.toString())
        binding.etVolumnOfSampleStir.setText(paramGeneralParams.volumnOfSampleStir.toString())
        binding.etSpeedOfSampleStir.setText(paramGeneralParams.speedOfSampleStir.toString())

        refreshSwitches()

        binding.etPreSprayTime.setText(paramGeneralParams.preSprayTime.toString())
        binding.etPreSprayLiquidAddition.setText(paramGeneralParams.preSprayLiquidAddition.toString())
//        binding.etPreSprayLiquidAdditionReverse.setText(paramGeneralParams.preSprayLiquidAdditionReverse.toString())
        binding.etSprayLiquidAdditionInitSpeed.setText(paramGeneralParams.sprayLiquidAdditionSpeedInit.toString())
        binding.etSprayLiquidAdditionTargetSpeed.setText(paramGeneralParams.sprayLiquidAdditionSpeedTarget.toString())
        binding.etSprayLiquidAdditionAcceleratedSpeed.setText(paramGeneralParams.sprayLiquidAdditionSpeedAccelerated.toString())

        binding.etDispenseSpeedMaxInitSpeed.setText(paramGeneralParams.dispenseSpeedMaxInit.toString())
        binding.etDispenseSpeedMaxTargetSpeed.setText(paramGeneralParams.dispenseSpeedMaxTarget.toString())
        binding.etDispenseSpeedMaxAcceleratedSpeed.setText(paramGeneralParams.dispenseSpeedMaxAccelerated.toString())

        binding.etYSpeedMaxWhenSprayingInitSpeed.setText(paramGeneralParams.ySpeedMaxWhenSprayingInit.toString())
        binding.etYSpeedMaxWhenSprayingTargetSpeed.setText(paramGeneralParams.ySpeedMaxWhenSprayingTarget.toString())
        binding.etYSpeedMaxWhenSprayingAcceleratedSpeed.setText(paramGeneralParams.ySpeedMaxWhenSprayingAccelerated.toString())

        binding.etQrCodeScanLength.setText(paramGeneralParams.qrCodeScanLength.toString())

        binding.etSpeedAirFlow.setText(4.toString())

        binding.etHumitureStabilityThreshold.setText(paramGeneralParams.humitureStabilityThreshold.toString())
    }

    private fun refreshSwitches()
    {
        binding.switchbtnTubeBaseManualCheck.isChecked = paramGeneralParams.tubeBaseManualCheck != EOnOff.Off
        binding.switchbtnRunImmediatelyAfterScanned.isChecked = paramGeneralParams.runImmAfterScanned != EOnOff.Off
        binding.switchbtnAlarmTipCheck.isChecked = paramGeneralParams.alarmTipCheck == EOnOff.On
        binding.switchbtnAlarmDoor.isChecked = paramGeneralParams.alarmDoor == EOnOff.On
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

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
                }).show(parentFragmentManager, null)

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
                paramGeneralParams.additionalAirInTipBeforeAbsorb = if (binding.etAdditionalAirInTipBeforeAbsorb.text.toString().isNullOrEmpty()) 0 else binding.etAdditionalAirInTipBeforeAbsorb.text.toString().toInt()
                paramGeneralParams.additionalAirInTip = if (binding.etAdditionalAirInTipAfterAbsorb.text.toString().isNullOrEmpty()) 0 else binding.etAdditionalAirInTipAfterAbsorb.text.toString().toInt()
                paramGeneralParams.additionalAirInTipAfterDispense = if (binding.etAdditionalAirInTipAfterDispense.text.toString().isNullOrEmpty()) 0 else binding.etAdditionalAirInTipAfterDispense.text.toString().toInt()
                paramGeneralParams.numberOfSampleStir = if (binding.etNumberOfSampleStir.text.toString().isNullOrEmpty()) 0 else binding.etNumberOfSampleStir.text.toString().toInt()
                paramGeneralParams.volumnOfSampleStir = if (binding.etVolumnOfSampleStir.text.toString().isNullOrEmpty()) 0 else binding.etVolumnOfSampleStir.text.toString().toInt()
                paramGeneralParams.speedOfSampleStir = if (binding.etSpeedOfSampleStir.text.toString().isNullOrEmpty()) 0 else binding.etSpeedOfSampleStir.text.toString().toInt()
                paramGeneralParams.preSprayTime = if (binding.etPreSprayTime.text.toString().isNullOrEmpty()) 0 else binding.etPreSprayTime.text.toString().toInt()
                paramGeneralParams.preSprayLiquidAddition = if (binding.etPreSprayLiquidAddition.text.toString().isNullOrEmpty()) 0 else binding.etPreSprayLiquidAddition.text.toString().toInt()
//                paramGeneralParams.preSprayLiquidAdditionReverse = if(binding.etPreSprayLiquidAdditionReverse.text.toString().isNullOrEmpty()) 0 else binding.etPreSprayLiquidAdditionReverse.text.toString().toInt()
                paramGeneralParams.sprayLiquidAdditionSpeedInit = if (binding.etSprayLiquidAdditionInitSpeed.text.toString().isNullOrEmpty()) 0 else binding.etSprayLiquidAdditionInitSpeed.text.toString().toInt()
                paramGeneralParams.sprayLiquidAdditionSpeedTarget = if (binding.etSprayLiquidAdditionTargetSpeed.text.toString().isNullOrEmpty()) 0 else binding.etSprayLiquidAdditionTargetSpeed.text.toString().toInt()
                paramGeneralParams.sprayLiquidAdditionSpeedAccelerated = if (binding.etSprayLiquidAdditionAcceleratedSpeed.text.toString().isNullOrEmpty()) 0 else binding.etSprayLiquidAdditionAcceleratedSpeed.text.toString().toInt()
                paramGeneralParams.dispenseSpeedMaxInit = if (binding.etDispenseSpeedMaxInitSpeed.text.toString().isNullOrEmpty()) 0 else binding.etDispenseSpeedMaxInitSpeed.text.toString().toInt()
                paramGeneralParams.dispenseSpeedMaxTarget = if (binding.etDispenseSpeedMaxTargetSpeed.text.toString().isNullOrEmpty()) 0 else binding.etDispenseSpeedMaxTargetSpeed.text.toString().toInt()
                paramGeneralParams.dispenseSpeedMaxAccelerated = if (binding.etDispenseSpeedMaxAcceleratedSpeed.text.toString().isNullOrEmpty()) 0 else binding.etDispenseSpeedMaxAcceleratedSpeed.text.toString().toInt()
                paramGeneralParams.ySpeedMaxWhenSprayingInit = if (binding.etYSpeedMaxWhenSprayingInitSpeed.text.toString().isNullOrEmpty()) 0 else binding.etYSpeedMaxWhenSprayingInitSpeed.text.toString().toInt()
                paramGeneralParams.ySpeedMaxWhenSprayingTarget = if (binding.etYSpeedMaxWhenSprayingTargetSpeed.text.toString().isNullOrEmpty()) 0 else binding.etYSpeedMaxWhenSprayingTargetSpeed.text.toString().toInt()
                paramGeneralParams.ySpeedMaxWhenSprayingAccelerated = if (binding.etYSpeedMaxWhenSprayingAcceleratedSpeed.text.toString().isNullOrEmpty()) 0 else binding.etYSpeedMaxWhenSprayingAcceleratedSpeed.text.toString().toInt()
                paramGeneralParams.qrCodeScanLength = if (binding.etQrCodeScanLength.text.toString().isNullOrEmpty()) 20 else binding.etQrCodeScanLength.text.toString().toInt()
                paramGeneralParams.humitureStabilityThreshold = if (binding.etHumitureStabilityThreshold.text.toString().isNullOrEmpty()) 0.5f else binding.etHumitureStabilityThreshold.text.toString().toFloat()
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

                            fullScreen(activity)
                        }
                    }

                    override fun cancel()
                    {
                        fullScreen(activity)
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
                fullScreen(activity)
            }

            override fun cancel()
            {
                fullScreen(activity)
            }
        }).show(parentFragmentManager, null)
    }

    private fun saveAndRefreshSwitches()
    {
        FileUtils.saveGeneralParameters(paramGeneralParams, true)
        refreshSwitches()
    }
}
