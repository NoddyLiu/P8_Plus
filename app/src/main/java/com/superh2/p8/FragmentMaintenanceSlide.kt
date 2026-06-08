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
import com.superh2.library.utils.ParamsHelper.paramPosSlide
import com.superh2.p8.databinding.FragmentMaintenanceSlideBinding
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen

/**
 *@Description 工程师界面（玻片界面）
 *@Author  Noddy
 */
class FragmentMaintenanceSlide : FragmentBase<FragmentMaintenanceSlideBinding>(FragmentMaintenanceSlideBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceSlide()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnSuckSlideX.setOnClickListener(this)
        binding.btnSuckSlideY.setOnClickListener(this)
        binding.btnSuckSlideHeight.setOnClickListener(this)
        binding.btnWalkingHeight.setOnClickListener(this)
        binding.btnReleaseFirstPointX.setOnClickListener(this)
        binding.btnReleaseFirstPointY.setOnClickListener(this)
        binding.btnReleaseHeight.setOnClickListener(this)

        binding.btnSuck.setOnClickListener(this)
        binding.btnRelease.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.etSuckSlideX.setText(paramPosSlide.suckPos.x.toString())
        binding.etSuckSlideY.setText(paramPosSlide.suckPos.y.toString())
        binding.etSuckSlideHeight.setText(paramPosSlide.suckHeight.toString())
        binding.etSlideThickness.setText(paramPosSlide.thickness.toString())
        binding.etWalkingHeight.setText(paramPosSlide.walkingHeight.toString())

        binding.etReleaseFirstPointX.setText(paramPosSlide.slides[0].x.toString())
        binding.etReleaseFirstPointY.setText(paramPosSlide.slides[0].y.toString())
        binding.etReleaseHeight.setText(paramPosSlide.releaseHeight.toString())
        binding.etStepLengthX.setText(paramPosSlide.stepLengthX.toString())
        binding.etStepLengthY.setText(paramPosSlide.stepLengthY.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")

            R.id.btn_suck_slide_x -> CmdHelper.xa(binding.etSuckSlideX.text.toString().toDouble(), true)
            R.id.btn_suck_slide_y -> CmdHelper.ya(binding.etSuckSlideY.text.toString().toDouble(), true)
            R.id.btn_suck_slide_height -> CmdHelper.wa(binding.etSuckSlideHeight.text.toString().toDouble(), true)
            R.id.btn_walking_height -> CmdHelper.wa(binding.etWalkingHeight.text.toString().toDouble(), true)

            R.id.btn_release_first_point_x -> CmdHelper.xa(binding.etReleaseFirstPointX.text.toString().toDouble(), true)
            R.id.btn_release_first_point_y -> CmdHelper.ya(binding.etReleaseFirstPointY.text.toString().toDouble(), true)
            R.id.btn_release_height -> CmdHelper.wa(binding.etReleaseHeight.text.toString().toDouble(), true)

            R.id.btn_suck -> CmdHelper.doCmd("AO11", true)
            R.id.btn_release -> CmdHelper.doCmd("AO10", true)
            R.id.btn_save ->
            {
                val suckSlideX = if (binding.etSuckSlideX.text.toString().isNullOrEmpty()) 0.0
                else binding.etSuckSlideX.text.toString().toDouble()
                val suckSlideY = if (binding.etSuckSlideY.text.toString().isNullOrEmpty()) 0.0
                else binding.etSuckSlideY.text.toString().toDouble()
                val suckSlideHeight = if (binding.etSuckSlideHeight.text.toString().isNullOrEmpty()) 0.0
                else binding.etSuckSlideHeight.text.toString().toDouble()
                val slideThickness = if (binding.etSlideThickness.text.toString().isNullOrEmpty()) 0.0
                else binding.etSlideThickness.text.toString().toDouble()
                val walkingHeight = if (binding.etWalkingHeight.text.toString().isNullOrEmpty()) 0.0
                else binding.etWalkingHeight.text.toString().toDouble()

                val releaseFirstPointX =
                    if (binding.etReleaseFirstPointX.text.toString().isNullOrEmpty()) 0.0
                    else binding.etReleaseFirstPointX.text.toString().toDouble()
                val releaseFirstPointY =
                    if (binding.etReleaseFirstPointY.text.toString().isNullOrEmpty()) 0.0
                    else binding.etReleaseFirstPointY.text.toString().toDouble()
                val releaseHeight = if (binding.etReleaseHeight.text.toString().isNullOrEmpty()) 0.0
                else binding.etReleaseHeight.text.toString().toDouble()
                val stepLengthX = if (binding.etStepLengthX.text.toString().isNullOrEmpty()) 0.0
                else binding.etStepLengthX.text.toString().toDouble()
                val stepLengthY = if (binding.etStepLengthY.text.toString().isNullOrEmpty()) 0.0
                else binding.etStepLengthY.text.toString().toDouble()

                paramPosSlide.suckPos.x = suckSlideX
                paramPosSlide.suckPos.y = suckSlideY
                paramPosSlide.suckHeight = suckSlideHeight
                paramPosSlide.thickness = slideThickness
                paramPosSlide.walkingHeight = walkingHeight

                for (i in 0..63)
                {
                    val col = i % 16
                    val row = i / 16
                    paramPosSlide.slides[i].x = releaseFirstPointX + col * stepLengthX * if(ConstantsUtils.HEAD_POS == EDirection.Right) -1 else 1
                    paramPosSlide.slides[i].y = releaseFirstPointY + row * stepLengthY
                }
                paramPosSlide.releaseHeight = releaseHeight
                paramPosSlide.stepLengthX = stepLengthX
                paramPosSlide.stepLengthY = stepLengthY

                if (FileUtils.saveSlidePos(paramPosSlide, true)) Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }

        }
    }

}
