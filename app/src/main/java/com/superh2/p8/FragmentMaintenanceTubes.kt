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
import com.superh2.library.utils.ParamsHelper.paramPosTubes
import com.superh2.p8.databinding.FragmentMaintenanceTubesBinding
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen
import info.hoang8f.widget.FButton

/**
 *@Description 工程师界面（试管界面）
 *@Author  Noddy
 */
class FragmentMaintenanceTubes : FragmentBase<FragmentMaintenanceTubesBinding>(FragmentMaintenanceTubesBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceTubes()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnFirstPointX.setOnClickListener(this)
        binding.btnFirstPointY.setOnClickListener(this)
        binding.btnSuckHeight.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        // 64个位置按钮
        val layout_btn_group = binding.layoutBtnGroup as GridLayout
        layout_btn_group.removeAllViews()
        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val btn = FButton(mActivity)
                btn.text = ((col + 1) + row * 8).toString()
                btn.buttonColor = ContextCompat.getColor(requireActivity(), R.color.fbutton_default_color)
                btn.isShadowEnabled = true
                btn.shadowHeight = 3
                val rowSpec = GridLayout.spec(row, GridLayout.CENTER)
                val columnSpec = GridLayout.spec(col, GridLayout.CENTER)
                val layoutParams = GridLayout.LayoutParams(rowSpec, columnSpec)
                layoutParams.height = 39
                layoutParams.width = 72
                layoutParams.leftMargin = 10
                layoutParams.topMargin = 3
                layout_btn_group.addView(btn, layoutParams)

                btn.setOnClickListener {
                    fullScreen(activity)
                    CmdHelper.toTubePos(btn.text.toString().trim().toInt() - 1, true)
                }
            }
        }

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.etFirstPointX.setText(paramPosTubes.tubes[0].x.toString())
        binding.etFirstPointY.setText(paramPosTubes.tubes[0].y.toString())
        binding.etSuckHeight.setText(paramPosTubes.suckHeight.toString())
        binding.etStepLength.setText(paramPosTubes.stepLength.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(binding.etFirstPointX.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(binding.etFirstPointY.text.toString().toDouble(), true)
            R.id.btn_suck_height ->  CmdHelper.za(binding.etSuckHeight.text.toString().toDouble(), true)
            R.id.btn_save ->
            {
                val firstPosX = if (binding.etFirstPointX.text.toString().isNullOrEmpty()) 0.0 else binding.etFirstPointX.text.toString().toDouble()
                val firstPosY = if (binding.etFirstPointY.text.toString().isNullOrEmpty()) 0.0 else binding.etFirstPointY.text.toString().toDouble()
                val suckHeight = if (binding.etSuckHeight.text.toString().isNullOrEmpty()) 0.0 else binding.etSuckHeight.text.toString().toDouble()
                val stepLength = if (binding.etStepLength.text.toString().isNullOrEmpty()) 0.0 else binding.etStepLength.text.toString().toDouble()

                for (i in 0..63)
                {
                    val col = i % 8
                    val row = i / 8
                    paramPosTubes.tubes[i].x = firstPosX + col * stepLength * if(ConstantsUtils.HEAD_POS == EDirection.Right) -1 else 1
                    paramPosTubes.tubes[i].y = firstPosY + row * stepLength
                }
                paramPosTubes.suckHeight = suckHeight
                paramPosTubes.stepLength = stepLength

                if (FileUtils.saveTubesPos(paramPosTubes, true))
                    Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
