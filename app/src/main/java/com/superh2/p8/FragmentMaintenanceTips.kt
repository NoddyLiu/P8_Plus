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
import com.superh2.p8.utils.CmdHelper
import kotlinx.android.synthetic.main.fragment_maintenance_tips.*

/**
 *@Description 工程师界面（枪头界面）
 *@Author  Noddy
 */
class FragmentMaintenanceTips : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceTips()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_tips, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_first_point_x.setOnClickListener(this)
        btn_first_point_y.setOnClickListener(this)
        btn_pre_take_tip_height.setOnClickListener(this)
        btn_take_tip_height.setOnClickListener(this)
        btn_release_tip_x.setOnClickListener(this)
        btn_release_tip_y.setOnClickListener(this)
        btn_release_tip_height.setOnClickListener(this)
        btn_release_tip_offset.setOnClickListener(this)
        btn_release_tip_channel_1_y.setOnClickListener(this)
        btn_release_tip_box_point_1.setOnClickListener(this)
        btn_release_tip_box_point_2.setOnClickListener(this)
        btn_additional_air_height.setOnClickListener(this)
        btn_retracted_pos.setOnClickListener(this)
        btn_extended_pos.setOnClickListener(this)
        btn_tip_check_pos.setOnClickListener(this)
        btn_tip_check_height.setOnClickListener(this)
        btn_save.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        et_first_point_x.setText(paramPosTips.tips[0].x.toString())
        et_first_point_y.setText(paramPosTips.tips[0].y.toString())

        et_pre_take_tip_height.setText(paramPosTips.preTakeTipHeight.toString())
        et_take_tip_height.setText(paramPosTips.takeTipHeight.toString())
        et_step_length.setText(paramPosTips.stepLength.toString())
        et_release_tip_x.setText(paramPosTips.releaseTipPos.x.toString())
        et_release_tip_y.setText(paramPosTips.releaseTipPos.y.toString())
        et_release_tip_height.setText(paramPosTips.releaseTipHeight.toString())
        et_release_tip_offset.setText(paramPosTips.releaseTipOffset.toString())
        et_release_tip_channel_1_y.setText(paramPosTips.releaseTipChannel1Y.toString())
        et_release_tip_box_point_1_x.setText(paramPosTips.releaseTipBoxPoint1X.toString())
        et_release_tip_box_point_1_y.setText(paramPosTips.releaseTipBoxPoint1Y.toString())
        et_release_tip_box_point_2_x.setText(paramPosTips.releaseTipBoxPoint2X.toString())
        et_release_tip_box_point_2_y.setText(paramPosTips.releaseTipBoxPoint2Y.toString())
        et_additional_air_height.setText(paramPosTips.additionalAirHeight.toString())
        et_retracted_pos.setText(paramPosTips.plateRetractedPos.toString())
        et_extended_pos.setText(paramPosTips.plateExtendedPos.toString())
        et_tip_check_pos_x.setText(paramPosTips.tipCheckPos.x.toString())
        et_tip_check_pos_y.setText(paramPosTips.tipCheckPos.y.toString())
        et_tip_check_height.setText(paramPosTips.tipCheckHeight.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(et_first_point_x.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(et_first_point_y.text.toString().toDouble(), true)
            R.id.btn_pre_take_tip_height -> CmdHelper.za(et_pre_take_tip_height.text.toString().toDouble(), true)
            R.id.btn_take_tip_height -> CmdHelper.za(et_take_tip_height.text.toString().toDouble(), true)
            R.id.btn_release_tip_x -> CmdHelper.xa(et_release_tip_x.text.toString().toDouble(), true)
            R.id.btn_release_tip_y -> CmdHelper.ya(et_release_tip_y.text.toString().toDouble(), true)
            R.id.btn_release_tip_height -> CmdHelper.za(et_release_tip_height.text.toString().toDouble(), true)
            R.id.btn_release_tip_offset -> CmdHelper.za(et_release_tip_height.text.toString().toDouble() - et_release_tip_offset.text.toString().toDouble(), true)
            R.id.btn_release_tip_channel_1_y -> CmdHelper.ya(et_release_tip_channel_1_y.text.toString().toDouble(), true)
            R.id.btn_release_tip_box_point_1 -> CmdHelper.xaya(et_release_tip_box_point_1_x.text.toString().toDouble(), et_release_tip_box_point_1_y.text.toString().toDouble(), true)
            R.id.btn_release_tip_box_point_2 -> CmdHelper.xaya(et_release_tip_box_point_2_x.text.toString().toDouble(), et_release_tip_box_point_2_y.text.toString().toDouble(), true)
            R.id.btn_additional_air_height -> CmdHelper.za(et_additional_air_height.text.toString().toDouble(), true)
            R.id.btn_retracted_pos -> CmdHelper.ma(et_retracted_pos.text.toString().toDouble(), true)
            R.id.btn_extended_pos -> CmdHelper.ma(et_extended_pos.text.toString().toDouble(), true)
            R.id.btn_tip_check_pos -> CmdHelper.xaya(et_tip_check_pos_x.text.toString().toDouble(), et_tip_check_pos_y.text.toString().toDouble(), true)
            R.id.btn_tip_check_height -> CmdHelper.za(et_tip_check_height.text.toString().toDouble(), true)
            R.id.btn_save ->
            {
                val firstPosX = if (et_first_point_x.text.toString().isNullOrEmpty()) 0.0 else et_first_point_x.text.toString().toDouble()
                val firstPosY = if (et_first_point_y.text.toString().isNullOrEmpty()) 0.0 else et_first_point_y.text.toString().toDouble()
                val preTakeTipHeight = if (et_pre_take_tip_height.text.toString().isNullOrEmpty()) 0.0 else et_pre_take_tip_height.text.toString().toDouble()
                val takeTipHeight = if (et_take_tip_height.text.toString().isNullOrEmpty()) 0.0 else et_take_tip_height.text.toString().toDouble()
                val stepLength = if (et_step_length.text.toString().isNullOrEmpty()) 0.0 else et_step_length.text.toString().toDouble()
                val releaseTipX = if (et_release_tip_x.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_x.text.toString().toDouble()
                val releaseTipY = if (et_release_tip_y.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_y.text.toString().toDouble()
                val releaseTipHeight = if (et_release_tip_height.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_height.text.toString().toDouble()
                val releaseTipOffset = if (et_release_tip_offset.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_offset.text.toString().toDouble()
                val releaseTipChannel1Y = if (et_release_tip_channel_1_y.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_channel_1_y.text.toString().toDouble()
                val releaseTipBoxPoint1X = if (et_release_tip_box_point_1_x.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_box_point_1_x.text.toString().toDouble()
                val releaseTipBoxPoint1Y = if (et_release_tip_box_point_1_y.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_box_point_1_y.text.toString().toDouble()
                val releaseTipBoxPoint2X = if (et_release_tip_box_point_2_x.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_box_point_2_x.text.toString().toDouble()
                val releaseTipBoxPoint2Y = if (et_release_tip_box_point_2_y.text.toString().isNullOrEmpty()) 0.0 else et_release_tip_box_point_2_y.text.toString().toDouble()
                val additionalAirHeight = if (et_additional_air_height.text.toString().isNullOrEmpty()) 0.0 else et_additional_air_height.text.toString().toDouble()
                val plateRetractedPos = if (et_retracted_pos.text.toString().isNullOrEmpty()) 0.0 else et_retracted_pos.text.toString().toDouble()
                val plateExtendedPos = if (et_extended_pos.text.toString().isNullOrEmpty()) 0.0 else et_extended_pos.text.toString().toDouble()
                val tipCheckPosX = if (et_tip_check_pos_x.text.toString().isNullOrEmpty()) 0.0 else et_tip_check_pos_x.text.toString().toDouble()
                val tipCheckPosY = if (et_tip_check_pos_y.text.toString().isNullOrEmpty()) 0.0 else et_tip_check_pos_y.text.toString().toDouble()
                val tipCheckHeight = if (et_tip_check_height.text.toString().isNullOrEmpty()) 0.0 else et_tip_check_height.text.toString().toDouble()

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
                paramPosTips.plateRetractedPos = plateRetractedPos
                paramPosTips.plateExtendedPos = plateExtendedPos
                paramPosTips.tipCheckPos.x = tipCheckPosX
                paramPosTips.tipCheckPos.y = tipCheckPosY
                paramPosTips.tipCheckHeight = tipCheckHeight

                if (FileUtils.saveTipsPos(paramPosTips, true)) Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
