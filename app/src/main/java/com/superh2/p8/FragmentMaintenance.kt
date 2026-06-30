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
import com.superh2.p8.databinding.FragmentMaintenanceBinding
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen
import kotlin.concurrent.thread


/**
 *@Description 工程师界面（主界面）
 *@Author  Noddy
 */
class FragmentMaintenance : FragmentBase<FragmentMaintenanceBinding>(FragmentMaintenanceBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenance()
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnDo.setOnClickListener(this)

        // 复位按钮
        binding.btnXReset.setOnClickListener(this)
        binding.btnYReset.setOnClickListener(this)
        binding.btnZReset.setOnClickListener(this)
        binding.btnMReset.setOnClickListener(this)
        binding.btnWReset.setOnClickListener(this)
        binding.btnPReset.setOnClickListener(this)
        binding.btnAllReset.setOnClickListener(this)

        binding.btnScale.setOnClickListener(this)
        binding.btnTubesPos.setOnClickListener(this)
        binding.btnTipsPos.setOnClickListener(this)
        binding.btnBarcodePos.setOnClickListener(this)
        binding.btnSprayPos.setOnClickListener(this)
        binding.btnDispensePos.setOnClickListener(this)
        binding.btnFixativePos.setOnClickListener(this)
        binding.btnSlidePos.setOnClickListener(this)
        binding.btnCollectPos.setOnClickListener(this)
        binding.btnGeneralParams.setOnClickListener(this)

        binding.btnLightOn.setOnClickListener(this)
        binding.btnLightOff.setOnClickListener(this)
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMain.newInstance(), "FragmentMain")
            R.id.btn_do ->
            {
                fullScreen(activity)

                try
                {
                    CmdHelper.doCmd(binding.etDo.text.toString(), true)
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
            R.id.btn_barcode_pos -> replaceFragment(FragmentMaintenanceBarcode.newInstance(), "FragmentMaintenanceBarcode")
            R.id.btn_spray_pos -> replaceFragment(FragmentMaintenanceSpray.newInstance(), "FragmentMaintenanceSpray")
            R.id.btn_dispense_pos -> replaceFragment(FragmentMaintenanceDispense.newInstance(), "FragmentMaintenanceDispense")
            R.id.btn_fixative_pos -> replaceFragment(FragmentMaintenanceFixative.newInstance(), "FragmentMaintenanceFixative")
            R.id.btn_slide_pos ->replaceFragment(FragmentMaintenanceSlide.newInstance(), "FragmentMaintenanceSlide")
            R.id.btn_collect_pos ->replaceFragment(FragmentMaintenanceCollect.newInstance(), "FragmentMaintenanceCollect")
            R.id.btn_general_params -> replaceFragment(FragmentMaintenanceGeneralParameters.newInstance(), "FragmentMaintenanceEnginnerSetting")

            R.id.btn_light_on -> thread { mSerialClientHumiture.lightToggle(EOnOff.On) }
            R.id.btn_light_off -> thread { mSerialClientHumiture.lightToggle(EOnOff.Off) }
        }
    }

}
