package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramScale
import com.superh2.p8.databinding.FragmentMaintenanceScaleBinding
import com.superh2.p8.utils.ViewUtils.fullScreen

/**
 *@Description 工程师界面（scale比例值界面）
 *@Author  Noddy
 */
class FragmentMaintenanceScale : FragmentBase<FragmentMaintenanceScaleBinding>(FragmentMaintenanceScaleBinding::inflate), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceScale()
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        binding.btnSave.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        binding.etX.setText(paramScale.x.toString())
        binding.etY.setText(paramScale.y.toString())
        binding.etZ.setText(paramScale.z.toString())
        binding.etP.setText(paramScale.p.toString())
        binding.etW.setText(paramScale.w.toString())
        binding.etN.setText(paramScale.n.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen(activity)

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_save ->
            {
                paramScale.x = if (binding.etX.text.toString().isNullOrEmpty()) 0.0 else binding.etX.text.toString().toDouble()
                paramScale.y = if (binding.etY.text.toString().isNullOrEmpty()) 0.0 else binding.etY.text.toString().toDouble()
                paramScale.z = if (binding.etZ.text.toString().isNullOrEmpty()) 0.0 else binding.etZ.text.toString().toDouble()
                paramScale.p = if (binding.etP.text.toString().isNullOrEmpty()) 0.0 else binding.etP.text.toString().toDouble()
                paramScale.w = if (binding.etW.text.toString().isNullOrEmpty()) 0.0 else binding.etW.text.toString().toDouble()
                paramScale.n = if (binding.etN.text.toString().isNullOrEmpty()) 0.0 else binding.etN.text.toString().toDouble()

               if( FileUtils.saveScale(paramScale, true))
                   Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
