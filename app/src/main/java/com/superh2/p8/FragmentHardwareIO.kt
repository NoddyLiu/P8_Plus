package com.superh2.p8

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper
import com.superh2.library.utils.ParamsHelper.paramPosOther
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.databinding.FragmentHardwareIoBinding
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen
import kotlin.concurrent.thread


/**
 *@Description 硬件测试界面
 *@Author  Noddy
 */
class FragmentHardwareIO : FragmentBase<FragmentHardwareIoBinding>(FragmentHardwareIoBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentHardwareIO()
    }

    override fun onResume()
    {
        super.onResume()

        // 设置固定液速度
        CmdHelper.fixativePressure(selectedMethodParams.paramsSlideMode.fixativeDispensePressure, false)

        // 缩回防漏挡板，避免误撞
        CmdHelper.ma(ParamsHelper.paramPosTips.plateRetractedPos, true)
    }

    override fun initWidget()
    {
        // 软键盘
        //        val mCustomKeyboard = find<FloatingKeyboardViewNotDragable>(R.id.keyboardview)
        //        mCustomKeyboard.setKeyboard(R.xml.keyboard_number) // 设置键盘
        //        mCustomKeyboard.isPreviewEnabled = false

        // 注册控件
        //        mCustomKeyboard.registerEditText(et_spray_time)
        //        mCustomKeyboard.registerEditText(et_fixative_volume)
        //        mCustomKeyboard.registerEditText(et_humid_blow)

        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        val btnNext = mActivity?.findViewById(R.id.btnFunctionRight) as Button
        btnNext.visibility = View.GONE

        binding.btnSprayTime.setOnClickListener(this)
        binding.btnSprayLiquidAddition.setOnClickListener(this)
        binding.btnFixativeVolume.setOnClickListener(this)
        binding.btnFixativeLiquidSpitOut.setOnClickListener(this)
        //        binding.btnHumidBlowOpen.setOnClickListener(this)
        //        binding.btnHumidBlowClose.setOnClickListener(this)

        binding.etReplacePosX.setText(paramPosOther.replaceSealingRingPos.x.toString())
        binding.etReplacePosY.setText(paramPosOther.replaceSealingRingPos.y.toString())
        binding.etReplacePosZ.setText(paramPosOther.replaceSealingRingHeight.toString())
        binding.etReplacePosX.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                var value = 0.0
                if (binding.etReplacePosX.text.toString().isNotEmpty()) value = binding.etReplacePosX.text.toString().toDouble()
                paramPosOther.replaceSealingRingPos.x = value
                FileUtils.saveOtherPos(paramPosOther, true)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }
        })
        binding.etReplacePosY.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                var value = 0.0
                if (binding.etReplacePosY.text.toString().isNotEmpty()) value = binding.etReplacePosY.text.toString().toDouble()
                paramPosOther.replaceSealingRingPos.y = value
                FileUtils.saveOtherPos(paramPosOther, true)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }
        })
        binding.etReplacePosZ.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                var value = 0.0
                if (binding.etReplacePosZ.text.toString().isNotEmpty()) value = binding.etReplacePosZ.text.toString().toDouble()
                paramPosOther.replaceSealingRingHeight = value
                FileUtils.saveOtherPos(paramPosOther, true)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
            }
        })
        binding.btnReplacePosXY.setOnClickListener(this)
        binding.btnReplacePosZ.setOnClickListener(this)
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)
        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMain.newInstance(), "FragmentMain")
            R.id.btn_spray_time ->
            {
                val sec = binding.etSprayTime.text.toString()
                if (!sec.isNullOrEmpty())
                {
                    thread {
                        CmdHelper.toSprayTestPos(false)
                        CmdHelper.sprayMsec((sec.toFloat() * 10).toInt(), isSeparate = false, isAsync = false)
                    }
                }
            }
            R.id.btn_spray_liquid_addition ->
            {
                CmdHelper.sprayAddLiquid(binding.etSprayLiquidAddition.text.toString().toInt(), true)
            }
            R.id.btn_fixative_volume ->
            {
                val vol = binding.etFixativeVolume.text.toString()
                if (!vol.isNullOrEmpty())
                {
                    thread {
                        CmdHelper.toFixativeTestPos(false)
                        CmdHelper.ns(vol.toInt(), false)
                    }
                }
            }
            R.id.btn_fixative_liquid_spit_out ->
            {
                thread {
                    CmdHelper.toFixativeTestPos(false)
                    CmdHelper.ns(200, false)
                }
            }
            //            R.id.btn_humid_blow_open ->
            //            {
            //                val level = et_humid_blow.text.toString()
            //                if (!level.isNullOrEmpty() && level.toInt() <= 100)
            //                {
            //                    openHumidBlows(level.toInt(), EOnOff.On)
            //                }
            //            }
            //            R.id.btn_humid_blow_close ->
            //            {
            //                closeHumidBlows()
            //            }
            R.id.btn_replace_pos_x_y ->
            {
                CmdHelper.xaya(binding.etReplacePosX.text.toString().toDouble(), binding.etReplacePosY.text.toString().toDouble(), true)
            }
            R.id.btn_replace_pos_z ->
            {
                CmdHelper.za(binding.etReplacePosZ.text.toString().toDouble(), true)
            }
        }
    }
}
