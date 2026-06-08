package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.superh2.library.myEnum.EDirection
import com.superh2.library.utils.ConstantsUtils
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramPosFixative
import com.superh2.p8.databinding.FragmentMaintenanceFixativeBinding
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen
import info.hoang8f.widget.FButton

/**
 *@Description 工程师界面（固定液界面）
 *@Author  Noddy
 */
class FragmentMaintenanceFixative : FragmentBase<FragmentMaintenanceFixativeBinding>(FragmentMaintenanceFixativeBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceFixative()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnFirstPointX.setOnClickListener(this)
        binding.btnFirstPointY.setOnClickListener(this)
        binding.btnHeightRange.setOnClickListener(this)
        binding.btnFixativeHeight.setOnClickListener(this)
        binding.btnTestPointX.setOnClickListener(this)
        binding.btnTestPointY.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        // 64个位置按钮
        val layout_btn_group = binding.layoutBtnGroup
        layout_btn_group.removeAllViews()
        for (row in 0..3)
        {
            for (col in 0..15)
            {
                val btn = FButton(mActivity)
                btn.text = ((col + 1) + row * 16).toString()
                btn.buttonColor = ContextCompat.getColor(requireActivity(), R.color.fbutton_default_color)
                btn.isShadowEnabled = true
                btn.shadowHeight = 3
                val rowSpec = GridLayout.spec(row, GridLayout.CENTER)
                val columnSpec = GridLayout.spec(col, GridLayout.CENTER)
                val layoutParams = GridLayout.LayoutParams(rowSpec, columnSpec)
                layoutParams.height = 39
                layoutParams.width = 58
                layoutParams.leftMargin = 4
                layoutParams.topMargin = 3
                layout_btn_group.addView(btn, layoutParams)

                btn.setOnClickListener {
                    fullScreen(activity)
                    CmdHelper.toFixativePos(btn.text.toString().trim().toInt() - 1, 0, true)
                }
            }
        }

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.etFirstPointX.setText(paramPosFixative.slides[0].x.toString())
        binding.etFirstPointY.setText(paramPosFixative.slides[0].y.toString())
        binding.etPointYStep.setText((paramPosFixative.yDistance).toString())
        binding.etHeightRange.setText(paramPosFixative.heightRange.toString())
        binding.etFixativeHeight.setText(paramPosFixative.fixativeHeight.toString())
        binding.etStepLengthX.setText(paramPosFixative.stepLengthX.toString())
        binding.etStepLengthY.setText(paramPosFixative.stepLengthY.toString())
        binding.etTestPointX.setText(paramPosFixative.testPos.x.toString())
        binding.etTestPointY.setText(paramPosFixative.testPos.y.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(binding.etFirstPointX.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(binding.etFirstPointY.text.toString().toDouble(), true)
            R.id.btn_height_range -> CmdHelper.wa(binding.etHeightRange.text.toString().toDouble(), true)
            R.id.btn_fixative_height -> CmdHelper.wa(binding.etHeightRange.text.toString().toDouble() - binding.etFixativeHeight.text.toString().toDouble(), true)
            R.id.btn_test_point_x -> CmdHelper.xa(binding.etTestPointX.text.toString().toDouble(), true)
            R.id.btn_test_point_y -> CmdHelper.ya(binding.etTestPointY.text.toString().toDouble(), true)
            R.id.btn_save ->
            {
                val firstPosX = if (binding.etFirstPointX.text.toString().isNullOrEmpty()) 0.0 else binding.etFirstPointX.text.toString().toDouble()
                val firstPosY = if (binding.etFirstPointY.text.toString().isNullOrEmpty()) 0.0 else binding.etFirstPointY.text.toString().toDouble()
                val yDistance = if (binding.etPointYStep.text.toString().isNullOrEmpty()) 0.0 else binding.etPointYStep.text.toString().toDouble()
                val heightRange = if (binding.etHeightRange.text.toString().isNullOrEmpty()) 0.0 else binding.etHeightRange.text.toString().toDouble()
                val fixativeHeight = if (binding.etFixativeHeight.text.toString().isNullOrEmpty()) 0 else binding.etFixativeHeight.text.toString().toInt()
                val stepLengthX = if (binding.etStepLengthX.text.toString().isNullOrEmpty()) 0.0 else binding.etStepLengthX.text.toString().toDouble()
                val stepLengthY = if (binding.etStepLengthY.text.toString().isNullOrEmpty()) 0.0 else binding.etStepLengthY.text.toString().toDouble()
                val testPosX = if (binding.etTestPointX.text.toString().isNullOrEmpty()) 0.0 else binding.etTestPointX.text.toString().toDouble()
                val testPosY = if (binding.etTestPointY.text.toString().isNullOrEmpty()) 0.0 else binding.etTestPointY.text.toString().toDouble()

                for (i in 0..63)
                {
                    val col = i % 16
                    val row = i / 16
                    paramPosFixative.slides[i].x = firstPosX + col * stepLengthX * if(ConstantsUtils.HEAD_POS == EDirection.Right) -1 else 1
                    paramPosFixative.slides[i].y = firstPosY + row * stepLengthY
                }
                paramPosFixative.yDistance = yDistance
                paramPosFixative.heightRange = heightRange
                paramPosFixative.fixativeHeight = fixativeHeight
                paramPosFixative.stepLengthX = stepLengthX
                paramPosFixative.stepLengthY = stepLengthY
                paramPosFixative.testPos.x = testPosX
                paramPosFixative.testPos.y = testPosY

                if (FileUtils.saveFixativePos(paramPosFixative, true))
                    Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
