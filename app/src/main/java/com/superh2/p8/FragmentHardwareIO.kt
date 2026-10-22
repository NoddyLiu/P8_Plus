package com.superh2.p8

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper
import com.superh2.library.utils.ParamsHelper.paramPosOther
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.databinding.FragmentHardwareIoBinding
import com.superh2.p8.serial.PipetteCompat
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
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        val btnNext = mActivity?.findViewById(R.id.btnFunctionRight) as Button
        btnNext.visibility = View.GONE

        binding.btnSprayTime.setOnClickListener(this)
        binding.btnSprayLiquidAddition.setOnClickListener(this)
        binding.btnFixativeVolume.setOnClickListener(this)
        binding.btnFixativeLiquidSpitOut.setOnClickListener(this)

        binding.btnAdpAspirate.setOnClickListener(this)
        binding.btnAdpDispense.setOnClickListener(this)
        binding.btnAdpStir.setOnClickListener(this)
        binding.btnAdpReleaseTip.setOnClickListener(this)
        binding.btnAdpCheckTip.setOnClickListener(this)
        binding.btnAdpInit.setOnClickListener(this)

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

            R.id.btn_adp_aspirate -> runAdpVolumeCommand(binding.etAdpAspirateVolume.text.toString(), getString(R.string.aspirate)) {
                PipetteCompat.driver.aspirate(it)
            }

            R.id.btn_adp_dispense -> runAdpVolumeCommand(binding.etAdpDispenseVolume.text.toString(), getString(R.string.dispense)) {
                PipetteCompat.driver.dispense(it)
            }

            R.id.btn_adp_stir -> runAdpVolumeCommand(binding.etAdpStirVolume.text.toString(), getString(R.string.stir)) {
                PipetteCompat.driver.stir(it, binding.etAdpStirTimes.text.toString().toIntOrNull() ?: 1)
            }

            R.id.btn_adp_release_tip -> thread { PipetteCompat.driver.releaseTip() }
            R.id.btn_adp_check_tip ->
            {
                val hostActivity = requireActivity()
                thread {
                    val hasTip = PipetteCompat.driver.hasTip()
                    hostActivity.runOnUiThread {
                        val message = if (hasTip) R.string.tip_detected else R.string.tip_not_detected
                        binding.tvAdpCheckTipResult.text = getString(message)
                    }
                }
            }
            R.id.btn_adp_init -> thread { PipetteCompat.driver.init() }

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

    private fun runAdpVolumeCommand(value: String, label: String, command: (Int) -> Unit)
    {
        val volume = value.toIntOrNull()
        if (volume == null || volume < 0)
        {
            Toast.makeText(requireContext(), getString(R.string.invalid_volume, label), Toast.LENGTH_SHORT).show()
            return
        }
        thread { command(volume) }
    }
}
