package com.superh2.p8

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.superh2.library.comm.DataCommunication
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myInterface.BarcodeScanListener
import com.superh2.library.utils.LogHelper
import com.superh2.library.utils.ParamsHelper
import com.superh2.library.utils.ParamsHelper.loadAllParams
import com.superh2.library.utils.ViewUtils
import com.superh2.library.utils.ViewUtils.changeAlertdialogTextSize
import com.superh2.p8.serial.SerialClientHumiture
import com.superh2.p8.serial.SerialClientScanner
import com.superh2.p8.utils.CmdHelper
import pub.devrel.easypermissions.EasyPermissions
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity()
{
    val TAG = "MainActivity"

    // 二维码扫描监听回调
    var barcodeScanListener: BarcodeScanListener? = null
    // 扫描枪扫描二维码
    private var barcodeFromScannerGun = ""

    // 全局静态变量
    companion object
    {
        lateinit var mContext: Context
        lateinit var mDataCommunication: DataCommunication

        // 程序是否正在运行
        var isProcedureRunning = false

        // 是否扫描玻片二维码中
        var isScanningQRCode = false

        var mHandlerMain: Handler = Handler() // 主线程handler
        var mHandlerRunTh: Handler = Handler() // 运行线程handler
        var mHandlerOperationTh: Handler = Handler() // 操作线程handler

        // 弹出对话框
        var waitDialog: AlertDialog? = null
        var rnHideWaitDialog: Runnable = Runnable {
            ViewUtils.closeDialog(waitDialog!!)
            waitDialog!!.dismiss()

            isProcedureRunning = false

            mHandlerOperationTh.post { CmdHelper.closeAllMachine() }
        }

        var isManualPause = false
        var isManualStop = false
        var rnShowWaitDialog: Runnable = Runnable {
            val dialogOnclickListener = DialogInterface.OnClickListener { _, i ->
                when (i)
                {
                    Dialog.BUTTON_NEGATIVE // 继续按钮
                    ->
                    {
                        waitDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.INVISIBLE
                        waitDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.INVISIBLE
                        waitDialog!!.getButton(AlertDialog.BUTTON_NEUTRAL).visibility = View.VISIBLE
                        isManualPause = false
                        waitDialog!!.setMessage(mContext!!.getString(R.string.pls_wait))
                        ViewUtils.keepDialogOpen(waitDialog!!)

                        // 扫码期间，关照明
                        if (isScanningQRCode) thread { mSerialClientHumiture.lightToggle(EOnOff.Off) }
                    }

                    Dialog.BUTTON_NEUTRAL  // 暂停按钮
                    ->
                    {
                        waitDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.VISIBLE
                        waitDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.VISIBLE
                        waitDialog!!.getButton(AlertDialog.BUTTON_NEUTRAL).visibility = View.INVISIBLE
                        isManualPause = true
                        waitDialog!!.setMessage(mContext!!.getString(R.string.manual_pause))
                        ViewUtils.keepDialogOpen(waitDialog!!)

                        // 扫码期间，开照明
                        if (isScanningQRCode) thread { mSerialClientHumiture.lightToggle(EOnOff.On) }
                    }

                    Dialog.BUTTON_POSITIVE // 终止按钮
                    ->
                    {
                        isManualStop = true
                        mHandlerMain.post(rnHideWaitDialog)

                        // 扫码期间，开照明
                        if (isScanningQRCode)
                        {
                            thread { mSerialClientHumiture.lightToggle(EOnOff.On) }
                            isScanningQRCode = false
                        }
                    }
                }
            }

            waitDialog = AlertDialog.Builder(mContext).setPositiveButton(mContext!!.getString(R.string.stop), dialogOnclickListener).setNegativeButton(mContext!!.getString(R.string.goon), dialogOnclickListener).setNeutralButton(mContext!!.getString(R.string.pause), dialogOnclickListener).create()

            waitDialog!!.setMessage(mContext!!.getString(R.string.pls_wait))

            // 暂时FragmentDialog失去焦点，弹出FragmentDialog后，永久隐藏navigation bar. edited by liuja 2017-9-26
            waitDialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

            waitDialog!!.setCancelable(false)

            waitDialog!!.setOnDismissListener {
                // 恢复控件点击事件
                val view = (mContext as Activity).findViewById<FrameLayout>(R.id.framelayout_container)
                ViewUtils.setSubControlsClickable(view, true)
                val layoutParamsGroup = (mContext as Activity).findViewById<LinearLayout>(R.id.layout_params_group)
                layoutParamsGroup.isEnabled = true
            }
            waitDialog!!.setOnShowListener {
                // 禁止控件点击事件
                val view = (mContext as Activity).findViewById<FrameLayout>(R.id.framelayout_container)
                ViewUtils.setSubControlsClickable(view, false)
                val layoutParamsGroup = (mContext as Activity).findViewById<LinearLayout>(R.id.layout_params_group)
                layoutParamsGroup.isEnabled = false
            }

            waitDialog!!.show()

            changeAlertdialogTextSize(waitDialog!!)
            waitDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.INVISIBLE
            waitDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.INVISIBLE
        }

        // 串口通信
        val mSerialClientHumiture = SerialClientHumiture()
        val mSerialClientScanner = SerialClientScanner()

        // 是否首次进入主界面
        var isFirstIn = true
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        // 延时执行网络连接，避免硬件还没准备好
        mHandlerMain.postDelayed({
            // 连接网络
            mDataCommunication = DataCommunication(this)
            val connectThread = Thread(mDataCommunication)
            connectThread.start()

            // 打开温湿度串口
            mSerialClientHumiture.openSerial()
            // 打开扫码模块串口
            mSerialClientScanner.openSerial()
        }, 1500)

        supportFragmentManager.beginTransaction().replace(R.id.framelayout_container, FragmentMain.newInstance(), "FragmentMain").commit()
    }

    override fun onResume()
    {
        super.onResume()

        // 运行线程handler
        val mHandlerRunThread = HandlerThread("mHandlerRunThread")
        mHandlerRunThread.start()
        val mHandlerRunThreadLooper = mHandlerRunThread.looper
        mHandlerRunTh = object : Handler(mHandlerRunThreadLooper)
        {}

        // 操作线程handler
        val mHandlerOperationThread = HandlerThread("mHandlerOperationThread")
        mHandlerOperationThread.start()
        val mHandlerOperationThreadLooper = mHandlerOperationThread.looper
        mHandlerOperationTh = object : Handler(mHandlerOperationThreadLooper)
        {}

        //  动态请求权限
        requestPermission()

        // 加载所有参数
        loadAllParams()
    }

    override fun onDestroy()
    {
        super.onDestroy()

        mDataCommunication.closeConnection()

        if (mSerialClientHumiture.isConnected) mSerialClientHumiture.closeSerial()

        // 扫码模块
        if (mSerialClientScanner.isConnected) mSerialClientScanner.closeSerial()
    }

    /**
     *  设置拦截扫描枪二维码
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean
    {
        // 判断是否为主页面，避免输入其他EditText控件输入影响扫码枪接收
        val myFragment = fragmentManager.findFragmentByTag("FragmentMain")
        if (myFragment != null)
        {
            val c = event.unicodeChar
            if (c == 10) // 回车结束
            {
                barcodeFromScannerGun = barcodeFromScannerGun.trim()
                if (barcodeFromScannerGun != "")
                {
                    if (barcodeScanListener != null)
                    {
                        /**
                         * 二维码、条码识别长度
                         * 逻辑：
                         * ①识别结果长度少于或等于设置的长度，直接采用；
                         * ②识别结果长度大于设置的长度，截取设置的长度再采用
                         */
                        if (barcodeFromScannerGun.count() > ParamsHelper.paramGeneralParams.qrCodeScanLength) barcodeFromScannerGun = barcodeFromScannerGun.substring(0, ParamsHelper.paramGeneralParams.qrCodeScanLength)

                        barcodeScanListener?.onBarcodeScanCallback(barcodeFromScannerGun) // 回调
                        barcodeFromScannerGun = ""
                    }
                }
            }
            else
            {
                val singleChar = c.toChar()
                if (event.action == 0 && singleChar != '\u0000') // 排除char为null的情况
                {
                    barcodeFromScannerGun += singleChar
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    /**
     * 动态请求权限
     * @author Noddy
     */
    private fun requestPermission()
    {
        val permissionsPerms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(this, *permissionsPerms))
        {
            EasyPermissions.requestPermissions(this, getString(R.string.info_permission_camera), 0, *permissionsPerms)
        }
        else
        {
            // 配置日志
            LogHelper.configLog()
            LogHelper.init(this)
        }
    }
}
