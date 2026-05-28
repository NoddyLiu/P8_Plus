package com.superh2.p8


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper
import com.superh2.library.utils.ParamsHelper.paramPosOther
import com.superh2.p8.FragmentMain.Companion.selectedMethodParams
import com.superh2.p8.utils.CmdHelper
import kotlinx.android.synthetic.main.fragment_hardware_io.*
import kotlin.concurrent.thread


/**
 *@Description 硬件测试界面
 *@Author  Noddy
 */
class FragmentHardwareIO : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentHardwareIO()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_hardware_io, container, false)
        return rootView as View
    }

    override fun onResume()
    {
        super.onResume()

        // 设置固定液速度
        CmdHelper.fixativePressure(selectedMethodParams.paramsSlideMode.fixativeDispensePressure, false)

        // 缩回防漏挡板，避免误撞
        CmdHelper.ma(ParamsHelper.paramPosTips.plateRetractedPos, true)
    }

    override fun initWidget()
    {
        // 软键盘
        //        val mCustomKeyboard = find<FloatingKeyboardViewNotDragable>(R.id.keyboardview)
        //        mCustomKeyboard.setKeyboard(R.xml.keyboard_number) // 设置键盘
        //        mCustomKeyboard.isPreviewEnabled = false

        // 注册控件
        //        mCustomKeyboard.registerEditText(et_spray_time)
        //        mCustomKeyboard.registerEditText(et_fixative_volume)
        //        mCustomKeyboard.registerEditText(et_humid_blow)

        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        val btnNext = mActivity?.findViewById(R.id.btnFunctionRight) as Button
        btnNext.visibility = View.GONE

        btn_spray_time.setOnClickListener(this)
        btn_spray_liquid_addition.setOnClickListener(this)
        btn_fixative_volume.setOnClickListener(this)
        btn_fixative_liquid_spit_out.setOnClickListener(this)
        //        btn_humid_blow_open.setOnClickListener(this)
        //        btn_humid_blow_close.setOnClickListener(this)


        et_replace_pos_x.setText(paramPosOther.replaceSealingRingPos.x.toString())
        et_replace_pos_y.setText(paramPosOther.replaceSealingRingPos.y.toString())
        et_replace_pos_z.setText(paramPosOther.replaceSealingRingHeight.toString())
        et_replace_pos_x.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                var value = 0.0
                if (et_replace_pos_x.text.toString().isNotEmpty()) value = et_replace_pos_x.text.toString().toDouble()
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
        et_replace_pos_y.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                var value = 0.0
                if (et_replace_pos_y.text.toString().isNotEmpty()) value = et_replace_pos_y.text.toString().toDouble()
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
        et_replace_pos_z.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(p0: Editable?)
            {
                var value = 0.0
                if (et_replace_pos_z.text.toString().isNotEmpty()) value = et_replace_pos_z.text.toString().toDouble()
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
        btn_replace_pos_x_y.setOnClickListener(this)
        btn_replace_pos_z.setOnClickListener(this)
    }

    override fun onClick(v: View)
    {
        fullScreen()
        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMain.newInstance(), "FragmentMain")
            R.id.btn_spray_time ->
            {
                val sec = et_spray_time.text.toString()
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
                CmdHelper.sprayAddLiquid(et_spray_liquid_addition.text.toString().toInt(), true)
            }
            R.id.btn_fixative_volume ->
            {
                val vol = et_fixative_volume.text.toString()
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
            //            R.id.btn_humid_blow_open ->
            //            {
            //                val level = et_humid_blow.text.toString()
            //                if (!level.isNullOrEmpty() && level.toInt() <= 100)
            //                {
            //                    openHumidBlows(level.toInt(), EOnOff.On)
            //                }
            //            }
            //            R.id.btn_humid_blow_close ->
            //            {
            //                closeHumidBlows()
            //            }
            R.id.btn_replace_pos_x_y ->
            {
                CmdHelper.xaya(et_replace_pos_x.text.toString().toDouble(), et_replace_pos_y.text.toString().toDouble(), true)
            }
            R.id.btn_replace_pos_z ->
            {
                CmdHelper.za(et_replace_pos_z.text.toString().toDouble(), true)
            }
        }
    }
}
