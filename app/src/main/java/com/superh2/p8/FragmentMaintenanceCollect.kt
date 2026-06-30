package com.superh2.p8

import android.view.View
import android.widget.Button
import android.widget.Toast
import com.superh2.library.myInterface.ParamsDecimalSetListener
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramPosCollect
import com.superh2.p8.databinding.FragmentMaintenanceCollectBinding
import com.superh2.p8.dialogs.DialogFragment_Params_Setting_Decimal
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.ViewUtils.fullScreen

/**
 *@Description 工程师界面（收集位置界面）
 *@Author  Noddy
 */
class FragmentMaintenanceCollect : FragmentBase<FragmentMaintenanceCollectBinding>(FragmentMaintenanceCollectBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceCollect()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnRack1X.setOnClickListener(this)
        binding.btnRack1Y.setOnClickListener(this)
        binding.btnRack1Z.setOnClickListener(this)
        binding.btnRack2X.setOnClickListener(this)
        binding.btnRack2Y.setOnClickListener(this)
        binding.btnRack2Z.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.switchAutoCollect.isChecked = paramPosCollect.isAutoCollect
        binding.etRack1X.setText(paramPosCollect.rack1StartX.toString())
        binding.etRack1Y.setText(paramPosCollect.rack1StartY.toString())
        binding.etRack1Z.setText(paramPosCollect.rack1StartZ.toString())
        binding.etRack2X.setText(paramPosCollect.rack2StartX.toString())
        binding.etRack2Y.setText(paramPosCollect.rack2StartY.toString())
        binding.etRack2Z.setText(paramPosCollect.rack2StartZ.toString())
        binding.etInsertY.setText(paramPosCollect.insertYDist.toString())
        binding.etPlaceZ.setText(paramPosCollect.placeZDist.toString())
        binding.etStepX.setText(paramPosCollect.stepX.toString())
        binding.etStepZ.setText(paramPosCollect.stepZ.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_rack1_x -> CmdHelper.xa(binding.etRack1X.text.toString().toDouble(), true)
            R.id.btn_rack1_y -> CmdHelper.ya(binding.etRack1Y.text.toString().toDouble(), true)
            R.id.btn_rack1_z -> CmdHelper.wa(binding.etRack1Z.text.toString().toDouble(), true)
            R.id.btn_rack2_x -> CmdHelper.xa(binding.etRack2X.text.toString().toDouble(), true)
            R.id.btn_rack2_y -> CmdHelper.ya(binding.etRack2Y.text.toString().toDouble(), true)
            R.id.btn_rack2_z -> CmdHelper.wa(binding.etRack2Z.text.toString().toDouble(), true)

            R.id.btn_save ->
            {
                paramPosCollect.isAutoCollect = binding.switchAutoCollect.isChecked
                paramPosCollect.rack1StartX = binding.etRack1X.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.rack1StartY = binding.etRack1Y.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.rack1StartZ = binding.etRack1Z.text.toString().toDoubleOrNull() ?: 0.0

                paramPosCollect.rack2StartX = binding.etRack2X.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.rack2StartY = binding.etRack2Y.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.rack2StartZ = binding.etRack2Z.text.toString().toDoubleOrNull() ?: 0.0

                paramPosCollect.insertYDist = binding.etInsertY.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.placeZDist = binding.etPlaceZ.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.stepX = binding.etStepX.text.toString().toDoubleOrNull() ?: 0.0
                paramPosCollect.stepZ = binding.etStepZ.text.toString().toDoubleOrNull() ?: 0.0

                if (FileUtils.saveCollectPos(paramPosCollect, true))
                {
                    Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}