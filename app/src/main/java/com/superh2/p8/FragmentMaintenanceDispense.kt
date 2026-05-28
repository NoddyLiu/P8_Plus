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
import com.superh2.library.utils.ParamsHelper.paramPosDispense
import com.superh2.p8.utils.CmdHelper
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.fragment_maintenance_dispense.*

/**
 *@Description 工程师界面（滴液界面）
 *@Author  Noddy
 */
class FragmentMaintenanceDispense : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceDispense()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_dispense, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_first_point_x.setOnClickListener(this)
        btn_first_point_y.setOnClickListener(this)
        btn_height_range.setOnClickListener(this)
        btn_save.setOnClickListener(this)

        // 64个位置按钮
        val layout_btn_group = rootView!!.findViewById<ViewGroup>(R.id.layout_btn_group) as GridLayout
        layout_btn_group.removeAllViews()
        for (row in 0..3)
        {
            for (col in 0..15)
            {
                val btn = FButton(mActivity)
                btn.text = ((col + 1) + row * 16).toString()
                btn.buttonColor = ContextCompat.getColor(context, R.color.fbutton_default_color)
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
                    fullScreen()
                    CmdHelper.toDispensePos(btn.text.toString().trim().toInt() - 1, 0, true)
                }
            }
        }

        refreshControls()
    }

    private fun refreshControls()
    {
        et_first_point_x.setText(paramPosDispense.slides[0].x.toString())
        et_first_point_y.setText(paramPosDispense.slides[0].y.toString())
        et_point_y_step.setText((paramPosDispense.yDistance).toString())
        et_height_range.setText(paramPosDispense.heightRange.toString())
        et_step_length_x.setText(paramPosDispense.stepLengthX.toString())
        et_step_length_y.setText(paramPosDispense.stepLengthY.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(et_first_point_x.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(et_first_point_y.text.toString().toDouble(), true)
            R.id.btn_height_range -> CmdHelper.za(et_height_range.text.toString().toDouble(), true)
            R.id.btn_save ->
            {
                val firstPosX = if (et_first_point_x.text.toString().isNullOrEmpty()) 0.0 else et_first_point_x.text.toString().toDouble()
                val firstPosY = if (et_first_point_y.text.toString().isNullOrEmpty()) 0.0 else et_first_point_y.text.toString().toDouble()
                val yDistance = if (et_point_y_step.text.toString().isNullOrEmpty()) 0.0 else et_point_y_step.text.toString().toDouble()
                val heightRange = if (et_height_range.text.toString().isNullOrEmpty()) 0.0 else et_height_range.text.toString().toDouble()
                val stepLengthX = if (et_step_length_x.text.toString().isNullOrEmpty()) 0.0 else et_step_length_x.text.toString().toDouble()
                val stepLengthY = if (et_step_length_y.text.toString().isNullOrEmpty()) 0.0 else et_step_length_y.text.toString().toDouble()

                for (i in 0..63)
                {
                    val col = i % 16
                    val row = i / 16
                    paramPosDispense.slides[i].x = firstPosX + col * stepLengthX * if(ConstantsUtils.HEAD_POS == EDirection.Right) -1 else 1
                    paramPosDispense.slides[i].y = firstPosY + row * stepLengthY
                }
                paramPosDispense.yDistance = yDistance
                paramPosDispense.heightRange = heightRange
                paramPosDispense.stepLengthX = stepLengthX
                paramPosDispense.stepLengthY = stepLengthY

                if (FileUtils.saveDispensePos(paramPosDispense, true))
                    Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
