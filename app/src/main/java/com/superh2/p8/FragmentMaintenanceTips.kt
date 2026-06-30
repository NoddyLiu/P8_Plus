package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.superh2.library.myEnum.EDirection
import com.superh2.library.utils.ConstantsUtils
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramPosTips
import com.superh2.p8.databinding.FragmentMaintenanceTipsBinding
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen

/**
 *@Description 工程师界面（枪头界面）
 *@Author  Noddy
 */
class FragmentMaintenanceTips : FragmentBase<FragmentMaintenanceTipsBinding>(FragmentMaintenanceTipsBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceTips()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnFirstPointX.setOnClickListener(this)
        binding.btnFirstPointY.setOnClickListener(this)
        binding.btnPreTakeTipHeight.setOnClickListener(this)
        binding.btnTakeTipHeight.setOnClickListener(this)
        binding.btnReleaseTipX.setOnClickListener(this)
        binding.btnReleaseTipY.setOnClickListener(this)
        binding.btnReleaseTipHeight.setOnClickListener(this)
        binding.btnReleaseTipOffset.setOnClickListener(this)
        binding.btnReleaseTipChannel1Y.setOnClickListener(this)
        binding.btnReleaseTipBoxPoint1.setOnClickListener(this)
        binding.btnReleaseTipBoxPoint2.setOnClickListener(this)
        binding.btnAdditionalAirHeight.setOnClickListener(this)
        binding.btnTipCheckPos.setOnClickListener(this)
        binding.btnTipCheckHeight.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.etFirstPointX.setText(paramPosTips.tips[0].x.toString())
        binding.etFirstPointY.setText(paramPosTips.tips[0].y.toString())

        binding.etPreTakeTipHeight.setText(paramPosTips.preTakeTipHeight.toString())
        binding.etTakeTipHeight.setText(paramPosTips.takeTipHeight.toString())
        binding.etStepLength.setText(paramPosTips.stepLength.toString())
        binding.etReleaseTipX.setText(paramPosTips.releaseTipPos.x.toString())
        binding.etReleaseTipY.setText(paramPosTips.releaseTipPos.y.toString())
        binding.etReleaseTipHeight.setText(paramPosTips.releaseTipHeight.toString())
        binding.etReleaseTipOffset.setText(paramPosTips.releaseTipOffset.toString())
        binding.etReleaseTipChannel1Y.setText(paramPosTips.releaseTipChannel1Y.toString())
        binding.etReleaseTipBoxPoint1X.setText(paramPosTips.releaseTipBoxPoint1X.toString())
        binding.etReleaseTipBoxPoint1Y.setText(paramPosTips.releaseTipBoxPoint1Y.toString())
        binding.etReleaseTipBoxPoint2X.setText(paramPosTips.releaseTipBoxPoint2X.toString())
        binding.etReleaseTipBoxPoint2Y.setText(paramPosTips.releaseTipBoxPoint2Y.toString())
        binding.etAdditionalAirHeight.setText(paramPosTips.additionalAirHeight.toString())
        binding.etTipCheckPosX.setText(paramPosTips.tipCheckPos.x.toString())
        binding.etTipCheckPosY.setText(paramPosTips.tipCheckPos.y.toString())
        binding.etTipCheckHeight.setText(paramPosTips.tipCheckHeight.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(binding.etFirstPointX.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(binding.etFirstPointY.text.toString().toDouble(), true)
            R.id.btn_pre_take_tip_height -> CmdHelper.za(binding.etPreTakeTipHeight.text.toString().toDouble(), true)
            R.id.btn_take_tip_height -> CmdHelper.za(binding.etTakeTipHeight.text.toString().toDouble(), true)
            R.id.btn_release_tip_x -> CmdHelper.xa(binding.etReleaseTipX.text.toString().toDouble(), true)
            R.id.btn_release_tip_y -> CmdHelper.ya(binding.etReleaseTipY.text.toString().toDouble(), true)
            R.id.btn_release_tip_height -> CmdHelper.za(binding.etReleaseTipHeight.text.toString().toDouble(), true)
            R.id.btn_release_tip_offset -> CmdHelper.za(binding.etReleaseTipHeight.text.toString().toDouble() - binding.etReleaseTipOffset.text.toString().toDouble(), true)
            R.id.btn_release_tip_channel_1_y -> CmdHelper.ya(binding.etReleaseTipChannel1Y.text.toString().toDouble(), true)
            R.id.btn_release_tip_box_point_1 -> CmdHelper.xaya(binding.etReleaseTipBoxPoint1X.text.toString().toDouble(), binding.etReleaseTipBoxPoint1Y.text.toString().toDouble(), true)
            R.id.btn_release_tip_box_point_2 -> CmdHelper.xaya(binding.etReleaseTipBoxPoint2X.text.toString().toDouble(), binding.etReleaseTipBoxPoint2Y.text.toString().toDouble(), true)
            R.id.btn_additional_air_height -> CmdHelper.za(binding.etAdditionalAirHeight.text.toString().toDouble(), true)
            R.id.btn_tip_check_pos -> CmdHelper.xaya(binding.etTipCheckPosX.text.toString().toDouble(), binding.etTipCheckPosY.text.toString().toDouble(), true)
            R.id.btn_tip_check_height -> CmdHelper.za(binding.etTipCheckHeight.text.toString().toDouble(), true)
            R.id.btn_save ->
            {
                val firstPosX = if (binding.etFirstPointX.text.toString().isNullOrEmpty()) 0.0 else binding.etFirstPointX.text.toString().toDouble()
                val firstPosY = if (binding.etFirstPointY.text.toString().isNullOrEmpty()) 0.0 else binding.etFirstPointY.text.toString().toDouble()
                val preTakeTipHeight = if (binding.etPreTakeTipHeight.text.toString().isNullOrEmpty()) 0.0 else binding.etPreTakeTipHeight.text.toString().toDouble()
                val takeTipHeight = if (binding.etTakeTipHeight.text.toString().isNullOrEmpty()) 0.0 else binding.etTakeTipHeight.text.toString().toDouble()
                val stepLength = if (binding.etStepLength.text.toString().isNullOrEmpty()) 0.0 else binding.etStepLength.text.toString().toDouble()
                val releaseTipX = if (binding.etReleaseTipX.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipX.text.toString().toDouble()
                val releaseTipY = if (binding.etReleaseTipY.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipY.text.toString().toDouble()
                val releaseTipHeight = if (binding.etReleaseTipHeight.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipHeight.text.toString().toDouble()
                val releaseTipOffset = if (binding.etReleaseTipOffset.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipOffset.text.toString().toDouble()
                val releaseTipChannel1Y = if (binding.etReleaseTipChannel1Y.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipChannel1Y.text.toString().toDouble()
                val releaseTipBoxPoint1X = if (binding.etReleaseTipBoxPoint1X.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipBoxPoint1X.text.toString().toDouble()
                val releaseTipBoxPoint1Y = if (binding.etReleaseTipBoxPoint1Y.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipBoxPoint1Y.text.toString().toDouble()
                val releaseTipBoxPoint2X = if (binding.etReleaseTipBoxPoint2X.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipBoxPoint2X.text.toString().toDouble()
                val releaseTipBoxPoint2Y = if (binding.etReleaseTipBoxPoint2Y.text.toString().isNullOrEmpty()) 0.0 else binding.etReleaseTipBoxPoint2Y.text.toString().toDouble()
                val additionalAirHeight = if (binding.etAdditionalAirHeight.text.toString().isNullOrEmpty()) 0.0 else binding.etAdditionalAirHeight.text.toString().toDouble()
                val tipCheckPosX = if (binding.etTipCheckPosX.text.toString().isNullOrEmpty()) 0.0 else binding.etTipCheckPosX.text.toString().toDouble()
                val tipCheckPosY = if (binding.etTipCheckPosY.text.toString().isNullOrEmpty()) 0.0 else binding.etTipCheckPosY.text.toString().toDouble()
                val tipCheckHeight = if (binding.etTipCheckHeight.text.toString().isNullOrEmpty()) 0.0 else binding.etTipCheckHeight.text.toString().toDouble()

                for (i in 0..95)
                {
                    val col = i % 8
                    val row = i / 8
                    paramPosTips.tips[i].x = firstPosX + col * stepLength * if (ConstantsUtils.HEAD_POS == EDirection.Right) -1 else 1
                    paramPosTips.tips[i].y = firstPosY + row * stepLength
                }
                paramPosTips.preTakeTipHeight = preTakeTipHeight
                paramPosTips.takeTipHeight = takeTipHeight
                paramPosTips.stepLength = stepLength
                paramPosTips.releaseTipPos.x = releaseTipX
                paramPosTips.releaseTipPos.y = releaseTipY
                paramPosTips.releaseTipHeight = releaseTipHeight
                paramPosTips.releaseTipOffset = releaseTipOffset
                paramPosTips.releaseTipChannel1Y = releaseTipChannel1Y
                paramPosTips.releaseTipBoxPoint1X = releaseTipBoxPoint1X
                paramPosTips.releaseTipBoxPoint1Y = releaseTipBoxPoint1Y
                paramPosTips.releaseTipBoxPoint2X = releaseTipBoxPoint2X
                paramPosTips.releaseTipBoxPoint2Y = releaseTipBoxPoint2Y
                paramPosTips.additionalAirHeight = additionalAirHeight
                paramPosTips.tipCheckPos.x = tipCheckPosX
                paramPosTips.tipCheckPos.y = tipCheckPosY
                paramPosTips.tipCheckHeight = tipCheckHeight

                if (FileUtils.saveTipsPos(paramPosTips, true)) Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
