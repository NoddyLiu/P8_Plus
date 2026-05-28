package com.superh2.p8


import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.superh2.library.myEntityJson.Humiture
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myInterface.IDispersancyIndexCallback
import com.superh2.library.myInterface.InfoPromptListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import com.superh2.p8.dialogs.DialogFragment_Dispersancy_Index
import com.superh2.p8.dialogs.DialogFragment_Info_Prompt
import kotlinx.android.synthetic.main.fragment_maintenance_general_parameters_more.*
import kotlinx.android.synthetic.main.fragment_method_parameters_container.*


/**
 *@Description 工程师界面2（通用参数设置界面2）（暂时停用，移植到FragmentMaintenanceGeneralParameters上）
 *@Author  Noddy
 */
class FragmentMaintenanceGeneralParametersMore : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceGeneralParametersMore()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_general_parameters_more, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)
        btn_prev.setOnClickListener(this)

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

        switchbtn_alarm_door.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked)
                paramGeneralParams.alarmDoor = EOnOff.On
            else
                paramGeneralParams.alarmDoor = EOnOff.Off
            saveAndRefreshSwitches()
        }

        btn_remote_maintenance.setOnClickListener(this)
        btn_reset.setOnClickListener(this)
        btn_save.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        et_speed_air_flow.setText(4.toString())

        refreshSwitches()
    }

    private fun refreshSwitches()
    {
        switchbtn_alarm_door.isChecked = paramGeneralParams.alarmDoor == EOnOff.On
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_prev -> replaceFragment(FragmentMaintenanceGeneralParameters.newInstance(), "FragmentMaintenanceGeneralParameters")
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
                if (FileUtils.saveGeneralParameters(paramGeneralParams, true)) Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
            R.id.btn_reset ->
            {
                //                alert{
                //                    customView {
                //                        iconResource = android.R.drawable.ic_dialog_alert
                //                        titleResource = R.string.prompt
                //                        var tv = textView(getString(R.string.question_makesure_reset))
                //                        tv.textSize = 35f
                //                        tv.leftPadding = 50
                //                        tv.topPadding = 25
                //                        tv.bottomPadding = 25
                //                        positiveButton(getString(R.string.yes))
                //                        {
                //                            val generalParams = GeneralParams()
                //                            paramGeneralParams.additionalAirInTipBeforeAbsorb = generalParams.additionalAirInTipBeforeAbsorb
                //                            paramGeneralParams.additionalAirInTip = generalParams.additionalAirInTip
                //                            paramGeneralParams.additionalAirInTipAfterDispense = generalParams.additionalAirInTipAfterDispense
                //                            paramGeneralParams.numberOfSampleStir = generalParams.numberOfSampleStir
                //                            paramGeneralParams.volumnOfSampleStir = generalParams.volumnOfSampleStir
                //                            paramGeneralParams.speedOfSampleStir = generalParams.speedOfSampleStir
                //                            paramGeneralParams.preSprayTime = generalParams.preSprayTime
                //                            paramGeneralParams.preSprayLiquidAddition = generalParams.preSprayLiquidAddition
                //                            paramGeneralParams.preSprayLiquidAdditionReverse = generalParams.preSprayLiquidAdditionReverse
                //                            paramGeneralParams.sprayLiquidAdditionSpeedInit = generalParams.sprayLiquidAdditionSpeedInit
                //                            paramGeneralParams.sprayLiquidAdditionSpeedTarget = generalParams.sprayLiquidAdditionSpeedTarget
                //                            paramGeneralParams.sprayLiquidAdditionSpeedAccelerated = generalParams.sprayLiquidAdditionSpeedAccelerated
                //                            paramGeneralParams.dispenseSpeedMaxInit = generalParams.dispenseSpeedMaxInit
                //                            paramGeneralParams.dispenseSpeedMaxTarget = generalParams.dispenseSpeedMaxTarget
                //                            paramGeneralParams.dispenseSpeedMaxAccelerated = generalParams.dispenseSpeedMaxAccelerated
                //                            paramGeneralParams.ySpeedMaxWhenSprayingInit =generalParams.ySpeedMaxWhenSprayingInit
                //                            paramGeneralParams.ySpeedMaxWhenSprayingTarget = generalParams.ySpeedMaxWhenSprayingTarget
                //                            paramGeneralParams.ySpeedMaxWhenSprayingAccelerated = generalParams.ySpeedMaxWhenSprayingAccelerated
                //                            paramGeneralParams.qrCodeScanLength = generalParams.qrCodeScanLength
                //
                //                            if (FileUtils.saveGeneralParameters(paramGeneralParams, true))
                //                            {
                //                                toast(getString(R.string.info_reset_successfully))
                //                                refreshControls()
                //
                //                                fullScreen()
                //                            }
                //                        }
                //                        negativeButton(getString(R.string.no)) { fullScreen() }
                //                        isCancelable = false
                //                    }
                //                }.show()
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
