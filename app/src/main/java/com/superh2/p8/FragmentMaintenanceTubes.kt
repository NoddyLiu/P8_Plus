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
import com.superh2.p8.utils.CmdHelper
import kotlinx.android.synthetic.main.fragment_maintenance_tubes.*
import info.hoang8f.widget.FButton

/**
 *@Description 工程师界面（试管界面）
 *@Author  Noddy
 */
class FragmentMaintenanceTubes : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceTubes()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_tubes, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_first_point_x.setOnClickListener(this)
        btn_first_point_y.setOnClickListener(this)
        btn_suck_height.setOnClickListener(this)
        btn_save.setOnClickListener(this)

        // 64个位置按钮
        val layout_btn_group = rootView!!.findViewById<ViewGroup>(R.id.layout_btn_group) as GridLayout
        layout_btn_group.removeAllViews()
        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val btn = FButton(mActivity)
                btn.text = ((col + 1) + row * 8).toString()
                btn.buttonColor = ContextCompat.getColor(context, R.color.fbutton_default_color)
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
                    fullScreen()
                    CmdHelper.toTubePos(btn.text.toString().trim().toInt() - 1, true)
                }
            }
        }

        refreshControls()
    }

    private fun refreshControls()
    {
        et_first_point_x.setText(paramPosTubes.tubes[0].x.toString())
        et_first_point_y.setText(paramPosTubes.tubes[0].y.toString())
        et_suck_height.setText(paramPosTubes.suckHeight.toString())
        et_step_length.setText(paramPosTubes.stepLength.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(et_first_point_x.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(et_first_point_y.text.toString().toDouble(), true)
            R.id.btn_suck_height ->  CmdHelper.za(et_suck_height.text.toString().toDouble(), true)
            R.id.btn_save ->
            {
                val firstPosX = if (et_first_point_x.text.toString().isNullOrEmpty()) 0.0 else et_first_point_x.text.toString().toDouble()
                val firstPosY = if (et_first_point_y.text.toString().isNullOrEmpty()) 0.0 else et_first_point_y.text.toString().toDouble()
                val suckHeight = if (et_suck_height.text.toString().isNullOrEmpty()) 0.0 else et_suck_height.text.toString().toDouble()
                val stepLength = if (et_step_length.text.toString().isNullOrEmpty()) 0.0 else et_step_length.text.toString().toDouble()

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
