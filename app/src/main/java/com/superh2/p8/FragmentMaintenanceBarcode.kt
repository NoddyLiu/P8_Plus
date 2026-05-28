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
import com.superh2.library.myInterface.IScannerResultCallback
import com.superh2.library.utils.ConstantsUtils
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramPosBarcode
import com.superh2.p8.MainActivity.Companion.mSerialClientScanner
import com.superh2.p8.utils.CmdHelper
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.fragment_maintenance_barcode.*

/**
 *@Description 工程师界面（二维码识别界面）
 *@Author  Noddy
 */
class FragmentMaintenanceBarcode : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceBarcode()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_barcode, container, false)
        return rootView as View
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_first_point_x.setOnClickListener(this)
        btn_first_point_y.setOnClickListener(this)
        btn_left_1mm.setOnClickListener(this)
        btn_right_1mm.setOnClickListener(this)
        btn_top_1mm.setOnClickListener(this)
        btn_bottom_1mm.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        btn_scan.setOnClickListener(this)

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
                layoutParams.leftMargin = 5
                layoutParams.topMargin = 3
                layout_btn_group.addView(btn, layoutParams)

                btn.setOnClickListener {
                    fullScreen()
                    CmdHelper.toBarcodePos(btn.text.toString().trim().toInt() - 1, true)
                }
            }
        }

        refreshControls()
    }

    private fun refreshControls()
    {
        et_first_point_x.setText(paramPosBarcode.slides[0].x.toString())
        et_first_point_y.setText(paramPosBarcode.slides[0].y.toString())
        et_step_length_x.setText(paramPosBarcode.stepLengthX.toString())
        et_step_length_y.setText(paramPosBarcode.stepLengthY.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_first_point_x -> CmdHelper.xa(et_first_point_x.text.toString().toDouble(), true)
            R.id.btn_first_point_y -> CmdHelper.ya(et_first_point_y.text.toString().toDouble(), true)
            R.id.btn_left_1mm -> CmdHelper.xs(-1.0, true)
            R.id.btn_right_1mm -> CmdHelper.xs(1.0, true)
            R.id.btn_top_1mm -> CmdHelper.ys(-1.0, true)
            R.id.btn_bottom_1mm -> CmdHelper.ys(1.0, true)
            R.id.btn_save ->
            {
                val firstPosX = if (et_first_point_x.text.toString().isNullOrEmpty()) 0.0
                else et_first_point_x.text.toString().toDouble()
                val firstPosY = if (et_first_point_y.text.toString().isNullOrEmpty()) 0.0
                else et_first_point_y.text.toString().toDouble()
                val stepLengthX = if (et_step_length_x.text.toString().isNullOrEmpty()) 0.0
                else et_step_length_x.text.toString().toDouble()
                val stepLengthY = if (et_step_length_y.text.toString().isNullOrEmpty()) 0.0
                else et_step_length_y.text.toString().toDouble()

                for (i in 0..63)
                {
                    val col = i % 16
                    val row = i / 16
                    paramPosBarcode.slides[i].x = firstPosX + col * stepLengthX * if(ConstantsUtils.HEAD_POS == EDirection.Right) -1 else 1
                    paramPosBarcode.slides[i].y = firstPosY + row * stepLengthY
                }
                paramPosBarcode.stepLengthX = stepLengthX
                paramPosBarcode.stepLengthY = stepLengthY

                if (FileUtils.saveBarcodePos(paramPosBarcode, true)) Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }

            R.id.btn_scan ->
            {
                if (mSerialClientScanner.isConnected)
                {
                    mSerialClientScanner.decodeStart(object : IScannerResultCallback
                    {
                        override fun success(info: String)
                        {
                            tv_scan_result.text = info
                        }

                        override fun timeOut()
                        {
                            Toast.makeText(mActivity, getString(R.string.info_scanner_scan_error), Toast.LENGTH_LONG).show()
                        }
                    })
                }
                else
                {
                    Toast.makeText(mActivity, getString(R.string.info_scanner_connect_error), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
