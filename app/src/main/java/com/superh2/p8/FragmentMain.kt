package com.superh2.p8

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.suke.widget.SwitchButton
import com.superh2.library.myEntityCommon.Barcode
import com.superh2.library.myEntityCommon.One2More
import com.superh2.library.myEntityJson.MethodParams
import com.superh2.library.myEntityJson.Position
import com.superh2.library.myEnum.*
import com.superh2.library.myException.BarcodeException
import com.superh2.library.myException.InitException
import com.superh2.library.myException.ManualStopException
import com.superh2.library.myException.TimeOutException
import com.superh2.library.myInterface.*
import com.superh2.library.myView.CircleView
import com.superh2.library.myView.SlideView
import com.superh2.library.utils.*
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.library.utils.ParamsHelper.paramPosDispense
import com.superh2.library.utils.ParamsHelper.paramPosFixative
import com.superh2.library.utils.ParamsHelper.paramPosSpray
import com.superh2.library.utils.ParamsHelper.paramPosTips
import com.superh2.p8.MainActivity.Companion.isFirstIn
import com.superh2.p8.MainActivity.Companion.isManualPause
import com.superh2.p8.MainActivity.Companion.isManualStop
import com.superh2.p8.MainActivity.Companion.isProcedureRunning
import com.superh2.p8.MainActivity.Companion.isScanningQRCode
import com.superh2.p8.MainActivity.Companion.mContext
import com.superh2.p8.MainActivity.Companion.mHandlerMain
import com.superh2.p8.MainActivity.Companion.mHandlerOperationTh
import com.superh2.p8.MainActivity.Companion.mHandlerRunTh
import com.superh2.p8.MainActivity.Companion.mSerialClientHumiture
import com.superh2.p8.MainActivity.Companion.mSerialClientScanner
import com.superh2.p8.MainActivity.Companion.rnHideWaitDialog
import com.superh2.p8.MainActivity.Companion.rnShowWaitDialog
import com.superh2.p8.database.DBHelper
import com.superh2.p8.database.RunStateEntity
import com.superh2.p8.databinding.FragmentMainBinding
import com.superh2.p8.dialogs.*
import com.superh2.p8.utils.CmdHelper
import com.superh2.p8.utils.CmdHelper.ResetYAndWSpeed
import com.superh2.p8.utils.ViewUtils
import com.superh2.p8.utils.ViewUtils.exitCurrentLauncher
import com.superh2.p8.utils.ViewUtils.fullScreen
import com.superh2.p8.utils.ViewUtils.showWarningDialogAutoPause
import kotlinx.coroutines.cancel
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread


/**
 *@Description 主界面（增加：①先吸放玻片，再扫码；②扫码不成功手动修正）
 *@Author  Noddy
 */
class FragmentMain : FragmentBase<FragmentMainBinding>(FragmentMainBinding::inflate), BarcodeScanListener, OnClickListener, OnLongClickListener
{
    private val TAG = "FragmentMain"

    // 扫描枪扫描二维码
    private var barcodeFromScannerGunLast = "" // 上次扫描的二维码
    private var alreadyScannedTubeListFromScannerGun: MutableList<Barcode> = mutableListOf()
    private var currentScannedIndexFromScannerGun = 0 // 当前摆放位置

    // 摄像头扫描二维码（即扫码玻片扫码模块）
    private var alreadyScannedBarcodeFromCamera: MutableList<Barcode> = mutableListOf()

    // 二维码运行模式（默认 或者 修正模式）
    private var barcodeRunMode = EBarcodeRunMode.Default

    // 运行相关
    var currentScannedBarcode = "" // 当前摄像头扫描的玻片二维码
    var currentTakeTipIndex = 0 // 当前取tip index

    // 执行玻片底板板数计数器
    var baseCountTick = 0     //  默认每个玻片底板有64块玻片：如果少于64块，就提前结束；如果多于64块，就提示是否还有另一玻片底板

    // 温湿度查询定时器状态
    private lateinit var mRefreshTimerHumiture: Timer
    private var isRefreshHumiture = true // 是否刷新温湿度

    // 64个试管孔
    var listTubeHole64 = ArrayList<CircleView>(64)

    // 64个玻片
    var listSlide64 = ArrayList<SlideView>(64)

    companion object
    {
        fun newInstance() = FragmentMain()

        // 当前选择参数组
        var selectedMethodParams: MethodParams = MethodParams()
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        (context as MainActivity).barcodeScanListener = this // 设置二维码枪扫描监听回调
    }

    override fun onDetach()
    {
        super.onDetach()
        (context as MainActivity).barcodeScanListener = null // 清空二维码枪扫描监听回调
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        // 取消所有协程
        DBHelper.coroutineScope.cancel()
    }

    // 扫描枪二维码扫描回调
    override fun onBarcodeScanCallback(barcode: String)
    {
        // 禁用二维码时，提示
        if (selectedMethodParams.barcode == EOnOff.Off)
        {
            Toast.makeText(mActivity, getString(R.string.info_barcode_reader_not_available_while_off), Toast.LENGTH_SHORT).show()
            return
        }

        // 程序处于非运行状态，才识别二维码
        if (!isProcedureRunning && barcodeFromScannerGunLast != barcode) // 避免扫描枪同时扫描多次同一个试管
        {
            barcodeFromScannerGunLast = barcode
            val existBarcode = alreadyScannedTubeListFromScannerGun.find { barcode == it.barcode }
            if (existBarcode == null)  // 排除重复的二维码
            {
                // 发送试管摆放指示灯指令（位置 = index+1）
                val lightTubePosition = currentScannedIndexFromScannerGun + 1
                // 闪烁试管孔
                refreshTubeHoleStatus(currentScannedIndexFromScannerGun, true, false)

                /**
                 * 试管二维码手动确认（即不需要检测底座）
                 */
                if (paramGeneralParams.tubeBaseManualCheck == EOnOff.On)
                {
                    // 刷新试管孔状态信息
                    refreshTubeHoleStatus(currentScannedIndexFromScannerGun, isBlink = false, isFill = true)
                    // 试管已摆放（且更新currentScannedIndexFromScannerGun）
                    addTubeBarcode(barcode)
                }
                /**
                 * 试管二维码自动确认
                 */
                else
                {
                    CmdHelper.lightTubeLed(lightTubePosition, object : ICommandResultCallback
                    {
                        override fun success()
                        {
                            // 弹出框提示用户摆放试管到底座
                            var rnCheckTubeLed: Runnable? = null
                            var dialogPromptPutTube = DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_pls_put_tube_correct_to_tubes_base), getString(R.string.cancel), 300, 100, object : InfoPromptListener
                            {
                                override fun clicked()
                                {
                                    if (rnCheckTubeLed != null) mHandlerOperationTh.removeCallbacks(rnCheckTubeLed!!)

                                    // 取消试管摆放提示（用户手动操作）
                                    mHandlerMain.post {
                                        refreshTubeHoleStatus(currentScannedIndexFromScannerGun, isBlink = false, isFill = false)
                                        barcodeFromScannerGunLast = ""
                                    }
                                }
                            })
                            dialogPromptPutTube.show(parentFragmentManager, null)

                            // 不断发送检测试管摆放指令，直到检测有为止
                            rnCheckTubeLed = Runnable {
                                Log.i(TAG, "发送检测试管指令......")
                                CmdHelper.checkTubeLed(lightTubePosition, object : ICommandResultCallback
                                {
                                    override fun success()
                                    {
                                        Log.i(TAG, "检测成功，有试管！！！")
                                        mHandlerOperationTh.removeCallbacks(rnCheckTubeLed!!)
                                        if (dialogPromptPutTube.isShow) mHandlerMain.post {
                                            // 关闭弹窗
                                            dialogPromptPutTube.dismissDialog()

                                            // 刷新试管孔状态信息
                                            refreshTubeHoleStatus(currentScannedIndexFromScannerGun, isBlink = false, isFill = true)
                                            // 试管已摆放（且更新currentScannedIndexFromScannerGun）
                                            addTubeBarcode(barcode)
                                        }
                                    }

                                    override fun fail(ex: Exception)
                                    {
                                    }
                                })
                                mHandlerOperationTh.postDelayed(rnCheckTubeLed!!, 500)
                            }
                            mHandlerOperationTh.postDelayed(rnCheckTubeLed, 10)

                        }

                        override fun fail(ex: Exception)
                        {
                        }
                    })
                }

            }
        }
    }

    override fun onResume()
    {
        super.onResume()

        isRefreshHumiture = true

        // 当前选择参数组
        refreshSelectedParamsGroup()

        // 先查询一次温湿度开启状态
        mSerialClientHumiture.queryStatus(null)
        binding.switchbtnToggleHumiturePower.isChecked = mSerialClientHumiture.isSysStart
        binding.switchbtnToggleHumiturePower.tag = false
        binding.switchbtnToggleHumiturePower.setOnCheckedChangeListener(switchButtonOnCheckedChangeListener)

        // 分散指数
        binding.spinnerDispersancyIndex.setSelection(paramGeneralParams.DispersancyIndexIndex)
        binding.switchbtnDispersancyIndex.isChecked = paramGeneralParams.DispersancyIndexEnable
        binding.switchbtnDispersancyIndex.setOnCheckedChangeListener(switchButtonOnCheckedChangeListener)

        binding.tvEnvironmentTemp.text = mSerialClientHumiture.tempValueRealTime.toString()
        binding.tvRelativeHumidity.text = mSerialClientHumiture.humValueRealTime.toString()

        // 默认打开照明
        toggleLight(EOnOff.On, runImmediately = true)

        // 不断查询温湿度
        refreshHumiture()

        if (isFirstIn)
        {
            // 复位
            thread {
                sleep(1500)
                CmdHelper.initMotor()
            }

            /**
             * 数据库相关（检测上次是否异常退出程序）
             */
            // 初始化数据库
            DBHelper.initDB()
            // 检测是否上次异常退出程序
            checkForInterruptedRun()

            isFirstIn = false
        }

        // 测试函数
//        test()
    }

    /**
     * 测试函数
     */
    private fun test()
    {
        var tsec = 300
        val msec = String.format("%04d", tsec * 100)
        Log.i(TAG, "测试时间格式化前：$tsec，格式化后：AO5$msec")

//        for (i in 0..63)
//        {
//            var barcodeContent = "slide" + (i + 1)
//            alreadyScannedBarcodeFromCamera.add(Barcode(barcodeContent, i, isWrong = false))
//        }
//        DialogFragment_Slide_Frame.newInstance().setDialogContent(context, getString(R.string.question_makesure_slide_frame_scan_completed), getString(R.string.confirm), getString(R.string.rescan), getString(R.string.cancel), alreadyScannedBarcodeFromCamera, object : SlideFrameConfirmListener
//        {
//            override fun clickedOk()
//            {
//                Toast.makeText(mContext, "点击确认了", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun clickedRescan()
//            {
//                Toast.makeText(mContext, "点击继续了", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun clickedCancel()
//            {
//                Toast.makeText(mContext, "点击取消了", Toast.LENGTH_SHORT).show()
//            }
//        }).show(fragmentManager, null)


//        var oldIndex = if(runState.isBarcodeMode) runState.slideIndex else runState.tubeIndex
//        var oldItemStr = if(runState.isBarcodeMode) getString(R.string.slide_pos_interrupted) else getString(R.string.tube_pos_interrupted)
//        var newItemStr = if(runState.isBarcodeMode) getString(R.string.slide_pos_continued) else getString(R.string.tube_pos_continued)
        DialogFragment_Interrupted_Selector.newInstance().setDialogContent(5, getString(R.string.tube_pos_interrupted), getString(R.string.tube_pos_continued), getString(R.string.yes), getString(R.string.no), object : INewIndexSelectedListener
        {
            override fun confirm(selectedIndex: Int)
            {
                // TODO 再次确认是否运行

                fullScreen(activity)
                DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_confirm_recovery_again), getString(R.string.yes), getString(R.string.no), true, false, object : WarnPromptListener
                {
                    override fun clickedOk()
                    {
                        fullScreen(activity)
                    }

                    override fun clickedCancel()
                    {
                        fullScreen(activity)
                    }
                }).show(parentFragmentManager, null)

            }

            override fun cancel()
            {
                fullScreen(activity)
            }
        }).show(parentFragmentManager, null)
    }

    override fun onPause()
    {
        super.onPause()

        // 关闭温湿度查询定时器
        isRefreshHumiture = false
        mRefreshTimerHumiture.cancel()
    }

    /**
     * 刷新温湿度
     */
    private fun refreshHumiture()
    {
        resetHumiture()

        // 查询定时器
        mRefreshTimerHumiture = fixedRateTimer("humiture-getter-timer", false, 1000, 2200) {
            if (mSerialClientHumiture.isConnected)
            {
                showHumitureSet()
                showHumiture()
                checkAutoRun()
            }
        }
    }

    override fun initWidget()
    {
        // 试管
        for (i in 0..63)
        {
            var tube = CircleView(requireActivity())
            tube.text = (i + 1).toString()
            val row = i / 8
            val col = i % 8
            binding.gridLayoutTube.addView(tube, GridLayout.LayoutParams(GridLayout.spec(row, GridLayout.CENTER), GridLayout.spec(col, GridLayout.CENTER)))

            listTubeHole64.add(tube)
        }

        // 玻片
        for (posIndex in 0..63)
        {
            var slide = SlideView(requireActivity())
            slide.text = (posIndex + 1).toString()
            val row = posIndex / 16
            val col = posIndex % 16
            binding.gridLayoutSlide.addView(slide, GridLayout.LayoutParams(GridLayout.spec(row, GridLayout.CENTER), GridLayout.spec(col, GridLayout.CENTER)))

            listSlide64.add(slide)

            slide.setOnClickListener {
                // 纠正玻片二维码
                correctSlideBarcode(posIndex)
            }
        }

        // 分散指数
        val dispersancy_index_array = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18")
        val adapter: ArrayAdapter<String> = ArrayAdapter(mContext!!, android.R.layout.simple_spinner_item, dispersancy_index_array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDispersancyIndex.adapter = adapter
        binding.spinnerDispersancyIndex.onItemSelectedListener = object : OnItemSelectedListener
        {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long)
            {
                paramGeneralParams.DispersancyIndexIndex = position
                FileUtils.saveGeneralParameters(paramGeneralParams, true)

                resetHumiture()
                showHumitureSet()

                fullScreen(activity)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?)
            {
                fullScreen(activity)
            }
        }

        // 顶部参数组
        val layoutParamsGroup = mActivity?.findViewById(R.id.layout_params_group) as LinearLayout
        layoutParamsGroup.visibility = View.VISIBLE

        val btnReturn = mActivity?.findViewById(R.id.btnReturn) as Button
        btnReturn.visibility = View.GONE
        val viewExit = mActivity?.findViewById(R.id.view_exit) as View
        viewExit.setOnLongClickListener(this)

        (mActivity?.findViewById<SwitchButton>(R.id.switch_auto_run)!!).setOnCheckedChangeListener(switchButtonOnCheckedChangeListener)
        layoutParamsGroup.setOnClickListener(this)
        binding.btnMethodParameters.setOnClickListener(this)

        binding.btnTempSet.setOnClickListener(this)
        binding.btnHumSet.setOnClickListener(this)

        binding.btnRun.setOnClickListener(this)
        binding.btnScanTubeBarcode.setOnClickListener(this)
        binding.btnHardwareIo.setOnClickListener(this)
        binding.btnHome.setOnClickListener(this)
    }

    /**
     * 检测是否上次异常退出程序
     */
    private fun checkForInterruptedRun()
    {
        val runState = DBHelper.getLatestRunState()
        when
        {
            // 无记录，全新启动
            runState == null ->
            {

            }
            // 异常中断状态，提示恢复
            runState.isInterrupted ->
            {
                mHandlerMain.post {
                    showRecoveryDialog(runState)
                }
            }
            // 正常结束的状态
            else ->
            {

            }
        }
    }

    /**
     * 显示恢复对话框
     * @param runState 运行状态
     */
    private fun showRecoveryDialog(runState: RunStateEntity)
    {
        DialogFragment_Warn_Prompt.newInstance().setDialogContent(getString(R.string.info_recovery), getString(R.string.question_makesure_recovery), getString(R.string.yes), getString(R.string.no), true, false, object : WarnPromptListener
        {
            override fun clickedOk()
            {
                // 区分扫码和不扫码弹出框显示内容
                var oldIndex = if (runState.isBarcodeMode) runState.slideIndex else runState.tubeIndex
                var oldItemStr = if (runState.isBarcodeMode) getString(R.string.slide_pos_interrupted) else getString(R.string.tube_pos_interrupted)
                var newItemStr = if (runState.isBarcodeMode) getString(R.string.slide_pos_continued) else getString(R.string.tube_pos_continued)

                DialogFragment_Interrupted_Selector.newInstance().setDialogContent(oldIndex, oldItemStr, newItemStr, getString(R.string.goon), getString(R.string.cancel), object : INewIndexSelectedListener
                {
                    override fun confirm(selectedIndex: Int)
                    {
                        fullScreen(activity)
                        DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_confirm_recovery_again), getString(R.string.yes), getString(R.string.no), true, false, object : WarnPromptListener
                        {
                            override fun clickedOk()
                            {
                                fullScreen(activity)
                                // 赋值给alreadyScannedTubeListFromScannerGun 和 alreadyScannedBarcodeFromCamera
                                if (runState.scannedTubesJson != null)
                                {
                                    alreadyScannedTubeListFromScannerGun = JsonParamHelper.jsonToBarcodes(runState.scannedTubesJson).toMutableList()
                                    // 刷新试管孔状态
                                    alreadyScannedTubeListFromScannerGun.forEach {
                                        refreshTubeHoleStatus(it.posIndex, isBlink = false, isFill = true)
                                    }
                                }
                                if (runState.isBarcodeMode && runState.scannedSlidesJson != null)
                                {
                                    alreadyScannedBarcodeFromCamera = JsonParamHelper.jsonToBarcodes(runState.scannedSlidesJson).toMutableList()
                                    // 刷新玻片状态
                                    alreadyScannedBarcodeFromCamera.forEach {
                                        refreshSlideStatus(it.posIndex, ESlideStatus.Scanned)
                                    }
                                }
                                // 执行程序
                                runProcedure(runState.slideIndex, runState.tubeIndex, true)
                            }

                            override fun clickedCancel()
                            {
                                fullScreen(activity)
                                // 用户取消恢复，标记程序为正常完成
                                DBHelper.markRunCompleted(false)
                            }
                        }).show(parentFragmentManager, null)
                    }

                    override fun cancel()
                    {

                    }
                }).show(parentFragmentManager, null)
            }

            override fun clickedCancel()
            {
                // 用户取消恢复，标记程序为正常完成
                DBHelper.markRunCompleted(false)
            }
        }).show(parentFragmentManager, null)
    }

    /**
     * 显示温湿度数据
     */
    private fun showHumiture()
    {
        // 系统状态
        if (isRefreshHumiture)
        {
            mSerialClientHumiture.queryStatus(object : ICommandResultCallback
            {
                override fun success()
                {
                    if (isRefreshHumiture)
                    {
                        mHandlerMain.post {
                            if (binding.switchbtnToggleHumiturePower.isChecked != mSerialClientHumiture.isSysStart)
                            {
                                binding.switchbtnToggleHumiturePower.tag = true
                                binding.switchbtnToggleHumiturePower.isChecked = mSerialClientHumiture.isSysStart
                            }
                        }
                    }
                }

                override fun fail(ex: Exception)
                {
                }

            })
        }
        sleep(200)

        // 实时温度
        if (isRefreshHumiture)
        {
            mSerialClientHumiture.queryData(ESerialOperation.TempRealTime, object : ICommandResultCallback
            {
                override fun success()
                {
                    if (isRefreshHumiture)
                    {
                        mHandlerMain.post {
                            binding.tvEnvironmentTemp.text = mSerialClientHumiture.tempValueRealTime.toString()
                        }
                    }
                }

                override fun fail(ex: Exception)
                {
                }
            })
        }
        sleep(200)

        // 实时湿度
        if (isRefreshHumiture)
        {
            mSerialClientHumiture.queryData(ESerialOperation.HumRealTime, object : ICommandResultCallback
            {
                override fun success()
                {
                    if (isRefreshHumiture)
                    {
                        mHandlerMain.post {
                            binding.tvRelativeHumidity.text = mSerialClientHumiture.humValueRealTime.toString()
                        }
                    }
                }

                override fun fail(ex: Exception)
                {
                }
            })
        }
        sleep(200)

        // 温湿度设定值
        if (isRefreshHumiture) mSerialClientHumiture.queryData(ESerialOperation.TempSet, null)
        sleep(200)

        if (isRefreshHumiture) mSerialClientHumiture.queryData(ESerialOperation.HumSet, null)
        sleep(200)

        // 开门报警状态
        if (isRefreshHumiture && paramGeneralParams.alarmDoor == EOnOff.On) mSerialClientHumiture.queryData(ESerialOperation.AlarmDoor, null)
        sleep(200)
    }

    /**
     * 显示温湿度设置值
     */
    private fun showHumitureSet()
    {
        mHandlerMain.post {
            // 温湿度设定值（如果分散指数使能，就显示该分散指数值，否则，显示单独设置的值）
            var tempSet = 0.0f
            var humiSet = 0.0f
            if (binding.switchbtnDispersancyIndex.isChecked)
            {
                tempSet = paramGeneralParams.DispersancyIndexList[this.binding.spinnerDispersancyIndex.selectedItemPosition].temp
                humiSet = paramGeneralParams.DispersancyIndexList[this.binding.spinnerDispersancyIndex.selectedItemPosition].humi
            }
            else
            {
                tempSet = selectedMethodParams.paramsSlideMode.temp
                humiSet = selectedMethodParams.paramsSlideMode.hum
            }
            binding.btnTempSet.text = "$tempSet°C"
            binding.btnHumSet.text = "$humiSet%"
        }
    }

    /**
     * 刷新选择的参数组
     */
    private fun refreshSelectedParamsGroup()
    {
        selectedMethodParams = paramMethodParamsGroup.methodParamsGroup[paramMethodParamsGroup.selectedGroupIndex]
        mActivity?.findViewById<TextView>(R.id.tv_selected_params_group)?.text = selectedMethodParams.groupName

        showHumitureSet()
    }

    /**
     * 刷新试管孔状态
     * @param holeIndex 孔index
     * @param isBlink 是否闪烁
     * @param isFill 是否填充
     */
    private fun refreshTubeHoleStatus(holeIndex: Int, isBlink: Boolean, isFill: Boolean)
    {
        listTubeHole64[holeIndex].isBlink = isBlink
        listTubeHole64[holeIndex].isFill = isFill
    }

    /**
     * 复位全部已扫描试管状态信息
     */
    private fun initAllTubeHoleStatus()
    {
        barcodeFromScannerGunLast = ""
        alreadyScannedTubeListFromScannerGun.clear()
        currentScannedIndexFromScannerGun = 0
        baseCountTick = 0
        listTubeHole64.forEach { v ->
            v.isBlink = false
            v.isFill = false
        }
    }

    /**
     * 刷新玻片状态
     * @param slideIndex 玻片index
     * @param slideStatus 玻片状态
     */
    private fun refreshSlideStatus(slideIndex: Int, slideStatus: ESlideStatus)
    {
        listSlide64[slideIndex].slideStatus = slideStatus
    }

    /**
     * 复位全部已滴片玻片状态信息
     */
    private fun initAllSlideStatus()
    {
        listSlide64.forEach { v ->
            v.slideStatus = ESlideStatus.Default
        }

        alreadyScannedBarcodeFromCamera.clear()
        barcodeRunMode = EBarcodeRunMode.Default
        baseCountTick = 0
    }

    /**
     * 添加试管二维码
     * @param code 二维码
     */
    private fun addTubeBarcode(code: String)
    {
        alreadyScannedTubeListFromScannerGun.add(Barcode(code, currentScannedIndexFromScannerGun))
        currentScannedIndexFromScannerGun++
    }

    override fun onClick(v: View)
    {
        when (v.id)
        {
            R.id.btn_temp_set -> // 温度设置
            {
                if (binding.switchbtnDispersancyIndex.isChecked)
                {
                    DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_disabled_dispersancy_index), getString(R.string.ok), 0, 0, object : InfoPromptListener
                    {
                        override fun clicked()
                        {
                        }
                    }).show(parentFragmentManager, null)
                    return
                }

                DialogFragment_Params_Setting_Decimal.newInstance().setDialogParams(getString(R.string.environment_temp), getString(R.string.temp_20_45), 2, selectedMethodParams.paramsSlideMode.temp, 20f, 50f, object : ParamsDecimalSetListener
                {
                    override fun valueSetCompleted(number: Float)
                    {
                        selectedMethodParams.paramsSlideMode.temp = number
                        FileUtils.saveMethodParametersGroup(paramMethodParamsGroup, true)
                        resetHumiture()
                        showHumitureSet()
                    }
                }).show(parentFragmentManager, null)
            }

            R.id.btn_hum_set -> // 湿度设置
            {
                if (binding.switchbtnDispersancyIndex.isChecked)
                {
                    DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_disabled_dispersancy_index), getString(R.string.ok), 0, 0, object : InfoPromptListener
                    {
                        override fun clicked()
                        {
                        }
                    }).show(parentFragmentManager, null)
                    return
                }

                DialogFragment_Params_Setting_Decimal.newInstance().setDialogParams(getString(R.string.relative_humidity), getString(R.string.humidity_35_60), 2, selectedMethodParams.paramsSlideMode.hum, 0f, 100f, object : ParamsDecimalSetListener
                {
                    override fun valueSetCompleted(number: Float)
                    {
                        selectedMethodParams.paramsSlideMode.hum = number
                        FileUtils.saveMethodParametersGroup(paramMethodParamsGroup, true)
                        resetHumiture()
                        showHumitureSet()
                    }
                }).show(parentFragmentManager, null)
            }
            // 方法参数
            R.id.btn_method_parameters ->
            {
                replaceFragment(FragmentMethodParametersContainer.newInstance(), "FragmentMethodParametersContainer")
            }
            // 运行
            R.id.btn_run ->
            {
                // 运行前，检查开门报警是否使能，如果使能，保证关门才运行
                if (paramGeneralParams.alarmDoor == EOnOff.On && mSerialClientHumiture.isAlarmDoor)
                {
                    DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_pls_run_before_close_door), getString(R.string.confirm), 0, 0, object : InfoPromptListener
                    {
                        override fun clicked()
                        {
                        }
                    }).show(parentFragmentManager, null)
                    return
                }

                // 正常模式
                if (barcodeRunMode == EBarcodeRunMode.Default)
                {
                    // 是否已经扫试管二维码
                    if (alreadyScannedTubeListFromScannerGun.size == 0 && selectedMethodParams.barcode == EOnOff.On)
                    {
                        DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_pls_scan_tube_barcode_first), getString(R.string.cancel), 0, 0, object : InfoPromptListener
                        {
                            override fun clicked()
                            {
                            }
                        }).show(parentFragmentManager, null)

                        return
                    }

                    DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_makesure_run_procedure), getString(R.string.yes), getString(R.string.no), true, true, object : WarnPromptListener
                    {
                        override fun clickedOk()
                        {
                            sleep(20)
                            // 所需枪头数目
                            var tipCountNeed: Int? = null
                            if (selectedMethodParams.barcode == EOnOff.On) tipCountNeed = selectedMethodParams.slideQuantity
                            else
                            {
                                tipCountNeed = selectedMethodParams.tubeQuantity
                                if (tipCountNeed > 96) tipCountNeed = null
                            }
                            // 选择枪头起始位置
                            DialogFragment_TipBox_Selector.newInstance().setDialogContent(currentTakeTipIndex, tipCountNeed, object : TipBoxSelectorListener
                            {
                                override fun clickedOk(index: Int)
                                {
                                    currentTakeTipIndex = index

                                    // 禁用二维码扫描，提示用户一一摆放好试管和玻片
                                    if (selectedMethodParams.barcode == EOnOff.Off)
                                    {
                                        DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_barcode_reader_not_available), getString(R.string.confirm), 0, 0, object : InfoPromptListener
                                        {
                                            override fun clicked()
                                            {
                                                runProcedure(0, 0, isInterrupted = false)
                                            }
                                        }).show(parentFragmentManager, null)
                                    }
                                    else runProcedure(0, 0, isInterrupted = false)
                                }

                                override fun clickedCancel()
                                {
                                }
                            }).show(parentFragmentManager, null)
                        }

                        override fun clickedCancel()
                        {
                        }
                    }).show(parentFragmentManager, null)
                }
                // 二维码纠正模式
                else if (barcodeRunMode == EBarcodeRunMode.Correct)
                {
                    // 判断所有二维码是否纠正完成
                    var wrongBarcodeCount = alreadyScannedBarcodeFromCamera.count { barcode -> barcode.isWrong }
                    // 还有错误二维码
                    if (wrongBarcodeCount > 0)
                    {
                        // 还有哪些错误二维码没有纠正
                        var whichStr = ""
                        alreadyScannedBarcodeFromCamera.forEach {
                            if (it.isWrong) whichStr = whichStr + (it.posIndex + 1) + ","
                        }
                        Toast.makeText(mActivity, "These slide QR codes have not been corrected：$whichStr", Toast.LENGTH_LONG).show()
                        DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_click_red_slide_and_correct_qr_code), getString(R.string.ok), 0, 0, object : InfoPromptListener
                        {
                            override fun clicked()
                            {
                            }
                        }).show(parentFragmentManager, null)

                    }
                    // 已经全部纠正
                    else
                    {
                        // 提示继续执行程序
                        DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_makesure_run_procedure_continue), getString(R.string.yes), getString(R.string.no), true, true, object : WarnPromptListener
                        {
                            override fun clickedOk()
                            {
                                runProcedure(0, 0, isInterrupted = false)
                            }

                            override fun clickedCancel()
                            {
                            }
                        }).show(parentFragmentManager, null)
                    }
                }
            }

            R.id.btn_scan_tube_barcode ->
            {
                DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_makesure_rescan_tube_barcode), getString(R.string.yes), getString(R.string.no), true, false, object : WarnPromptListener
                {
                    override fun clickedOk()
                    {
                        // 初始化玻片、试管状态信息
                        initAllSlideStatus()
                        initAllTubeHoleStatus()

                        Toast.makeText(mActivity, getString(R.string.info_pls_scan_tube_barcode_first), Toast.LENGTH_SHORT).show()
                    }

                    override fun clickedCancel()
                    {
                    }
                }).show(parentFragmentManager, null)
            }

            R.id.layout_params_group ->
            {
                DialogFragment_Params_Groups_RadioGroup.newInstance().setDialogContent(getString(R.string.parameter_groups), getString(R.string.confirm), getString(R.string.cancel), object : ParamsGroupSetListener
                {
                    override fun clickedOk(index: Int)
                    {
                        if (paramMethodParamsGroup.selectedGroupIndex != index)
                        {
                            paramMethodParamsGroup.selectedGroupIndex = index
                            FileUtils.saveMethodParametersGroup(paramMethodParamsGroup, true)
                            refreshSelectedParamsGroup()

                            resetHumiture()
                        }
                    }

                }).show(parentFragmentManager, null)
            }
            // 硬件测试
            R.id.btn_hardware_io ->
            {
                replaceFragment(FragmentHardwareIO.newInstance(), "FragmentHardwareIO")
            }

            R.id.btn_home ->
            {
                if (!isProcedureRunning)
                {
                    try
                    {
                        // 复位手动停止属性
                        isManualPause = false
                        isManualStop = false
                        thread { CmdHelper.initMotor() }
                    }
                    catch (e: InitException)
                    {

                    }
                }
            }
        }
    }

    override fun onLongClick(p0: View): Boolean
    {
        when (p0.id)
        {
            // 长按左上角退出程序
            R.id.view_exit ->
            {
                exitCurrentLauncher(mContext)
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
        return true
    }

    /**
     * toggle监听器
     */
    var switchButtonOnCheckedChangeListener = SwitchButton.OnCheckedChangeListener { view, isChecked ->
        if (view == binding.switchbtnToggleHumiturePower)
        {
            // 用tag属性来控制代码切换状态还是界面手动切换状态
            if (view.tag != true)
            {
                isRefreshHumiture = false
                if (!mSerialClientHumiture.isSysStart) thread {
                    mSerialClientHumiture.powerToggle(EOnOff.On)
                    sleep(200)
                    isRefreshHumiture = true
                }
                else thread {
                    mSerialClientHumiture.powerToggle(EOnOff.Off)
                    sleep(200)
                    isRefreshHumiture = true
                }
            }
            else
            {
                view.tag = false
            }
        }
        else if (view == binding.switchbtnDispersancyIndex)
        {
            paramGeneralParams.DispersancyIndexEnable = isChecked
            FileUtils.saveGeneralParameters(paramGeneralParams, true)
            resetHumiture()
            showHumitureSet()
        }
        else if (view == (mActivity?.findViewById<SwitchButton>(R.id.switch_auto_run)!!))
        {
            var switch_auto_run = (mActivity?.findViewById<SwitchButton>(R.id.switch_auto_run)!!)

            // 提示自动运行前置条件
            if (isChecked)
            {
                // 运行前，检查开门报警是否使能，如果使能，保证关门才运行
                if (paramGeneralParams.alarmDoor == EOnOff.On && mSerialClientHumiture.isAlarmDoor)
                {
                    Toast.makeText(mActivity, getString(R.string.info_pls_run_before_close_door), Toast.LENGTH_SHORT).show()
                    Handler().post { switch_auto_run.isChecked = false }
                    return@OnCheckedChangeListener
                }
                // 正常模式
                if (barcodeRunMode == EBarcodeRunMode.Default)
                {
                    // 是否已经扫试管二维码
                    if (alreadyScannedTubeListFromScannerGun.size == 0 && selectedMethodParams.barcode == EOnOff.On)
                    {
                        Toast.makeText(mActivity, getString(R.string.info_pls_scan_tube_barcode_first), Toast.LENGTH_SHORT).show()
                        Handler().post { switch_auto_run.isChecked = false }
                        return@OnCheckedChangeListener
                    }
                }
                else
                {
                    currentTakeTipIndex = 0
                    Toast.makeText(mActivity, getString(R.string.info_fill_tipbox_and_tube_and_slide_well), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 重新设置温湿度值
     */
    private fun resetHumiture()
    {
        // 温湿度设定值（如果分散指数使能，就设置该分散指数值，否则，设置单独的值）
        var tempSet = 0.0f
        var humiSet = 0.0f
        if (binding.switchbtnDispersancyIndex.isChecked)
        {
            tempSet = paramGeneralParams.DispersancyIndexList[binding.spinnerDispersancyIndex.selectedItemPosition].temp
            humiSet = paramGeneralParams.DispersancyIndexList[binding.spinnerDispersancyIndex.selectedItemPosition].humi
        }
        else
        {
            tempSet = selectedMethodParams.paramsSlideMode.temp
            humiSet = selectedMethodParams.paramsSlideMode.hum
        }

        thread {
            isRefreshHumiture = false

            sleep(1000)

            if (tempSet != mSerialClientHumiture.tempValueSet)
            {
                mSerialClientHumiture.setHumiture(tempSet, ESerialOperation.SetTemp, null)
            }
            if (humiSet != mSerialClientHumiture.humValueSet)
            {
                sleep(300)
                mSerialClientHumiture.setHumiture(humiSet, ESerialOperation.SetHum, null)
            }

            isRefreshHumiture = true
        }
    }

    /**
     * 设置照明状态
     * @param onOff 开关
     * @param runImmediately 是否立即运行（即不sleep）
     */
    private fun toggleLight(onOff: EOnOff, runImmediately: Boolean)
    {
        isRefreshHumiture = false
        if (!runImmediately) sleep(1000)
        mSerialClientHumiture.lightToggle(onOff)
        if (!runImmediately) sleep(200)
        isRefreshHumiture = true
    }

    /**
     * 检查是否自动运行程序
     */
    private fun checkAutoRun()
    {
        // 温湿度稳定，是否自启动程序
        var autoRun = false
        mHandlerMain.post {
            autoRun = (mActivity?.findViewById<SwitchButton>(R.id.switch_auto_run)!!).isChecked
        }
        if (autoRun && !isProcedureRunning && mSerialClientHumiture.checkHumitureStability())
        {
            // 先复位自动运行
            mHandlerMain.post {
                (mActivity?.findViewById<SwitchButton>(R.id.switch_auto_run)!!).isChecked = false
            }
            runProcedure(0, 0, isInterrupted = false)
        }
    }

    /**
     * 运行程序
     * @param startSlideIndex 玻片开始索引，默认为0。异常中断再继续，需要设置这个值
     * @param tubeIndex 试管索引，默认为0。异常中断再继续，需要设置这个值
     * @param isInterrupted 是否是异常中断恢复运行
     */
    private fun runProcedure(startSlideIndex: Int, tubeIndex: Int, isInterrupted: Boolean)
    {
        mHandlerMain.post(rnShowWaitDialog)
        mHandlerRunTh.post(createRnDoWork(startSlideIndex, tubeIndex, isInterrupted))

        fullScreen(activity)

        // 运行时禁止点击底部按钮
        var view = activity?.window?.decorView as? FrameLayout ?: return
        com.superh2.library.utils.ViewUtils.setSubControlsClickable(view, false)

        // 初始化界面
        if (barcodeRunMode == EBarcodeRunMode.Default)
        {
            initAllSlideStatus()
        }
    }

    /**
     * 开始工作Runnable
     * @param startSlideIndex 玻片开始索引，默认为0。异常中断再继续，需要设置这个值
     * @param tubeIndex 试管索引，默认为0。异常中断再继续，需要设置这个值
     * @param isInterrupted 是否是异常中断恢复运行
     */
    private fun createRnDoWork(startSlideIndex: Int, tubeIndex: Int, isInterrupted: Boolean): Runnable
    {
        return Runnable {
            try
            {
                isManualPause = false
                isManualStop = false
                isProcedureRunning = true

                // 数据库插入新状态（新状态默认isInterrupted为false，等程序完成所有扫码动作才置true，因为默认扫码动作不在异常中断恢复范围内）
                if (!isInterrupted)
                {
                    DBHelper.insertRunState(selectedMethodParams.barcode == EOnOff.On, false)
                }

                // 默认模式
                if (barcodeRunMode == EBarcodeRunMode.Default)
                {
                    // 运行前先到退枪头位置执行一次退枪头，预防移液头已经插了枪头
                    actionReleaseTipNoQRCodeChannel()

                    CmdHelper.initMotor()

                    // 是否扫二维码
                    if (selectedMethodParams.barcode == EOnOff.On)
                    {
                        doWorkWithBarcodeScan(startSlideIndex, isInterrupted = isInterrupted)
                    }
                    else
                    {
                        doWorkWithoutBarcode(tubeIndex, isInterrupted = isInterrupted)
                    }
                }
                // 二维码修正模式
                else if (barcodeRunMode == EBarcodeRunMode.Correct)
                {
                    doWorkWithBarcodeScan(startSlideIndex, isInterrupted = isInterrupted)
                }

                CmdHelper.initMotor()

                mHandlerMain.post(rnHideWaitDialog)

                //  清空扫描枪二维码
                alreadyScannedTubeListFromScannerGun.clear()

                isProcedureRunning = false

                // 程序正常或者手动结束，标记程序为正常完成
                DBHelper.markRunCompleted(false)
            }
            // 二维码异常
            catch (e: BarcodeException)
            {
                LogHelper.error("抛出了二维码异常")
                // 让程序完全停止
                sleep(1000)
                // 复位手动停止属性
                isManualPause = false
                isManualStop = false
                CmdHelper.initMotor()

                // 弹出纠正二维码输入提示
                DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_click_red_slide_and_correct_qr_code), getString(R.string.ok), 0, 0, object : InfoPromptListener
                {
                    override fun clicked()
                    {
                        barcodeRunMode = EBarcodeRunMode.Correct
                    }
                }).show(parentFragmentManager, null)
            }
            // 中断异常
            catch (e: InterruptedException)
            {
                e.printStackTrace()

                // 程序正常或者手动结束，标记程序为正常完成
                DBHelper.markRunCompleted(false)
            }
            // 超时异常
            catch (e: TimeOutException)
            {
                e.printStackTrace()
                showWarningDialogAutoPause(getString(R.string.info_action_failed_click_stop_to_main_ui), false)

                // 程序正常或者手动结束，标记程序为正常完成
                DBHelper.markRunCompleted(false)
            }
            // 初始化异常
            catch (e: InitException)
            {
                e.printStackTrace()
                showWarningDialogAutoPause(getString(R.string.info_init_failed_click_stop_to_main_ui), false)

                // 程序正常或者手动结束，标记程序为正常完成
                DBHelper.markRunCompleted(false)
            }
            // 手动停止
            catch (e: ManualStopException)
            {
                e.printStackTrace()

                // 程序正常或者手动结束，标记程序为正常完成
                DBHelper.markRunCompleted(false)
            }
        }
    }
//    private val rnDoWork = Runnable {
//        try
//        {
//            isManualPause = false
//            isManualStop = false
//            isProcedureRunning = true
//
//            异常中断不用插入新状态
//            // 数据库插入新状态（新状态默认isInterrupted为false，等程序完成所有扫码动作才置true，因为默认扫码动作不在异常中断恢复范围内）
//            DBHelper.insertRunState(selectedMethodParams.barcode == EOnOff.On, false)
//
//            // 默认模式
//            if (barcodeRunMode == EBarcodeRunMode.Default)
//            {
//                // 运行前先到退枪头位置执行一次退枪头，预防移液头已经插了枪头
//                actionReleaseTipNoQRCodeChannel()
//
//                CmdHelper.initMotor()
//
//                // 是否扫二维码
//                if (selectedMethodParams.barcode == EOnOff.On)
//                {
//                    doWorkWithBarcodeScan()
//                }
//                else
//                {
//                    doWorkWithoutBarcode()
//                }
//            }
//            // 二维码修正模式
//            else if (barcodeRunMode == EBarcodeRunMode.Correct)
//            {
//                doWorkWithBarcodeScan()
//            }
//
//            CmdHelper.initMotor()
//
//            mHandlerMain.post(rnHideWaitDialog)
//
//            //  清空扫描枪二维码
//            alreadyScannedTubeListFromScannerGun.clear()
//
//            isProcedureRunning = false
//
//            // 程序正常或者手动结束，标记程序为正常完成
//            DBHelper.markRunCompleted(false)
//        }
//        // 二维码异常
//        catch (e: BarcodeException)
//        {
//            LogHelper.error("抛出了二维码异常")
//            // 让程序完全停止
//            sleep(1000)
//            // 复位手动停止属性
//            isManualPause = false
//            isManualStop = false
//            CmdHelper.initMotor()
//
//            // 弹出纠正二维码输入提示
//            DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_click_red_slide_and_correct_qr_code), getString(R.string.ok), 0, 0, object : InfoPromptListener
//            {
//                override fun clicked()
//                {
//                    barcodeRunMode = EBarcodeRunMode.Correct
//                }
//            }).show(fragmentManager, null)
//        }
//        // 中断异常
//        catch (e: InterruptedException)
//        {
//            e.printStackTrace()
//
//            // 程序正常或者手动结束，标记程序为正常完成
//            DBHelper.markRunCompleted(false)
//        }
//        // 超时异常
//        catch (e: TimeOutException)
//        {
//            e.printStackTrace()
//            showWarningDialogAutoPause(getString(R.string.info_action_failed_click_stop_to_main_ui), false)
//
//            // 程序正常或者手动结束，标记程序为正常完成
//            DBHelper.markRunCompleted(false)
//        }
//        // 初始化异常
//        catch (e: InitException)
//        {
//            e.printStackTrace()
//            showWarningDialogAutoPause(getString(R.string.info_init_failed_click_stop_to_main_ui), false)
//
//            // 程序正常或者手动结束，标记程序为正常完成
//            DBHelper.markRunCompleted(false)
//        }
//        // 手动停止
//        catch (e: ManualStopException)
//        {
//            e.printStackTrace()
//
//            // 程序正常或者手动结束，标记程序为正常完成
//            DBHelper.markRunCompleted(false)
//        }
//    }

    /**
     * 开始工作前预备动作
     */
    private fun doWorkPrepare()
    {
        // 到废液位置
        CmdHelper.toSprayTestPos(false)
        // 喷雾头加液
        CmdHelper.sprayAddLiquid(paramGeneralParams.preSprayLiquidAddition, false)
        // 喷固定液5次，喷雾ns
        CmdHelper.fixativeLiquid(selectedMethodParams, selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray, 5, false)
        CmdHelper.sprayMsec(paramGeneralParams.preSprayTime * 10, isSeparate = true, isAsync = false)
    }

    /**
     * 开始工作（包括扫二维码，执行移液动作）(扫完整板再移液)
     * @param startSlideIndex 玻片开始索引，默认为0。异常中断再继续，需要设置这个值
     * @param isInterrupted 是否异常中断的，默认为false。异常中断再继续，需要设置这个值为true
     */
    @Throws(InitException::class, InterruptedException::class, TimeOutException::class, ManualStopException::class)
    private fun doWorkWithBarcodeScan(startSlideIndex: Int = 0, isInterrupted: Boolean = false)
    {
        // 是否继续运行程序
        var isContinueRun = true

        while (isContinueRun)
        {
            // 默认模式：做扫码动作
            if (barcodeRunMode == EBarcodeRunMode.Default && !isInterrupted)
            {
                // 是否需要重新扫码
                var isRescan = false

                // 吸取玻片到盘面
                if (selectedMethodParams.slideAutomatic == EOnOff.On)
                {
                    var slideQuantity = selectedMethodParams.slideQuantity
                    for (index in 0 until slideQuantity)
                    {
                        // 吸玻片
                        CmdHelper.slideSuck(slideQuantity - index, false)
                        // 放玻片
                        CmdHelper.slideRelease(index, false)
                    }
                }

                // 扫码前关闭照明
                toggleLight(EOnOff.Off, runImmediately = false)
                isScanningQRCode = true

                // 先全部扫描玻片二维码
                for (posIndex in 0..63)
                {
                    // 判断是否已经扫了设定的玻片数量
                    if (posIndex == selectedMethodParams.slideQuantity) break

                    val isScannedSuccess = actionScanSlideBarcode(posIndex)
                    var existBarcode: Barcode?
                    // 扫描成功
                    if (isScannedSuccess)
                    {
                        // 判断是否存在该二维码的试管
                        existBarcode = alreadyScannedTubeListFromScannerGun.find { it.barcode == currentScannedBarcode }
                        // 不存在试管
                        if (existBarcode == null)
                        {
                            mHandlerMain.post {
                                // 刷新玻片状态
                                refreshSlideStatus(posIndex, ESlideStatus.Wrong)
                            }
                            showWarningDialogAutoPause(getString(R.string.slide_pos) + " " + (posIndex + 1) + " " + getString(R.string.info_tube_not_exist_stop_to_main_ui), false)
                            CmdHelper.manualPause(object : InfoPromptListener
                            {
                                override fun clicked()
                                {
                                    mHandlerMain.post {
                                        // 初始化玻片、试管状态信息
                                        initAllSlideStatus()
                                        initAllTubeHoleStatus()
                                    }
                                }
                            })
                        }
                        // 存在试管
                        else
                        {
                            mHandlerMain.post {
                                // 刷新玻片状态
                                refreshSlideStatus(posIndex, ESlideStatus.Scanned)
                            }
                        }
                    }
                    // 扫描失败（判断是否已经达到用户设置的玻片数量：否---跳过，继续扫码；是---跳出循环）
                    else
                    {
                        if (posIndex < selectedMethodParams.slideQuantity)
                        {
                            // 把该玻片二维码状态置为Wrong
                            if (alreadyScannedBarcodeFromCamera.size < 64 && !alreadyScannedBarcodeFromCamera.any { it.posIndex == posIndex })
                            {
                                alreadyScannedBarcodeFromCamera.add(Barcode(currentScannedBarcode, posIndex, isWrong = true))
                                // 刷新玻片状态
                                mHandlerMain.post { refreshSlideStatus(posIndex, ESlideStatus.Wrong) }
                            }
                            continue
                        }
                        break
                    }
                }

                // 扫码后打开照明
                toggleLight(EOnOff.On, runImmediately = false)
                isScanningQRCode = false

                // 判断玻片二维码是否需要纠正
                var wrongBarcodeCount = alreadyScannedBarcodeFromCamera.count { barcode -> barcode.isWrong }
                if (wrongBarcodeCount > 0)
                {
                    LogHelper.info("玻片二维码需要纠正")
                    // 关闭“运行中”框
                    ViewUtils.closeRunningDialog()
                    LogHelper.info("已经关闭了运行框")
                    // 抛出二维码异常，捕获后再处理
                    throw BarcodeException()
                }

                // 是否扫码完毕后直接运行
                if (paramGeneralParams.runImmAfterScanned == EOnOff.Off)
                {
                    // 弹出玻片扫码确认框，确认---继续执行程序；取消---终止程序；重新扫描---再一次扫描玻片二维码
                    ViewUtils.pauseRunningDialog() // 模拟“暂停”按钮
                    DialogFragment_Slide_Frame.newInstance().setDialogContent(mContext, getString(R.string.question_makesure_slide_frame_scan_completed), getString(R.string.confirm), getString(R.string.rescan), getString(R.string.cancel), alreadyScannedBarcodeFromCamera, object : SlideFrameConfirmListener
                    {
                        // 继续执行程序
                        override fun clickedOk()
                        {
                            ViewUtils.continueRunningDialog() // 模拟“继续”按钮
                        }
                        // 再一次扫描玻片二维码
                        override fun clickedRescan()
                        {
                            isRescan = true
                            ViewUtils.continueRunningDialog() // 模拟“继续”按钮
                        }
                        // 终止程序
                        override fun clickedCancel()
                        {
                            ViewUtils.closeRunningDialog() // 模拟“停止”按钮
                        }
                    }).show(parentFragmentManager, null)

                    CmdHelper.manualPause()

                    // 重新扫码
                    if (isRescan)
                    {
                        mHandlerMain.post {
                            initAllSlideStatus()
                        }
                        continue
                    }
                }
            }

            // 首板玻片底板，就做预备动作
            baseCountTick++
            if (baseCountTick == 1 && !isInterrupted)
            {
                doWorkPrepare()
            }

            // Log所有玻片二维码
            var barCodeLog = StringBuilder()
            alreadyScannedBarcodeFromCamera.forEach {
                barCodeLog.append("位置：" + it.posIndex + "  二维码：" + it.barcode + "  长度：" + it.barcode.length + "  是否有误：" + it.isWrong)
                barCodeLog.append("\n\r")
            }
            FileUtils.saveJsonToSD(barCodeLog.toString(), ConstantsUtils.FOLDER_SLIDE_BARCODE, SimpleDateFormat("yyyyMMdd").format(Date()), true)

            // 运行开始，更新数据库异常中断为true，以便中断后继续
            DBHelper.getLatestRunState()?.let { state ->
                val newState = state.copy(isInterrupted = true, scannedTubesJson = JsonParamHelper.barcodesToJson(alreadyScannedTubeListFromScannerGun), slideIndex = startSlideIndex, scannedSlidesJson = JsonParamHelper.barcodesToJson(alreadyScannedBarcodeFromCamera))
                DBHelper.updateRunState(newState)
            }

            /**
             *  1 个枪头吸喷完相同的玻片（一吸多喷）
             */
            // 玻片计数器
            var slideTick = startSlideIndex
            // 相同玻片分组
            var slideGroups = alreadyScannedBarcodeFromCamera.filter { it.posIndex >= startSlideIndex }.groupBy { it.barcode }.values
            for (groupIndex in slideGroups.indices)
            {
                // 相同的玻片
                var slides = slideGroups.elementAt(groupIndex)

                // 更新数据库，一吸多喷下第一个玻片index
                DBHelper.getLatestRunState()?.let { state ->
                    val newState = state.copy(slideIndex = slides.first().posIndex)
                    DBHelper.updateRunState(newState)
                }

                /**
                 * 一吸多喷信息
                 */
                // 相同试管一吸多喷玻片Index列表
                var slideIndexList = arrayListOf<Int>()
                slides.forEach {
                    slideIndexList.add(it.posIndex)
                }
                var one2MoreList = getOne2MoreList(slideIndexList)

                // 一吸多喷组数
                var one2MoreCount = one2MoreList.count()
                for (i in 0 until one2MoreCount)
                {
                    var one2More = one2MoreList[i]
                    // 玻片数量
                    var slideCount = one2More.slideIndexList.count()
                    for (j in 0 until slideCount)
                    {
                        // 玻片信息
                        var slideIndex = one2More.slideIndexList[j]
                        var slide = alreadyScannedBarcodeFromCamera.first { it.posIndex == slideIndex }

                        // 喷雾头加液（每n个玻片加液一次）
                        var sprayLiquidAdditionInterval = selectedMethodParams.paramsSlideMode.sprayLiquidAdditionInterval
                        if (selectedMethodParams.spreadingMode == ESpreadingMode.Wet && slideTick % sprayLiquidAdditionInterval == 0 && slideTick != 0)
                        {
                            // 第1次玻片喷雾头加液，用非异步方式，否则用异步方式
                            CmdHelper.sprayAddLiquid(paramGeneralParams.preSprayLiquidAddition, slideTick / sprayLiquidAdditionInterval != 1)
                        }

                        /**
                         * (取tip)  => (吸液 = 滴液体积 * 滴片点数)  => (喷雾) => (雾化后等待) => (雾化后固定)  => (滴片 = 滴液体积 * 滴片点数) => (吹风)  => (退tip)
                         */
                        if (!slide.barcode.isNullOrEmpty())
                        {
                            var tubeBarcode = alreadyScannedTubeListFromScannerGun.find { it.barcode == slide.barcode }
                            // 取tip
                            if (i == 0 && j == 0) actionTakeTip()
                            // 样品吹打
                            if (null != tubeBarcode && j == 0)
                            {
                                actionSampleStir(tubeBarcode.posIndex)
                                // 吸液
                                actionSuckLiquid(one2More.volumeAbsorb)
                            }
                            // 雾化前固定
                            actionFixativeBeforeSpray(slideIndex)
                            // 喷雾
                            actionSpray(slideIndex)
                            // 雾化后等待
                            actionDelayAfterSpray()
                            // 滴固定液
                            actionFixativeBeforeDispense(slideIndex)
                            // 滴片前等待
                            actionDelayBeforeDispense()
                            // 滴片
                            actionDispense(slideIndex, j == 0, j == slideCount - 1)
                            // 滴片后等待
                            actionDelayAfterDispense()
                            // 追加固定液
                            actionFixativeAfterDispense(slideIndex)
                            // 刷新玻片状态
                            mHandlerMain.post { refreshSlideStatus(slideIndex, ESlideStatus.Finished) }
                            // 退tip
                            if (i == one2MoreCount - 1 && j == slideCount - 1) actionReleaseTip(slideIndex)

                            // 复位Z轴（避免位置走偏）
                            CmdHelper.za(0.0, false)
                            CmdHelper.zi(isAsync = false)
                        }

                        // 如果做完64块玻片，就提示是否更换玻片底板继续执行
                        if (slideTick == 63)
                        {
                            // 提示是否更换玻片底板
                            mHandlerMain.post {
                                DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_do_you_need_to_replace_slides_base), getString(R.string.yes), getString(R.string.no), true, true, object : WarnPromptListener
                                {
                                    // 点击"确定"
                                    override fun clickedOk()
                                    {
                                        // 确认已经更换玻片底板
                                        DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.question_makesure_put_slides_base_already), getString(R.string.yes), 0, 0, object : InfoPromptListener
                                        {
                                            override fun clicked()
                                            {
                                                // 初始化玻片状态信息
                                                initAllSlideStatus()

                                                ViewUtils.continueRunningDialog()
                                            }
                                        }).show(parentFragmentManager, null)
                                    }

                                    // 点击"取消"
                                    override fun clickedCancel()
                                    {
                                        // 继续执行余下的复位指令
                                        ViewUtils.continueRunningDialog()
                                        isContinueRun = false
                                    }
                                }).show(parentFragmentManager, null)
                            }

                            // 暂停当前线程
                            ViewUtils.pauseRunningDialog()
                            CmdHelper.manualPause()
                        }
                        else
                        {
                            // 不足64个玻片且为最后一个玻片
                            if (slideTick == (alreadyScannedBarcodeFromCamera.size - 1)) isContinueRun = false
                        }

                        slideTick++
                    }
                }
            }

            // 纠正模式（如果之前为纠正模式，就复位到默认模式）
            if (barcodeRunMode == EBarcodeRunMode.Correct)
            {
                // 复位为默认模式
                barcodeRunMode = EBarcodeRunMode.Default
            }
        }
    }

    /**
     * 开始工作（不扫二维码）
     * @param tubeIndex 试管索引，默认为0。异常中断再继续，需要设置这个值
     * @param isInterrupted 是否异常中断的，默认为false。异常中断再继续，需要设置这个值为true
     */
    @Throws(InitException::class, InterruptedException::class, TimeOutException::class, ManualStopException::class)
    private fun doWorkWithoutBarcode(tubeIndex: Int = 0, isInterrupted: Boolean = false)
    {
        // 是否继续运行程序
        var isContinueRun = true

        //  默认每个玻片底板有64块玻片
        val tubesCount = selectedMethodParams.tubeQuantity
        var slidePerTube = selectedMethodParams.slidePerTube // 每管标本滴片数量
        var slideCountTotal = tubesCount * slidePerTube // 总玻片数目

        var currentSlideIndex = if (!isInterrupted) 0 else (tubeIndex * slidePerTube) % 64  // 当前底板玻片 index
        var currentSlideTotalIndex = if (!isInterrupted) 0 else tubeIndex * slidePerTube // 所有玻片 index （可能多个底板）

        // 首板玻片底板，就做预备动作
        baseCountTick++
        if (baseCountTick == 1 && !isInterrupted)
        {
            doWorkPrepare()
        }

        // 运行开始，更新数据库异常中断为true，以便中断后继续
        DBHelper.getLatestRunState()?.let { state ->
            val newState = state.copy(isInterrupted = true, scannedTubesJson = JsonParamHelper.barcodesToJson(alreadyScannedTubeListFromScannerGun), slideIndex = currentSlideIndex, tubeIndex = tubeIndex)
            DBHelper.updateRunState(newState)
        }

        /**
         *   一吸多喷
         */
        for (currentTubeIndex in tubeIndex until tubesCount)
        {
            // 更新数据库，一吸多喷下试管index等
            DBHelper.getLatestRunState()?.let { state ->
                val newState = state.copy(slideIndex = currentSlideIndex, tubeIndex = currentTubeIndex)
                DBHelper.updateRunState(newState)
            }

            /**
             * 一吸多喷信息
             */
            // 相同试管一吸多喷玻片Index列表
            var slideIndexList = arrayListOf<Int>()
            for (i in 0 until slidePerTube)
            {
                slideIndexList.add(currentTubeIndex * slidePerTube + i)
            }
            var one2MoreList = getOne2MoreList(slideIndexList)

            // 一吸一喷组数
            var one2MoreCount = one2MoreList.count()
            for (i in 0 until one2MoreCount)
            {
                var one2More = one2MoreList[i]
                // 玻片数量
                var slideCount = one2More.slideIndexList.count()
                for (j in 0 until slideCount)
                {
                    // 喷雾头加液（每n个玻片加液一次）
                    var sprayLiquidAdditionInterval = selectedMethodParams.paramsSlideMode.sprayLiquidAdditionInterval
                    if (selectedMethodParams.spreadingMode == ESpreadingMode.Wet && currentSlideTotalIndex % sprayLiquidAdditionInterval == 0 && currentSlideTotalIndex != 0)
                    {
                        // 第1次玻片喷雾头加液，用非异步方式，否则用异步方式
                        CmdHelper.sprayAddLiquid(paramGeneralParams.preSprayLiquidAddition, currentSlideTotalIndex / sprayLiquidAdditionInterval != 1)
                    }

                    /**
                     *  (取tip) => (吸液 = 滴液体积 * 滴片数量 * 滴片点数) => (喷雾) => (雾化后等待) => (雾化后固定)  => (滴片 = 滴液体积 * 滴片点数)  => (吹风)  => (退tip)
                     */
                    // 取tip
                    if (i == 0 && j == 0) actionTakeTip()
                    if (j == 0)
                    {
                        // 样品吹打
                        actionSampleStir(currentTubeIndex)
                        // 吸液
                        actionSuckLiquid(one2More.volumeAbsorb)
                    }
                    // 雾化前固定
                    actionFixativeBeforeSpray(currentSlideIndex)
                    // 喷雾
                    actionSpray(currentSlideIndex)
                    // 雾化后等待
                    actionDelayAfterSpray()
                    // 滴固定液
                    actionFixativeBeforeDispense(currentSlideIndex)
                    // 滴片前等待
                    actionDelayBeforeDispense()
                    // 滴片
                    actionDispense(currentSlideIndex, j == 0, j == slideCount - 1)
                    // 滴片后等待
                    actionDelayAfterDispense()
                    // 追加固定液
                    actionFixativeAfterDispense(currentSlideIndex)
                    // 刷新玻片状态
                    mHandlerMain.post { refreshSlideStatus(currentSlideIndex, ESlideStatus.Finished) }
                    // 退tip
                    if (i == one2MoreCount - 1 && j == slideCount - 1) actionReleaseTip(currentSlideIndex)

                    // 复位Z轴（避免位置走偏）
                    CmdHelper.za(0.0, false)
                    CmdHelper.zi(isAsync = false)

                    currentSlideIndex++
                    currentSlideTotalIndex++

                    // 如果玻片不足以做完本试管，就提示是否更换玻片底板继续执行
                    if (currentSlideIndex > 63 && currentSlideTotalIndex < slideCountTotal)
                    {
                        // 提示是否更换玻片底板
                        mHandlerMain.post {
                            DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_do_you_need_to_replace_slides_base), getString(R.string.yes), getString(R.string.no), true, true, object : WarnPromptListener
                            {
                                override fun clickedOk()
                                {
                                    // 确认更换玻片底板
                                    DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.question_makesure_put_slides_base_already), getString(R.string.yes), 0, 0, object : InfoPromptListener
                                    {
                                        override fun clicked()
                                        {
                                            // 更换玻片底板后，玻片index复位为0
                                            currentSlideIndex = 0
                                            ViewUtils.continueRunningDialog()

                                            // 初始化玻片状态信息
                                            initAllSlideStatus()
                                        }
                                    }).show(parentFragmentManager, null)
                                }

                                override fun clickedCancel()
                                {
                                    // 继续执行余下的复位指令
                                    ViewUtils.continueRunningDialog()
                                    isContinueRun = false
                                }
                            }).show(parentFragmentManager, null)
                        }

                        // 暂停当前线程
                        ViewUtils.pauseRunningDialog()
                        CmdHelper.manualPause()

                        if (!isContinueRun) return
                    }
                }
            }
        }
    }

    /** 获取一吸多喷信息
     * @param slideIndexList 玻片Index列表
     */
    private fun getOne2MoreList(slideIndexList: ArrayList<Int>): ArrayList<One2More>
    {
        // 每个玻片滴液点数
        val spotPerSlide = selectedMethodParams.spotPerSlide
        // 每滴体积
        val volumnPerDrop = selectedMethodParams.paramsSlideMode.volumnPerDrop
        // 每个玻片滴液体积
        var volumeTotalPerSlide = spotPerSlide * volumnPerDrop

        // 一吸多喷中，超过量程吸喷液信息
        var one2MoreInfoList: ArrayList<One2More> = arrayListOf()
        var one2More = One2More()
        one2MoreInfoList.add(one2More)

        // 体积计数器
        var volumeTick = 0
        slideIndexList.forEach {
            volumeTick += volumeTotalPerSlide
            if (volumeTick <= ConstantsUtils.HEAD_RANGE)
            {
                one2More.volumeAbsorb = volumeTick
                one2More.slideIndexList.add(it)
            }
            else
            {
                volumeTick = 0
                volumeTick += volumeTotalPerSlide
                one2More = One2More()
                one2More.volumeAbsorb = volumeTick
                one2More.slideIndexList.add(it)
                one2MoreInfoList.add(one2More)
            }
        }

        return one2MoreInfoList
    }


    /**
     * 扫描玻片二维码
     * @param posIndex 位置Index
     * @return Boolean 是否成功
     */
    private fun actionScanSlideBarcode(posIndex: Int): Boolean
    {
        var isScannedSuccess: Boolean
        // 原点坐标
        var originPos = ParamsHelper.paramPosBarcode.slides[posIndex]

        // 第一次识别
        isScannedSuccess = actionScanSlideBarcodeSub(posIndex, originPos, EDirection.None, 0.0f, 1000)

        if (isScannedSuccess) return isScannedSuccess

        // 若一次识别不成功，就左上右下各移动 x mm，再次识别，增加识别成功率
        for (i in 1..3)
        {
            isScannedSuccess = actionScanSlideBarcodeOffset(posIndex, originPos, i * 0.5f, 1000)

            if (isScannedSuccess) break
        }

        return isScannedSuccess
    }

    /**
     * 扫描玻片二维码偏移
     * @param posIndex 位置Index
     * @param originPos 原点坐标
     * @param offset_mm 偏移值
     * @param scanMs 检测毫秒
     */
    private fun actionScanSlideBarcodeOffset(posIndex: Int, originPos: Position, offset_mm: Float, scanMs: Int): Boolean
    {
        if (actionScanSlideBarcodeSub(posIndex, originPos, EDirection.Left, offset_mm, scanMs)) // 原点左offset_mm
            return true
        if (actionScanSlideBarcodeSub(posIndex, originPos, EDirection.Top, offset_mm, scanMs)) // 原点上offset_mm
            return true
        if (actionScanSlideBarcodeSub(posIndex, originPos, EDirection.Right, offset_mm, scanMs)) // 原点右offset_mm
            return true
        if (actionScanSlideBarcodeSub(posIndex, originPos, EDirection.Bottom, offset_mm, scanMs)) // 原点下offset_mm
            return true

        return false
    }

    /**
     * 扫描玻片二维码（子方法）
     * @param posIndex 位置
     * @param originPos 原点坐标
     * @param offset_direction 偏移方向
     * @param offset_mm 偏移值
     * @param scanMs 检测毫秒
     */
    private fun actionScanSlideBarcodeSub(posIndex: Int, originPos: Position, offset_direction: EDirection, offset_mm: Float, scanMs: Int): Boolean
    {
        CmdHelper.manualPause()

        currentScannedBarcode = ""
        var isScannedSuccess = false // 是否识别成功

        when (offset_direction)
        {
            EDirection.None -> CmdHelper.toBarcodePos(posIndex, false)
            EDirection.Left -> CmdHelper.xaya(originPos.x - offset_mm, originPos.y, false)
            EDirection.Top -> CmdHelper.xaya(originPos.x, originPos.y - offset_mm, false)
            EDirection.Right -> CmdHelper.xaya(originPos.x + offset_mm, originPos.y, false)
            EDirection.Bottom -> CmdHelper.xaya(originPos.x, originPos.y + offset_mm, false)
        }

        // 同一位置识别1次
        for (i in 0 until 1)
        {
            mSerialClientScanner.decodeStart(object : IScannerResultCallback
            {
                override fun success(info: String)
                {
                    if (info.isNotBlank())
                    {
                        /**
                         * 二维码识别长度
                         * 逻辑：
                         * ①识别结果长度少于或等于设置的长度，直接采用；
                         * ②识别结果长度大于设置的长度，截取设置的长度再采用
                         */
                        var scannedResult = info.trim()
                        if (scannedResult.count() > paramGeneralParams.qrCodeScanLength) scannedResult = scannedResult.substring(0, paramGeneralParams.qrCodeScanLength)
                        currentScannedBarcode = scannedResult

                        // 避免多于64个二维码
                        if (alreadyScannedBarcodeFromCamera.size < 64 && !alreadyScannedBarcodeFromCamera.any { it.posIndex == posIndex }) alreadyScannedBarcodeFromCamera.add(Barcode(currentScannedBarcode, posIndex))


                        // 显示当前二维码内容
                        mHandlerMain.post {
                            binding.tvCurrentQrCode.text = currentScannedBarcode
                        }
                        isScannedSuccess = true
                    }
                }

                override fun timeOut()
                {
                    isScannedSuccess = false
                }
            })

            // 休眠指定时间，让摄像头识别出二维码，超时退出识别
            var timeMSecElapse = 0
            while (true)
            {
                if (isScannedSuccess || timeMSecElapse > scanMs) break
                timeMSecElapse += 5
                sleep(5)
            }

            // 识别成功
            if (isScannedSuccess) break
        }

        Log.i(TAG, "扫描的玻片总量:" + alreadyScannedBarcodeFromCamera.size + " 当前玻片编号：" + currentScannedBarcode)

        return isScannedSuccess
    }

    /**
     * 纠正玻片二维码
     * @param posIndex 玻片位置Index
     */
    private fun correctSlideBarcode(posIndex: Int)
    {
        if (listSlide64[posIndex].slideStatus == ESlideStatus.Wrong && selectedMethodParams.barcode == EOnOff.On)
        {
            DialogFragment_Input_Barcode_Manually.newInstance().setDialogContent(getString(R.string.info_input_barcode_manually), posIndex, getString(R.string.confirm), getString(R.string.cancel), object : RenameListener
            {
                override fun confirm(index: Int, newValue: String)
                {
                    var barcode = alreadyScannedBarcodeFromCamera.first { it.posIndex == posIndex }
                    // 改为已纠正
                    barcode.isWrong = false
                    // 非空值
                    if (!newValue.isNullOrEmpty())
                    {
                        // 纠正二维码
                        barcode.barcode = newValue

                        // 刷新玻片状态
                        refreshSlideStatus(posIndex, ESlideStatus.Scanned)
                    }
                    // 空值（代表该位置玻片不存在）
                    else
                    {
                        // 刷新玻片状态
                        refreshSlideStatus(posIndex, ESlideStatus.Default)
                    }

                    // 还有哪些错误二维码没有纠正
                    var whichStr = ""
                    alreadyScannedBarcodeFromCamera.forEach {
                        if (it.isWrong) whichStr = whichStr + (it.posIndex + 1) + ","
                    }
                    Toast.makeText(mActivity, "These slide QR codes have not been corrected：$whichStr", Toast.LENGTH_SHORT).show()
                }

                override fun cancel()
                {
                    TODO("Not yet implemented")
                }
            }).show(parentFragmentManager, null)
        }
    }

    /**
     * 取tip
     */
    private fun actionTakeTip()
    {
        // 超过tip盒容量，提示换tip盒
        if (currentTakeTipIndex == 96)
        {
            // 提示更换tip盒
            mHandlerMain.post {
                DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.info_you_need_to_replace_tips_box), getString(R.string.confirm), 0, 0, object : InfoPromptListener
                {
                    override fun clicked()
                    {
                        // 确认已经更换tip盒
                        DialogFragment_Info_Prompt.newInstance().setDialogContent(null, getString(R.string.question_makesure_replace_tips_box_already), getString(R.string.confirm), 0, 0, object : InfoPromptListener
                        {
                            override fun clicked()
                            {
                                // 更换tip盒后，tip index复位为0
                                currentTakeTipIndex = 0
                                ViewUtils.continueRunningDialog()
                            }
                        }).show(parentFragmentManager, null)
                    }
                }).show(parentFragmentManager, null)
            }

            // 暂停当前线程
            ViewUtils.pauseRunningDialog()
            CmdHelper.manualPause()
        }

        CmdHelper.toTipPos(currentTakeTipIndex, false)

        // 防漏挡板缩入
        actionSealingPlate(false)

        CmdHelper.toPreTakeTipHeight(false)
        CmdHelper.toTakeTipHeight(false)
        CmdHelper.za(0.0, false)

        // 预吸空气
        CmdHelper.suckLiquid(selectedMethodParams.paramsSlideMode.volumnPerDrop * selectedMethodParams.spotPerSlide, false)

        currentTakeTipIndex++

        // 取枪头检测
        actionCheckTakeTipStatus {
            CmdHelper.pi(false)
            actionTakeTip()
        }

        CmdHelper.za(0.0, false)
    }

    /**
     * 样品吹打
     * @param posIndex 试管位置
     */
    private fun actionSampleStir(posIndex: Int)
    {
        CmdHelper.toTubePos(posIndex, false)

        // 防漏挡板缩入
        actionSealingPlate(false)

        // 吹打
        val numberOfSampleStir = paramGeneralParams.numberOfSampleStir // 吹打次数
        val volumnOfSampleStir = paramGeneralParams.volumnOfSampleStir // 吹打体积
        for (i in 0 until numberOfSampleStir)
        {
            CmdHelper.stirLiquid(volumnOfSampleStir, false)
        }
    }

    /**
     * 吸液
     * @param volumeAbsorb 吸液体积
     */
    private fun actionSuckLiquid(volumeAbsorb: Int)
    {
        // 每个玻片滴液点数
        val spotPerSlide = selectedMethodParams.spotPerSlide
        // 每滴体积
        val volumnPerDrop = selectedMethodParams.paramsSlideMode.volumnPerDrop

        // 吸液前吸空气
        if (paramGeneralParams.additionalAirInTipBeforeAbsorb != 0)
        {
            CmdHelper.additionalAirBeforeAbosrb(false)
        }
        CmdHelper.toSuckHeight(false)

        // 滴1滴
        if (spotPerSlide == 1)
        {
            CmdHelper.suckLiquid(volumeAbsorb, false)
        }
        // 滴多滴
        else
        {
            // ev方案：先在取枪头后预压缩活塞空气(暂时不用)，再吸液，再吸ev1(每滴体积)，再喷ev2(ev1)
            CmdHelper.suckLiquid(volumeAbsorb, false)
            // ev1(增加吸取液体)
            val ev1 = volumnPerDrop
            CmdHelper.suckLiquid(ev1, false)
            // ev2(喷出液体)
            val ev2 = ev1 * -1
            CmdHelper.suckLiquid(ev2, false)
        }

        // Tip头隔离空气（吸液后）
        if (paramGeneralParams.additionalAirInTip != 0)
        {
            CmdHelper.additionalAir(false)
        }

        CmdHelper.za(0.0, false)

        // 防漏挡板伸出
        actionSealingPlate(true)

        // 到枪头盒点1避让。避免吸取样品后，走在枪头盒上方，液滴溅落
        CmdHelper.xa(paramPosTips.releaseTipBoxPoint1X, false)
        CmdHelper.ya(paramPosTips.releaseTipBoxPoint1Y, false)
    }

    /**
     * 防漏挡板
     * @param extended 是否伸展，true-伸展；false-收回
     */
    private fun actionSealingPlate(extended: Boolean)
    {
        if (extended) CmdHelper.ma(paramPosTips.plateExtendedPos, false)
        else CmdHelper.ma(paramPosTips.plateRetractedPos, false)
    }

    /**
     *  雾化前固定
     *  @param posIndex 玻片位置
     */
    private fun actionFixativeBeforeSpray(posIndex: Int)
    {
        if (selectedMethodParams.paramsSlideMode.fixativeVolumnBeforeSpray > 0)
        {
            actionFixativeSub(posIndex, selectedMethodParams.paramsSlideMode.fixativeVolumnBeforeSpray)
        }
    }

    /**
     * 喷雾
     * @param posIndex 玻片位置
     */
    private fun actionSpray(posIndex: Int)
    {
        //  滴片模式：干片法---不使用喷雾，滴片后使用蒸汽吹管吹风；湿片法---预先使用喷雾头润湿玻片，后续可选择是否使用蒸汽吹管吹风
        if (selectedMethodParams.spreadingMode == ESpreadingMode.Wet)
        {
            CmdHelper.toSprayPos(posIndex, false)
            CmdHelper.toSprayHeight(selectedMethodParams, false)

            // 喷雾移动前固定时间（即在玻片开始位置喷雾n秒后再移动）
            var sprayLiquidTimeBeforeMove = selectedMethodParams.paramsSlideMode.sprayLiquidTimeBeforeMove
            if (sprayLiquidTimeBeforeMove > 0) CmdHelper.sprayMsec(sprayLiquidTimeBeforeMove * 10, isSeparate = false, isAsync = false)

            // 先减慢y轴速度，再移动喷头，再恢复y轴速度
            CmdHelper.spraySpeed(selectedMethodParams.paramsSlideMode.ySpeedWhenSpray, false)
            // 喷雾时间（秒）（默认30s，可随时关停）
            var sprayTime = 30
            // 开始喷雾
            CmdHelper.sprayMsec(sprayTime * 10, isSeparate = false, isAsync = true)
            // 下降高度
            var declineHeight = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosSpray.yDistance)
            // 移动y、w轴
            CmdHelper.wsys(declineHeight, paramPosSpray.yDistance, false)
            // 关闭喷雾
            CmdHelper.closeSpray()
            // y轴和w轴恢复默认速度
            ResetYAndWSpeed()

            // 如果喷雾后不需要滴固定液，喷雾头高度恢复0位
            if (selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray == 0)
            {
                CmdHelper.wa(0.0, false)
            }
        }
    }

    /**
     * 喷雾后等待
     */
    private fun actionDelayAfterSpray()
    {
        // 等待条件：湿片法下，雾化后等待时间 >0 且 雾化后固定液 > 0
        if (selectedMethodParams.spreadingMode == ESpreadingMode.Wet && selectedMethodParams.paramsSlideMode.delayTimeAfterSpray > 0 && selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray > 0)
        {
            val delayTimeAfterSpraySec = selectedMethodParams.paramsSlideMode.delayTimeAfterSpray
            for (i in 1..(delayTimeAfterSpraySec * 100).toInt())
            {
                CmdHelper.manualPause()
                sleep(10)
            }
        }
    }

    /**
     * 固定液动作
     * @param posIndex 玻片位置
     * @param fixativeVol 固定液体积
     */
    private fun actionFixativeSub(posIndex: Int, fixativeVol: Int)
    {
        // 滴液数量
        val spotPerSlide = selectedMethodParams.spotPerSlide

        // 滴1滴（在3滴的中间位置，所以pointIndex为1）
        if (spotPerSlide == 1)
        {
            CmdHelper.toFixativePos(posIndex, 1, false)
            CmdHelper.toFixativeHeight(selectedMethodParams, 1, false)
            CmdHelper.fixativeLiquid(selectedMethodParams, fixativeVol, false)
        }
        // 滴2滴（在3滴的上、下位置，所以pointIndex为0、2）
        else if (spotPerSlide == 2)
        {
            // 第1滴
            CmdHelper.toFixativePos(posIndex, 0, false)
            CmdHelper.toFixativeHeight(selectedMethodParams, 0, false)
            CmdHelper.fixativeLiquid(selectedMethodParams, fixativeVol, false)

            // 第2滴
            // 高度要与第一滴一致：通过公式计算高度(高度 = y轴间距 * tan度数)
            // 下降高度
            var declineHeight = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosFixative.yDistance * 2)
            CmdHelper.wsys(declineHeight, paramPosFixative.yDistance * 2, false)
            CmdHelper.fixativeLiquid(selectedMethodParams, fixativeVol, false)
        }
        // 滴3滴（在3滴的上、中、下位置，所以pointIndex为0、1、2）
        else
        {
            // 第1滴
            CmdHelper.toFixativePos(posIndex, 0, false)
            CmdHelper.toFixativeHeight(selectedMethodParams, 0, false)
            CmdHelper.fixativeLiquid(selectedMethodParams, fixativeVol, false)

            // 第2滴
            // 高度要与第一滴一致：通过公式计算高度(高度 = y轴间距 * tan度数)
            // 下降高度
            var declineHeightPoint2 = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosFixative.yDistance)
            CmdHelper.wsys(declineHeightPoint2, paramPosFixative.yDistance, false)
            CmdHelper.fixativeLiquid(selectedMethodParams, fixativeVol, false)

            // 第3滴
            // 高度要与第一滴一致：通过公式计算高度(高度 = y轴间距 * tan度数)
            // 下降高度
            var declineHeightPoint3 = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosFixative.yDistance * 2)
            CmdHelper.wsys(declineHeightPoint3, paramPosFixative.yDistance, false)
            CmdHelper.fixativeLiquid(selectedMethodParams, fixativeVol, false)
        }

        // 喷雾头高度0位
        CmdHelper.wa(0.0, false)
    }

    /**
     *  雾化后固定
     *  @param posIndex 玻片位置
     */
    private fun actionFixativeBeforeDispense(posIndex: Int)
    {
        if (selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray > 0)
        {
            actionFixativeSub(posIndex, selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray)
        }
    }

    /**
     * 滴片前等待时间
     */
    private fun actionDelayBeforeDispense()
    {
        // 等待条件：滴片前等待时间 >0 且 雾化后固定 > 0
        if (selectedMethodParams.paramsSlideMode.delayTimeBeforeSampleDropping > 0 && selectedMethodParams.paramsSlideMode.fixativeVolumnAfterSpray > 0)
        {
            val delayTimeBeforeSampleDroppingSec = selectedMethodParams.paramsSlideMode.delayTimeBeforeSampleDropping
            for (i in 1..(delayTimeBeforeSampleDroppingSec * 100).toInt())
            {
                CmdHelper.manualPause()
                sleep(10)
            }
        }
    }

    /**
     * 滴片
     * @param posIndex 玻片位置
     * @param firstSlide 是否第一个玻片
     * @param lastSlide 是否最后一个玻片
     */
    private fun actionDispense(posIndex: Int, firstSlide: Boolean, lastSlide: Boolean)
    {
        // 每个玻片滴液点数
        val spotPerSlide = selectedMethodParams.spotPerSlide
        // 每滴容量
        val volumnPerDrop = selectedMethodParams.paramsSlideMode.volumnPerDrop
        // 滴液容积
        var liquidVolume: Int

        when (spotPerSlide)
        {
            // 滴1滴（在3滴的中间位置，所以pointIndex为1）
            1 ->
            {
                CmdHelper.toDispensePos(posIndex, 1, false)
                // 防漏挡板缩入
                actionSealingPlate(false)
                CmdHelper.toDispenseHeight(selectedMethodParams, 1, false)
                // 最后一个玻片（直接PA0）
                if (lastSlide) CmdHelper.pa(0.0, false)
                // 第一个玻片（滴液量+ 吸液后吸空气量）
                else if (firstSlide && !lastSlide)
                {
                    liquidVolume = volumnPerDrop + paramGeneralParams.additionalAirInTip
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }
                // 中间玻片（滴液量+ 喷液后吸空气量）
                else
                {
                    liquidVolume = volumnPerDrop + paramGeneralParams.additionalAirInTipAfterDispense
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }
            }
            // 滴2滴（在3滴的上、下位置，所以pointIndex为0、2）
            2 ->
            {
                /**
                 * 第1滴
                 */
                CmdHelper.toDispensePos(posIndex, 0, false)
                // 防漏挡板缩入
                actionSealingPlate(false)
                CmdHelper.toDispenseHeight(selectedMethodParams, 0, false)
                // 第一个玻片（滴液量+ 吸液后吸空气量）
                if (firstSlide && !lastSlide)
                {
                    liquidVolume = volumnPerDrop + paramGeneralParams.additionalAirInTip
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }
                // 非第一个玻片（滴液量+ 喷液后吸空气量）
                else
                {
                    liquidVolume = volumnPerDrop + paramGeneralParams.additionalAirInTipAfterDispense
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }

                /**
                 * 第2滴
                 */
                // 高度要与第一滴一致：通过公式计算高度(高度 = y轴间距 * tan度数)
                // 下降高度
                var declineHeight = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosDispense.yDistance * 2)
                CmdHelper.zsys(declineHeight, paramPosDispense.yDistance * 2, false)
                // 最后一个玻片（直接PA0）
                if (lastSlide) CmdHelper.pa(0.0, false)
                // 非最后一个玻片（滴液量）
                else
                {
                    liquidVolume = volumnPerDrop
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }
            }
            // 滴3滴（在3滴的上、中、下位置，所以pointIndex为0、1、2）
            3 ->
            {
                /**
                 * 第1滴
                 */
                CmdHelper.toDispensePos(posIndex, 0, false)
                // 防漏挡板缩入
                actionSealingPlate(false)
                CmdHelper.toDispenseHeight(selectedMethodParams, 0, false)
                // 第一个玻片（滴液量+ 吸液后吸空气量）
                if (firstSlide && !lastSlide)
                {
                    liquidVolume = volumnPerDrop + paramGeneralParams.additionalAirInTip
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }
                // 非第一个玻片（滴液量+ 喷液后吸空气量）
                else
                {
                    liquidVolume = volumnPerDrop + paramGeneralParams.additionalAirInTipAfterDispense
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }

                /**
                 * 第2滴
                 */
                // 高度要与第一滴一致：通过公式计算高度(高度 = y轴间距 * tan度数)
                // 下降高度
                var declineHeightPoint2 = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosDispense.yDistance)
                CmdHelper.zsys(declineHeightPoint2, paramPosDispense.yDistance, false)
                liquidVolume = volumnPerDrop
                CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)

                /**
                 * 第3滴
                 */
                // 高度要与第一滴一致：通过公式计算高度(高度 = y轴间距 * tan度数)
                // 下降高度
                var declineHeightPoint3 = MathHelper.calcHeightFromRightTriangle(selectedMethodParams.angleOfSlide, paramPosDispense.yDistance * 2)
                CmdHelper.zsys(declineHeightPoint3, paramPosDispense.yDistance, false)
                // 最后一个玻片（直接PA0）
                if (lastSlide) CmdHelper.pa(0.0, false)
                // 非最后一个玻片（滴液量）
                else
                {
                    liquidVolume = volumnPerDrop
                    CmdHelper.dispenseLiquid(selectedMethodParams, liquidVolume, false)
                }
            }
        }

        // Tip头隔离空气（喷液后）
        if (paramGeneralParams.additionalAirInTipAfterDispense > 0)
        {
            // 滴片后延迟1s再反吸
            sleep(1000)
            CmdHelper.ps(paramGeneralParams.additionalAirInTipAfterDispense, false)
        }

        // 回到0位
        CmdHelper.za(0.0, false)

        // 防漏挡板伸出
        actionSealingPlate(true)
    }

    /**
     * 滴片后等待时间
     */
    private fun actionDelayAfterDispense()
    {
        // 等待条件：滴片后等待时间 >0 且 追加固定液 > 0
        if (selectedMethodParams.paramsSlideMode.delayTimeAfterSampleDropping > 0 && selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop > 0)
        {
            val delayTimeAfterSampleDroppingSec = selectedMethodParams.paramsSlideMode.delayTimeAfterSampleDropping
            for (i in 1..(delayTimeAfterSampleDroppingSec * 100).toInt())
            {
                CmdHelper.manualPause()
                sleep(10)
            }
        }
    }

    /**
     *  追加固定液
     *  @param posIndex 玻片位置
     */
    private fun actionFixativeAfterDispense(posIndex: Int)
    {
        if (selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop > 0)
        {
            actionFixativeSub(posIndex, selectedMethodParams.paramsSlideMode.fixativeVolumnPerDrop)
        }
    }

    /**
     * 退tip（走二维码通道）
     * @param posIndex 玻片 index
     */
    private fun actionReleaseTip(posIndex: Int)
    {
        CmdHelper.wa(0.0, true)

        // 走二维码通道
        val rowIndexOfSlide = posIndex / 16
        CmdHelper.ya(paramPosTips.releaseTipChannel1Y + rowIndexOfSlide * paramPosDispense.stepLengthY, false)

        actionReleaseTipNoQRCodeChannel()

        // 因为Tip头隔离空气（喷液后），所有P轴需要复位
        if (paramGeneralParams.additionalAirInTipAfterDispense > 0) CmdHelper.pi(false)

        // 退枪头检测
        actionCheckReleaseTipStatus {
            actionReleaseTipNoQRCodeAndTipBoxChannel()
        }

        CmdHelper.za(0.0, false)
    }

    /**
     * 退tip（不走二维码通道，但会绕过枪头盒）
     */
    private fun actionReleaseTipNoQRCodeChannel()
    {
        // 绕过tip盒
        CmdHelper.xa(paramPosTips.releaseTipBoxPoint1X, false)
        CmdHelper.ya(paramPosTips.releaseTipBoxPoint1Y, false)

        CmdHelper.xa(paramPosTips.releaseTipBoxPoint2X, false)
        CmdHelper.ya(paramPosTips.releaseTipBoxPoint2Y, false)

        CmdHelper.toReleaseTipPosY(false)

        // 防漏挡板缩入
        actionSealingPlate(false)
        CmdHelper.toReleaseTipHeight(false)

        CmdHelper.toReleaseTipPosX(false)
        CmdHelper.toReleaseTipOffset(false)
    }

    /**
     * 退tip（不走二维码通道，也不走枪头盒两点）
     */
    private fun actionReleaseTipNoQRCodeAndTipBoxChannel()
    {
        // 防漏挡板缩入
        actionSealingPlate(false)
        CmdHelper.toReleaseTipHeight(false)
        CmdHelper.toReleaseTipPosY(false)
        CmdHelper.toReleaseTipPosX(false)
        CmdHelper.toReleaseTipOffset(false)
    }

    /**
     * 取枪头检测
     * @param reExecuteAction 重新执行动作
     */
    private fun actionCheckTakeTipStatus(reExecuteAction: () -> Unit)
    {
        if (paramGeneralParams.alarmTipCheck == EOnOff.On)
        {
            CmdHelper.toTipCheckPos(false)
            sleep(200)

            if (!CmdHelper.checkTakeTipStatus())
            {
                // 是否重新取枪头
                var needRetakeTip = false
                // TODO 蜂鸣器

                mHandlerMain.post {
                    DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_tip_take_unsuccess), getString(R.string.yes), getString(R.string.no), true, true, object : WarnPromptListener
                    {
                        // 点击"确定"
                        override fun clickedOk()
                        {
                            needRetakeTip = true
                            ViewUtils.continueRunningDialog()
                        }

                        // 点击"取消"
                        override fun clickedCancel()
                        {
                            ViewUtils.continueRunningDialog()
                        }
                    }).show(parentFragmentManager, null)
                }

                // 暂停当前线程
                ViewUtils.pauseRunningDialog()
                CmdHelper.manualPause()

                if (needRetakeTip) reExecuteAction()
            }
        }
    }

    /**
     * 退枪头检测
     * @param reExecuteAction 重新执行动作
     */
    private fun actionCheckReleaseTipStatus(reExecuteAction: () -> Unit)
    {
        if (paramGeneralParams.alarmTipCheck == EOnOff.On)
        {
            CmdHelper.toTipCheckPos(false)
            sleep(200)
            if (!CmdHelper.checkReleaseTipStatus())
            {
                // 是否重新退枪头
                var needRelease = false
                // TODO 蜂鸣器

                mHandlerMain.post {
                    DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, getString(R.string.question_tip_release_unsuccess), getString(R.string.yes), getString(R.string.no), true, true, object : WarnPromptListener
                    {
                        // 点击"确定"
                        override fun clickedOk()
                        {
                            needRelease = true
                            ViewUtils.continueRunningDialog()
                        }

                        // 点击"取消"
                        override fun clickedCancel()
                        {
                            ViewUtils.continueRunningDialog()
                        }
                    }).show(parentFragmentManager, null)
                }

                // 暂停当前线程
                ViewUtils.pauseRunningDialog()
                CmdHelper.manualPause()

                if (needRelease) reExecuteAction()
            }
        }
    }
}

