package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myException.InitException
import com.superh2.library.utils.ParamsHelper
import com.superh2.p8.MainActivity.Companion.mSerialClientHumiture
import com.superh2.p8.utils.CmdHelper
import kotlinx.android.synthetic.main.fragment_maintenance.*
import kotlin.concurrent.thread


/**
 *@Description 工程师界面（主界面）
 *@Author  Noddy
 */
class FragmentMaintenance : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance, container, false)
        return rootView as View
    }

    override fun onResume()
    {
        super.onResume()

        // 缩回防漏挡板，避免误撞
        CmdHelper.ma(ParamsHelper.paramPosTips.plateRetractedPos, true)
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_do.setOnClickListener(this)

        // 复位按钮
        btn_x_reset.setOnClickListener(this)
        btn_y_reset.setOnClickListener(this)
        btn_z_reset.setOnClickListener(this)
        btn_m_reset.setOnClickListener(this)
        btn_w_reset.setOnClickListener(this)
        btn_p_reset.setOnClickListener(this)
        btn_all_reset.setOnClickListener(this)

        btn_scale.setOnClickListener(this)
        btn_tubes_pos.setOnClickListener(this)
        btn_tips_pos.setOnClickListener(this)
//        btn_humid_blow_pos.setOnClickListener(this)
        btn_barcode_pos.setOnClickListener(this)
        btn_spray_pos.setOnClickListener(this)
        btn_dispense_pos.setOnClickListener(this)
        btn_fixative_pos.setOnClickListener(this)
        btn_slide_pos.setOnClickListener(this)
        btn_general_params.setOnClickListener(this)

        btn_light_on.setOnClickListener(this)
        btn_light_off.setOnClickListener(this)
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMain.newInstance(), "FragmentMain")
            R.id.btn_do ->
            {
                fullScreen()

                try
                {
                    CmdHelper.doCmd(et_do.text.toString(), true)
                }
                catch (ex: KotlinNullPointerException)
                {

                }
            }
            R.id.btn_x_reset -> CmdHelper.xi(true)
            R.id.btn_y_reset -> CmdHelper.yi(true)
            R.id.btn_z_reset -> CmdHelper.zi(true)
            R.id.btn_m_reset -> CmdHelper.mi(true)
            R.id.btn_w_reset -> CmdHelper.wi(true)
            R.id.btn_p_reset -> CmdHelper.pi(true)
            R.id.btn_all_reset ->
            {
                try
                {
                    thread { CmdHelper.initMotor() }
                }
                catch (e: InitException)
                {

                }
            }
            R.id.btn_scale -> replaceFragment(FragmentMaintenanceScale.newInstance(), "FragmentMaintenanceScale")
            R.id.btn_tubes_pos -> replaceFragment(FragmentMaintenanceTubes.newInstance(), "FragmentMaintenanceTubes")
            R.id.btn_tips_pos -> replaceFragment(FragmentMaintenanceTips.newInstance(), "FragmentMaintenanceTips")
//            R.id.btn_humid_blow_pos -> replaceFragment(FragmentMaintenanceHumidBlow.newInstance(), "FragmentMaintenanceHumidBlow")
            R.id.btn_barcode_pos -> replaceFragment(FragmentMaintenanceBarcode.newInstance(), "FragmentMaintenanceBarcode")
            R.id.btn_spray_pos -> replaceFragment(FragmentMaintenanceSpray.newInstance(), "FragmentMaintenanceSpray")
            R.id.btn_dispense_pos -> replaceFragment(FragmentMaintenanceDispense.newInstance(), "FragmentMaintenanceDispense")
            R.id.btn_fixative_pos -> replaceFragment(FragmentMaintenanceFixative.newInstance(), "FragmentMaintenanceFixative")
            R.id.btn_slide_pos ->replaceFragment(FragmentMaintenanceSlide.newInstance(), "FragmentMaintenanceSlide")
            R.id.btn_general_params -> replaceFragment(FragmentMaintenanceGeneralParameters.newInstance(), "FragmentMaintenanceEnginnerSetting")

            R.id.btn_light_on -> thread { mSerialClientHumiture.lightToggle(EOnOff.On) }
            R.id.btn_light_off -> thread { mSerialClientHumiture.lightToggle(EOnOff.Off) }
        }
    }

}
