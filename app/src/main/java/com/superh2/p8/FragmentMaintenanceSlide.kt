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
import com.superh2.p8.utils.CmdHelper
import kotlinx.android.synthetic.main.fragment_maintenance_slide.*
import kotlinx.android.synthetic.main.fragment_maintenance_slide.btn_save
import kotlinx.android.synthetic.main.fragment_maintenance_slide.et_step_length_x
import kotlinx.android.synthetic.main.fragment_maintenance_slide.et_step_length_y

/**
 *@Description 工程师界面（玻片界面）
 *@Author  Noddy
 */
class FragmentMaintenanceSlide : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceSlide()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_slide, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_suck_slide_x.setOnClickListener(this)
        btn_suck_slide_y.setOnClickListener(this)
        btn_suck_slide_height.setOnClickListener(this)
        btn_walking_height.setOnClickListener(this)
        btn_release_first_point_x.setOnClickListener(this)
        btn_release_first_point_y.setOnClickListener(this)
        btn_release_height.setOnClickListener(this)

        btn_suck.setOnClickListener(this)
        btn_release.setOnClickListener(this)
        btn_save.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        et_suck_slide_x.setText(paramPosSlide.suckPos.x.toString())
        et_suck_slide_y.setText(paramPosSlide.suckPos.y.toString())
        et_suck_slide_height.setText(paramPosSlide.suckHeight.toString())
        et_slide_thickness.setText(paramPosSlide.thickness.toString())
        et_walking_height.setText(paramPosSlide.walkingHeight.toString())

        et_release_first_point_x.setText(paramPosSlide.slides[0].x.toString())
        et_release_first_point_y.setText(paramPosSlide.slides[0].y.toString())
        et_release_height.setText(paramPosSlide.releaseHeight.toString())
        et_step_length_x.setText(paramPosSlide.stepLengthX.toString())
        et_step_length_y.setText(paramPosSlide.stepLengthY.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")

            R.id.btn_suck_slide_x -> CmdHelper.xa(et_suck_slide_x.text.toString().toDouble(), true)
            R.id.btn_suck_slide_y -> CmdHelper.ya(et_suck_slide_y.text.toString().toDouble(), true)
            R.id.btn_suck_slide_height -> CmdHelper.wa(et_suck_slide_height.text.toString().toDouble(), true)
            R.id.btn_walking_height -> CmdHelper.wa(et_walking_height.text.toString().toDouble(), true)

            R.id.btn_release_first_point_x -> CmdHelper.xa(et_release_first_point_x.text.toString().toDouble(), true)
            R.id.btn_release_first_point_y -> CmdHelper.ya(et_release_first_point_y.text.toString().toDouble(), true)
            R.id.btn_release_height -> CmdHelper.wa(et_release_height.text.toString().toDouble(), true)

            R.id.btn_suck -> CmdHelper.doCmd("AO11", true)
            R.id.btn_release -> CmdHelper.doCmd("AO10", true)
            R.id.btn_save ->
            {
                val suckSlideX = if (et_suck_slide_x.text.toString().isNullOrEmpty()) 0.0
                else et_suck_slide_x.text.toString().toDouble()
                val suckSlideY = if (et_suck_slide_y.text.toString().isNullOrEmpty()) 0.0
                else et_suck_slide_y.text.toString().toDouble()
                val suckSlideHeight = if (et_suck_slide_height.text.toString().isNullOrEmpty()) 0.0
                else et_suck_slide_height.text.toString().toDouble()
                val slideThickness = if (et_slide_thickness.text.toString().isNullOrEmpty()) 0.0
                else et_slide_thickness.text.toString().toDouble()
                val walkingHeight = if (et_walking_height.text.toString().isNullOrEmpty()) 0.0
                else et_walking_height.text.toString().toDouble()

                val releaseFirstPointX =
                    if (et_release_first_point_x.text.toString().isNullOrEmpty()) 0.0
                    else et_release_first_point_x.text.toString().toDouble()
                val releaseFirstPointY =
                    if (et_release_first_point_y.text.toString().isNullOrEmpty()) 0.0
                    else et_release_first_point_y.text.toString().toDouble()
                val releaseHeight = if (et_release_height.text.toString().isNullOrEmpty()) 0.0
                else et_release_height.text.toString().toDouble()
                val stepLengthX = if (et_step_length_x.text.toString().isNullOrEmpty()) 0.0
                else et_step_length_x.text.toString().toDouble()
                val stepLengthY = if (et_step_length_y.text.toString().isNullOrEmpty()) 0.0
                else et_step_length_y.text.toString().toDouble()

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
