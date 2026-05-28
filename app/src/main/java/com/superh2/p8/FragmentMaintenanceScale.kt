package com.superh2.p8


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.ParamsHelper.paramScale
import kotlinx.android.synthetic.main.fragment_maintenance_scale.*

/**
 *@Description 工程师界面（scale比例值界面）
 *@Author  Noddy
 */
class FragmentMaintenanceScale : FragmentBase(), View.OnClickListener
{
    companion object
    {
        fun newInstance() = FragmentMaintenanceScale()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        rootView = inflater.inflate(R.layout.fragment_maintenance_scale, container, false)
        return rootView as View
    }

    override fun initWidget()
    {
        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.VISIBLE
        btnReturn.setOnClickListener(this)

        btn_save.setOnClickListener(this)

        refreshControls()
    }

    private fun refreshControls()
    {
        et_x.setText(paramScale.x.toString())
        et_y.setText(paramScale.y.toString())
        et_z.setText(paramScale.z.toString())
        et_p.setText(paramScale.p.toString())
        et_m.setText(paramScale.m.toString())
        et_w.setText(paramScale.w.toString())
        et_n.setText(paramScale.n.toString())
    }

    override fun onClick(v: View)
    {
        fullScreen()

        when (v.id)
        {
            R.id.btnReturn -> replaceFragment(FragmentMaintenance.newInstance(), "FragmentMaintenance")
            R.id.btn_save ->
            {
                paramScale.x = if (et_x.text.toString().isNullOrEmpty()) 0.0 else et_x.text.toString().toDouble()
                paramScale.y = if (et_y.text.toString().isNullOrEmpty()) 0.0 else et_y.text.toString().toDouble()
                paramScale.z = if (et_z.text.toString().isNullOrEmpty()) 0.0 else et_z.text.toString().toDouble()
                paramScale.p = if (et_p.text.toString().isNullOrEmpty()) 0.0 else et_p.text.toString().toDouble()
                paramScale.m = if (et_m.text.toString().isNullOrEmpty()) 0.0 else et_m.text.toString().toDouble()
                paramScale.w = if (et_w.text.toString().isNullOrEmpty()) 0.0 else et_w.text.toString().toDouble()
                paramScale.n = if (et_n.text.toString().isNullOrEmpty()) 0.0 else et_n.text.toString().toDouble()

               if( FileUtils.saveScale(paramScale, true))
                   Toast.makeText(mActivity, getString(R.string.info_save_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
