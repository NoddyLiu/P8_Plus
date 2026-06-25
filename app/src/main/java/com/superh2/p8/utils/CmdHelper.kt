package com.superh2.p8.utils

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.superh2.library.myEntityJson.MethodParams
import com.superh2.library.myEnum.EAngle
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myException.InitException
import com.superh2.library.myException.ManualStopException
import com.superh2.library.myException.NoReachException
import com.superh2.library.myInterface.ICommandResultCallback
import com.superh2.library.myInterface.InfoPromptListener
import com.superh2.library.myInterface.WarnPromptListener
import com.superh2.library.utils.LogHelper
import com.superh2.library.utils.MathHelper
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import com.superh2.library.utils.ParamsHelper.paramPosBarcode
import com.superh2.library.utils.ParamsHelper.paramPosCollect
import com.superh2.library.utils.ParamsHelper.paramPosDispense
import com.superh2.library.utils.ParamsHelper.paramPosFixative
import com.superh2.library.utils.ParamsHelper.paramPosSlide
import com.superh2.library.utils.ParamsHelper.paramPosSpray
import com.superh2.library.utils.ParamsHelper.paramPosTips
import com.superh2.library.utils.ParamsHelper.paramPosTubes
import com.superh2.library.utils.ParamsHelper.paramScale
import com.superh2.p8.MainActivity
import com.superh2.p8.MainActivity.Companion.isManualPause
import com.superh2.p8.MainActivity.Companion.isManualStop
import com.superh2.p8.MainActivity.Companion.mContext
import com.superh2.p8.MainActivity.Companion.mDataCommunication
import com.superh2.p8.MainActivity.Companion.mSerialClientHumiture
import com.superh2.p8.R
import com.superh2.p8.dialogs.DialogFragment_Warn_Prompt
import com.superh2.p8.serial.PipetteCompat
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.roundToInt

/** 指令执行帮助类
 * 底板角度以0°为基准
 * Created by Noddy on 2018/4/25.
 */
object CmdHelper
{
    private val TAG = "CmdHelper"

    /**
     * 初始化电机
     */
    @Throws(InitException::class, ManualStopException::class)
    fun initMotor()
    {
        // y轴和w轴恢复默认速度
        ResetYAndWSpeed()

        // W轴：喷雾轴
        if (!doCmd("WI", false, 15000)) throw InitException()

        manualPause()

        if (!doCmd("ZI", false, 15000)) throw InitException()

        manualPause()

        // M轴：防漏挡板
        if (!doCmd("MI", false, 15000)) throw InitException()

        manualPause()

        // 吸玻片
        if (!doCmd("AO10", false, 10000)) throw InitException()

        manualPause()

        if (!doCmd("XI,YI", false, 20000)) throw InitException()

        manualPause()

        // 吸喷液
//        if (!doCmd("PI", false, 10000)) throw InitException()
        pi(false)

        // Q轴虚拟复位，防止喷雾加液泵步数溢出
        if (!doCmd("QI 0", false, 10000)) throw InitException()
    }

    /**
     * 枪头位置
     * @param index 位置0~95
     * @param isAsync 是否异步
     */
    fun toTipPos(index: Int, isAsync: Boolean)
    {
        manualPause()

        xaya(paramPosTips.tips[index].x, paramPosTips.tips[index].y, isAsync)
    }

    /**
     * 枪头预取tip高度
     * @param isAsync 是否异步
     */
    fun toPreTakeTipHeight(isAsync: Boolean)
    {
        manualPause()

        val heightLength = paramPosTips.preTakeTipHeight
        if (heightLength > 0) za(heightLength, isAsync)
    }

    /**
     * 枪头取tip高度
     * @param isAsync 是否异步
     */
    fun toTakeTipHeight(isAsync: Boolean)
    {
        manualPause()

        val heightLength = paramPosTips.takeTipHeight
        za(heightLength, isAsync)
    }

    /**
     * 退tip位置X
     * @param isAsync 是否异步
     */
    fun toReleaseTipPosX(isAsync: Boolean)
    {
        manualPause()

        val xLength = paramPosTips.releaseTipPos.x
        xa(xLength, isAsync)
    }

    /**
     * 退tip位置Y
     * @param isAsync 是否异步
     */
    fun toReleaseTipPosY(isAsync: Boolean)
    {
        manualPause()

        val yLength = paramPosTips.releaseTipPos.y

        ya(yLength, isAsync)
    }

    /**
     * 枪头退tip高度
     * @param isAsync 是否异步
     */
    fun toReleaseTipHeight(isAsync: Boolean)
    {
        manualPause()

        val heightLength = paramPosTips.releaseTipHeight
        za(heightLength, isAsync)
    }

    /**
     * 枪头退tip提拉
     * @param isAsync 是否异步
     */
    fun toReleaseTipOffset(isAsync: Boolean)
    {
        manualPause()

        val heightLength = paramPosTips.releaseTipHeight - paramPosTips.releaseTipOffset
        za(heightLength, isAsync)
    }

    /**
     * 试管位置
     * @param index 位置0~63
     * @param isAsync 是否异步
     */
    fun toTubePos(index: Int, isAsync: Boolean)
    {
        manualPause()

        xaya(paramPosTubes.tubes[index].x, paramPosTubes.tubes[index].y, isAsync)
    }

    /**
     * 试管吸液高度
     * @param isAsync 是否异步
     */
    fun toSuckHeight(isAsync: Boolean)
    {
        manualPause()

        val heightLength = paramPosTubes.suckHeight
        za(heightLength, isAsync)
    }

    /**
     * 玻片喷雾高度
     * @param methodParams 参数组
     * @param isAsync 是否异步
     */
    fun toSprayHeight(methodParams: MethodParams, isAsync: Boolean)
    {
        manualPause()

        // 喷雾高度（从上到下） = 高度量程 - 喷雾高度（从下到上）
        val heightTop2Bottom = paramPosSpray.heightRange - paramPosSpray.sprayHeight // 以0°为基准
        var heightLength: Double = when (methodParams.angleOfSlide)
        {
            // 底板角度转换造成高度偏移
            EAngle.Zero -> heightTop2Bottom
            EAngle.Five -> heightTop2Bottom + MathHelper.calcHeightFromRightTriangle(EAngle.Five, paramPosSpray.yDistance)
            else -> heightTop2Bottom + MathHelper.calcHeightFromRightTriangle(EAngle.Ten, paramPosSpray.yDistance)
        }
        // 取两位小数
        heightLength = String.format("%.2f", heightLength).toDouble()

        wa(heightLength, isAsync)
    }

    /**
     * 喷雾位置
     * @param index 位置0~63
     * @param isAsync 是否异步
     */
    fun toSprayPos(index: Int, isAsync: Boolean)
    {
        manualPause()

        xaya(paramPosSpray.slides[index].x, paramPosSpray.slides[index].y, isAsync)
    }

    /**
     * 喷雾测试位置
     * @param isAsync 是否异步
     */
    fun toSprayTestPos(isAsync: Boolean)
    {
        manualPause()
        xaya(paramPosSpray.testPos.x, paramPosSpray.testPos.y, isAsync)
    }

    /**
     * 玻片滴液高度
     * @param methodParams 参数组
     * @param pointIndex 滴液Index
     * @param isAsync 是否异步
     */
    fun toDispenseHeight(methodParams: MethodParams, pointIndex: Int, isAsync: Boolean)
    {
        manualPause()

        var yDistance = paramPosDispense.yDistance * pointIndex

        // 滴液高度（从上到下） = 高度量程 - 滴液高度（从下到上）
        val heightTop2Bottom = paramPosDispense.heightRange - methodParams.paramsSlideMode.dispenseHeight // 以0°为基准
        var heightLength: Double = when (methodParams.angleOfSlide)
        {
            // 底板角度转换造成高度偏移
            EAngle.Zero -> heightTop2Bottom
            EAngle.Five -> heightTop2Bottom + MathHelper.calcHeightFromRightTriangle(EAngle.Five, yDistance)
            else -> heightTop2Bottom + MathHelper.calcHeightFromRightTriangle(EAngle.Ten, yDistance)
        }
        // 取两位小数
        heightLength = String.format("%.2f", heightLength).toDouble()

        za(heightLength, isAsync)
    }

    /**
     * 滴液位置
     * @param index 位置0~63
     * @param pointIndex 滴液Index
     * @param isAsync 是否异步
     */
    fun toDispensePos(index: Int, pointIndex: Int, isAsync: Boolean)
    {
        manualPause()

        // 走二维码通道
        val rowIndexOfSlide = index / 16
        ya(paramPosTips.releaseTipChannel1Y + rowIndexOfSlide * paramPosDispense.stepLengthY, false)
        xa(paramPosDispense.slides[index].x, false)

        xaya(paramPosDispense.slides[index].x, paramPosDispense.slides[index].y + paramPosDispense.yDistance * pointIndex, isAsync)
    }

    /**
     * 玻片固定液高度
     * @param methodParams 参数组
     * @param pointIndex 固定液Index
     * @param isAsync 是否异步
     */
    fun toFixativeHeight(methodParams: MethodParams, pointIndex: Int, isAsync: Boolean)
    {
        manualPause()

        var yDistance = paramPosFixative.yDistance * pointIndex

        // 固定液高度（从上到下） = 高度量程 - 固定液高度（从下到上）
        val heightTop2Bottom = paramPosFixative.heightRange - paramPosFixative.fixativeHeight // 以10°为基准
        var heightLength: Double = when (methodParams.angleOfSlide)
        {
            // 底板角度转换造成高度偏移
            EAngle.Zero -> heightTop2Bottom
            EAngle.Five -> heightTop2Bottom + MathHelper.calcHeightFromRightTriangle(EAngle.Five, yDistance)
            else -> heightTop2Bottom + MathHelper.calcHeightFromRightTriangle(EAngle.Ten, yDistance)
        }
        // 取两位小数
        heightLength = String.format("%.2f", heightLength).toDouble()

        wa(heightLength, isAsync)
    }

    /**
     * 固定液位置
     * @param slideIndex 玻片位置0~63
     * @param pointIndex 固定液Index
     * @param isAsync 是否异步
     */
    fun toFixativePos(slideIndex: Int, pointIndex: Int, isAsync: Boolean)
    {
        manualPause()

        xaya(paramPosFixative.slides[slideIndex].x, paramPosFixative.slides[slideIndex].y + paramPosFixative.yDistance * pointIndex, isAsync)
    }

    /**
     * 固定液测试位置
     * @param isAsync 是否异步
     */
    fun toFixativeTestPos(isAsync: Boolean)
    {
        manualPause()
        xaya(paramPosFixative.testPos.x, paramPosFixative.testPos.y, isAsync)
    }

    /**
     * 二维码位置
     * @param index 位置0~63
     * @param isAsync 是否异步
     */
    fun toBarcodePos(index: Int, isAsync: Boolean)
    {
        manualPause()
        xaya(paramPosBarcode.slides[index].x, paramPosBarcode.slides[index].y, isAsync)
    }

    // region 玻片摆放(未滴片)
    /**
     * 吸玻片位置(未滴片)
     * @param isAsync 是否异步
     */
    private fun toSlideSuckPos(isAsync: Boolean)
    {
        manualPause()
        xaya(paramPosSlide.suckPos.x, paramPosSlide.suckPos.y, isAsync)
    }

    /**
     * 吸玻片高度(未滴片)
     * @param remainQty 剩余玻片数目
     * @param isAsync 是否异步
     */
    private fun toSlideSuckHeight(remainQty: Int, isAsync: Boolean)
    {
        manualPause()

        // 真实吸片高度= paramPosSlide.suckHeight - ( 剩余玻片数目 - 1) * 玻片厚度
        val suckHeight = paramPosSlide.suckHeight - (remainQty - 1) * paramPosSlide.thickness
        wa(suckHeight, isAsync)
    }

    /**
     * 放玻片位置(未滴片)
     * @param index 位置0~63
     * @param isAsync 是否异步
     */
    private fun toSlideReleasePos(index: Int, isAsync: Boolean)
    {
        manualPause()
        xaya(paramPosSlide.slides[index].x, paramPosSlide.slides[index].y, isAsync)
    }

    /**
     * 吸玻片(未滴片)
     * @param remainQty 剩余玻片数量
     * @param isAsync 是否异步
     */
    fun slideSuck(remainQty: Int, isAsync: Boolean)
    {
        manualPause()
        // 行走高度
        wa(paramPosSlide.walkingHeight, isAsync)

        // 吸玻片位置
        toSlideSuckPos(isAsync)

        // 吸玻片高度
        toSlideSuckHeight(remainQty, isAsync)

        // 吸
        manualPause()
        doCmd("AO11", isAsync)

        // 先提高5mm
        manualPause()
        ws(-5.0, isAsync)

        sleep(300)
    }

    /**
     * 放玻片高度(未滴片)
     * @param isAsync 是否异步
     */
    private fun toSlideReleaseHeight(isAsync: Boolean)
    {
        manualPause()
        wa(paramPosSlide.releaseHeight, isAsync)
    }

    /**
     * 放玻片(未滴片)
     * @param index 玻片位置Index
     * @param isAsync 是否异步
     */
    fun slideRelease(index: Int, isAsync: Boolean)
    {
        manualPause()
        // 行走高度
        wa(paramPosSlide.walkingHeight, isAsync)

        // 放玻片位置
        toSlideReleasePos(index, isAsync)

        // 放玻片高度
        toSlideReleaseHeight(isAsync)

        // 放
        manualPause()
        doCmd("AO10", isAsync)

        // 停稳玻片
        sleep(1000)

        manualPause()
        // 行走高度
        wa(paramPosSlide.walkingHeight, isAsync)
    }
    // endregion 吸玻片(未滴片)

    // region 玻片收集(已滴片)
    /**
     * 吸玻片位置(已滴片)
     * @param isAsync 是否异步
     */
    private fun toCollectSuckPos(index:Int,isAsync: Boolean)
    {
        manualPause()
        val originX = paramPosSlide.slides[index].x
        val originY = paramPosSlide.slides[index].y
        xaya(originX, originY, isAsync)
    }

    /**
     * 吸玻片高度(已滴片)
     * @param isAsync 是否异步
     */
    private fun toCollectSuckHeight(isAsync: Boolean)
    {
        manualPause()
        wa(paramPosSlide.releaseHeight, isAsync)
    }

    /**
     * 放玻片位置(已滴片)
     * @param trayIndex 收集架0~1
     * @param slideIndex 玻片位置0~63
     * @param isAsync 是否异步
     */
    private fun toCollectReleasePos(trayIndex:Int, slideIndex: Int, isAsync: Boolean)
    {
        manualPause()

        // 矩阵坐标映射
        // 假设托盘是 4行 x 16列 (你的UI也是 posIndex/16 行， posIndex%16 列)
        val trayRow = slideIndex / 16

        // 确定当前应该放入哪个收集架
        val startX = if (trayIndex == 0) paramPosCollect.rack1StartX else paramPosCollect.rack2StartX
        val targetY = if (trayIndex == 0) paramPosCollect.rack1StartY else paramPosCollect.rack2StartY

        // 计算该格子在收集架中的绝对坐标：托盘行对应X轴步长，托盘列对应Z轴步长
        val targetX = startX + (trayRow * paramPosCollect.stepX)

        xaya(targetX, targetY, isAsync)
    }

    /**
     * 放玻片高度(已滴片)
     * @param trayIndex 收集架0~1
     * @param slideIndex 玻片位置0~63
     * @param isAsync 是否异步
     */
    private fun toCollectReleaseHeight(trayIndex: Int, slideIndex: Int, isAsync: Boolean)
    {
        manualPause()

        // 矩阵坐标映射
        // 假设托盘是 4行 x 16列 (你的UI也是 posIndex/16 行， posIndex%16 列)
        val trayCol = slideIndex % 16
        val startZ = if (trayIndex == 0) paramPosCollect.rack1StartZ else paramPosCollect.rack2StartZ

        val targetZ = startZ + (trayCol * paramPosCollect.stepZ)
        wa(targetZ, isAsync)
    }

    /**
     * 吸玻片(已滴片)
     * @param index 玻片位置Index
     * @param isAsync 是否异步
     */
    fun collectSuck(index: Int, isAsync: Boolean)
    {
        manualPause()
        // 行走高度
        wa(paramPosSlide.walkingHeight, isAsync)

        // 吸玻片位置
        toCollectSuckPos(index, isAsync)

        // 吸玻片高度
        toCollectSuckHeight(isAsync)

        // 吸
        manualPause()
        doCmd("AO11", isAsync)

        // 先提高5mm
        manualPause()
        ws(-5.0, isAsync)

        sleep(300)
    }

    /**
     * 收集玻片(已滴片)
     * @param trayIndex 收集架0~1
     * @param slideIndex 玻片位置0~63
     * @param isAsync 是否异步
     */
    fun collectRelease(trayIndex: Int, slideIndex: Int, isAsync: Boolean)
    {
        manualPause()
        // 行走高度
        wa(paramPosSlide.walkingHeight, isAsync)

        // 放玻片位置
        toCollectReleasePos(trayIndex, slideIndex, isAsync)

        // 放玻片高度
        toCollectReleaseHeight(trayIndex, slideIndex, isAsync)

        // Y轴向前伸入
        manualPause()
        val finalY = paramPosCollect.insertYDist * -1 // 负数表示向前伸入
        ys(finalY, isAsync)

        // Z轴微降放置玻片
        manualPause()
        val finalZ = paramPosCollect.placeZDist // 正数表示向下放置
        ws(finalZ, isAsync)

        // 放
        manualPause()
        doCmd("AO10", isAsync)

        // 停稳玻片
        sleep(1000)

        // Y轴退回原位，Z轴抬起安全高度准备下一次循环
        manualPause()
        ys(finalY * -1, isAsync)
        manualPause()
        wa(paramPosSlide.walkingHeight, isAsync)
    }
    // endregion 吸玻片(未滴片)


    /**
     * 喷雾时间
     * @param tsec 十分之一秒（0：停止执行）
     * @param isSeparate 是否把时间拆分喷雾
     * @param isAsync 是否异步
     */
    fun sprayMsec(tsec: Int, isSeparate: Boolean, isAsync: Boolean)
    {
        manualPause()

        if (isSeparate)
        {
            // 把时间切断为每次下发100ms（避免喷雾时，界面切换导致程序没有真正关停，会继续运行）
            for (tick in 0 until tsec)
            {
                manualPause()
                doCmd("AK1", isAsync)
//                doCmd("AO500100", isAsync)
            }
        }
        else
        {
            doCmd("AK$tsec", isAsync)
//            if (tsec == 0) closeSpray()
//            else
//            {
//                val msec = String.format("%04d", tsec * 100)
//                doCmd("AO5$msec", isAsync)
//            }
        }
    }

    /**
     * 关闭喷雾
     */
    fun closeSpray()
    {
        doCmd("AK0", false)
//        doCmd("AO50", false)
    }

    /**
     * 喷雾头加液
     * @param isAsync 是否异步
     */
    fun sprayAddLiquid(sprayLiquidAddition: Int, isAsync: Boolean)
    {
        manualPause()

//        val qfPreCmd = "QW 88 1,QW 14 1"
        val qfPreCmd = "QW 14 1"
        // 加液速度
        val qfCmd = "QF ${paramGeneralParams.sprayLiquidAdditionSpeedInit} ${paramGeneralParams.sprayLiquidAdditionSpeedTarget} ${paramGeneralParams.sprayLiquidAdditionSpeedAccelerated}" // 同时设置加液速度
        // 加液步数
        val qsCmd = "QS $sprayLiquidAddition"
//        val qsCmdOverturn = "QS -${paramGeneralParams.preSprayLiquidAdditionReverse}"  // 反抽一下，避免喷不了雾

        doCmd(qfPreCmd, false)
        doCmd(qfCmd, false)

        if (isAsync)
        {
            doCmd(qsCmd, true)
        }
        else
        {
            // 超时时间 20 秒
            val timeoutMs = 20000
            var timePassedMs = 0
            mDataCommunication.isSprayWaterAddDone = false
            // 需要异步下发加液步数，再不断判断isSprayWaterAddDone状态
            doCmd(qsCmd, true)
            while (timePassedMs < timeoutMs)
            {
                if (mDataCommunication.isSprayWaterAddDone)
                {
                    LogHelper.info("喷雾头加液完成，耗时：$timePassedMs ms")
                    break
                }
                sleep(100) // 每 100ms 检测一次
                timePassedMs += 100
                manualPause()
            }
            if (!mDataCommunication.isSprayWaterAddDone)
            {
                LogHelper.info("喷雾头加液超时：加液未在 20 秒内完成")
                // 弹出超时提示框
                ViewUtils.pauseRunningDialog() // 模拟“暂停”按钮
                DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, MainActivity.mContext!!.getString(R.string.info_cmd_timeOut_1) + qsCmd + MainActivity.mContext!!.getString(R.string.info_cmd_timeOut_2), MainActivity.mContext!!.getString(R.string.yes), MainActivity.mContext!!.getString(R.string.no), true, true, object : WarnPromptListener
                {
                    override fun clickedOk()
                    {
                        ViewUtils.continueRunningDialog() // 模拟“继续”按钮
                    }

                    override fun clickedCancel()
                    {
                        ViewUtils.closeRunningDialog() // 模拟“停止”按钮
                    }
                }).show((MainActivity.mContext as AppCompatActivity).supportFragmentManager, null)
                manualPause()
            }

//            doCmd(qsCmdOverturn, false)
        }
    }

    /**
     * 吹打速度
     * @param speed 速度
     * @param isAsync 是否异步
     */
    private fun stirSpeed(speed: Int, isAsync: Boolean)
    {
//        doCmd("PF 50 1500 $speed", isAsync)
        runPipette(isAsync) {
            PipetteCompat.driver.setAspirateSpeed(speed)
            PipetteCompat.driver.setDispenseSpeed(speed)
        }
    }

    /**
     * 吹打样本（吸液、排液）
     * @param liquidVolume 吹打体积
     * @param isAsync 是否异步
     */
    fun stirLiquid(liquidVolume: Int, isAsync: Boolean)
    {
        manualPause()

        // 吸液高度
        toSuckHeight(false)

        // 吹打速度
        stirSpeed(paramGeneralParams.speedOfSampleStir, false)

        // 吸液容积
        ps(liquidVolume, isAsync)

        // 喷液容积
        pa(0.0, isAsync)
    }

    /** 吸液
     * @param liquidVolume 吸液容积
     * @param isAsync 是否异步
     */
    fun suckLiquid(liquidVolume: Int, isAsync: Boolean)
    {
        manualPause()

        // 吸液容积
//        ps(liquidVolume, isAsync)
        runPipette(isAsync) {
            PipetteCompat.driver.aspirate(liquidVolume)
        }
    }


    /**
     * Tip头预吸空气
     * @param isAsync 是否异步
     */
    fun additionalAirBeforeAbosrb(isAsync: Boolean)
    {
        manualPause()

        // 吸取空气高度
        za(paramPosTips.additionalAirHeight, isAsync)

        //Tip头预吸空气体积
//        ps(paramGeneralParams.additionalAirInTipBeforeAbsorb, isAsync)
        psBeforeAbsorb(paramGeneralParams.additionalAirInTipBeforeAbsorb, isAsync)
    }


    /**
     * Tip头隔离空气
     * @param isAsync 是否异步
     */
    fun additionalAir(isAsync: Boolean)
    {
        manualPause()

        // 吸取空气高度
        za(paramPosTips.additionalAirHeight, isAsync)

        //Tip头隔离空气体积
//        ps(paramGeneralParams.additionalAirInTip, isAsync)
        psAfterAbsorb(paramGeneralParams.additionalAirInTip, isAsync)
    }

    /** 滴液
     * @param methodParams 参数组
     * @param liquidVolume 滴液容积
     * @param isAsync 是否异步
     */
    fun dispenseLiquid(methodParams: MethodParams, liquidVolume: Int, isAsync: Boolean)
    {
        manualPause()

        // 滴液速度
//        dispenseSpeed(methodParams.paramsSlideMode.dispenseSpeed, isAsync)
//        ps(liquidVolume * -1, isAsync)

        runPipette(isAsync) {
            PipetteCompat.driver.setDispenseSpeed(methodParams.paramsSlideMode.dispenseSpeed)
            PipetteCompat.driver.dispense(liquidVolume)
        }
    }

    /**
     * 喷雾y、w速度
     * @param ySpeedLevel y轴速度1~10
     * @param isAsync 是否异步
     */
    fun spraySpeed(ySpeedLevel: Int, isAsync: Boolean)
    {
        //        doCmd("YF 10 100 1,WF 30 40 1", isAsync)

        val initSpeedEach = paramGeneralParams.ySpeedMaxWhenSprayingInit.toFloat() / 10
        val targetSpeedEach = paramGeneralParams.ySpeedMaxWhenSprayingTarget.toFloat() / 10
        val acceleratedSpeedEach = paramGeneralParams.ySpeedMaxWhenSprayingAccelerated.toFloat() / 10

        var initSpeed = (initSpeedEach * ySpeedLevel).roundToInt()
        var targetSpeed = (targetSpeedEach * ySpeedLevel).roundToInt()
        var acceleratedSpeed = (acceleratedSpeedEach * ySpeedLevel).roundToInt()

        if (initSpeed <= 0) initSpeed = 1
        if (targetSpeed <= 0) targetSpeed = 5
        if (acceleratedSpeed <= 0) acceleratedSpeed = 1
        doCmd("YF $initSpeed $targetSpeed $acceleratedSpeed,WF 30 40 1", isAsync)
    }

    /**
     *  固定液压力(速度)
     * @param pressureLevel 压力1~12
     * @param isAsync 是否异步
     */
    fun fixativePressure(pressureLevel: Int, isAsync: Boolean)
    {
        // 最高速度值 400 600 80
        val initPressure = pressureLevel * 34
        val targetPressure = pressureLevel * 50
        val acceleratedPressure = pressureLevel * 7
        doCmd("NF $initPressure $targetPressure $acceleratedPressure", isAsync)
    }

    /** 喷固定液
     * @param methodParams 参数组
     * @param fixativeVol 固定液体积
     * @param isAsync 是否异步
     */
    fun fixativeLiquid(methodParams: MethodParams, fixativeVol: Int, isAsync: Boolean)
    {
        manualPause()

        // 固定液压力
        fixativePressure(methodParams.paramsSlideMode.fixativeDispensePressure, false)

        // 回抽容积（默认300步）
        //        val pumpbackVol = (300 / paramScale.n).toInt()
//        val pumpbackVol = 0
        // 固定液容积
//            ns(methodParams.paramsSlideMode.fixativeVolumnPerDrop + pumpbackVol, isAsync)
        ns(fixativeVol, isAsync)

//        // 回抽固定液容积（避免挂液）
//        ns(pumpbackVol * -1, isAsync)
    }

    /** 喷固定液
     * @param methodParams 参数组
     * @param fixativeVol 固定液体积
     * @param time 次数
     * @param isAsync 是否异步
     */
    fun fixativeLiquid(methodParams: MethodParams, fixativeVol: Int, time: Int, isAsync: Boolean)
    {
        for (i in 0 until time)
        {
            fixativeLiquid(methodParams, fixativeVol, isAsync)
        }
    }

    /**
     * 滴液速度
     * @param speedLevel 速度1~12
     * @param isAsync 是否异步
     */
    private fun dispenseSpeed(speedLevel: Int, isAsync: Boolean)
    {
        val initSpeedEach = paramGeneralParams.dispenseSpeedMaxInit.toFloat() / 12
        val targetSpeedEach = paramGeneralParams.dispenseSpeedMaxTarget.toFloat() / 12
        val acceleratedSpeedEach = paramGeneralParams.dispenseSpeedMaxAccelerated.toFloat() / 12

        var initSpeed = (initSpeedEach * speedLevel).roundToInt()
        var targetSpeed = (targetSpeedEach * speedLevel).roundToInt()
        var acceleratedSpeed = (acceleratedSpeedEach * speedLevel).roundToInt()

        if (initSpeed <= 0) initSpeed = 1
        if (targetSpeed <= 0) targetSpeed = 5
        if (acceleratedSpeed <= 0) acceleratedSpeed = 1
        doCmd("PF $initSpeed $targetSpeed $acceleratedSpeed", isAsync)
    }

    /**
     * 点亮试管摆放指示灯
     * @param pos 位置 1~64
     * @param callback 回调函数
     */
    fun lightTubeLed(pos: Int, callback: ICommandResultCallback)
    {
        doCmd("OE $pos", true, 10000, object : ICommandResultCallback
        {
            override fun success()
            {
                Log.i(TAG, "点亮指示灯 $pos 成功")
                callback.success()
            }

            override fun fail(ex: Exception)
            {
                Log.i(TAG, "点亮指示灯 $pos 失败")
                callback.fail(ex)
            }
        })
    }

    /**
     * 检测试管摆放
     * @param pos 位置 1~64
     * @param callback 回调函数
     */
    fun checkTubeLed(pos: Int, callback: ICommandResultCallback)
    {
        doCmd("OD $pos", true, 10000, object : ICommandResultCallback
        {
            override fun success()
            {
                // 判断返回值： OD0 无；OD1 有（延迟50ms执行判断）
                if (mDataCommunication!!.isTubePutOk)
                {
                    Log.i(TAG, "试管位置 $pos 摆放成功")
                    callback.success()
                }
                else
                {
                    Log.i(TAG, "试管位置 $pos 未摆放")
                    callback.fail(NoReachException())
                }
            }

            override fun fail(ex: Exception)
            {
                Log.i(TAG, "试管位置 $pos 摆放失败")
                callback.fail(ex)
            }
        })
    }

    /**
     * 关闭所有电机，恢复到运行前状态
     */
    fun closeAllMachine()
    {
        // 关闭喷雾
        closeSpray()
        // y轴和w轴恢复默认速度
        ResetYAndWSpeed()
    }

    /**
     * 恢复Y、W速度
     */
    fun ResetYAndWSpeed()
    {
        doCmd("YF0,WF0", false)
    }

    /**
     * 枪头检测位置
     * @param isAsync 是否异步
     */
    fun toTipCheckPos(isAsync: Boolean)
    {
        manualPause()
        za(0.0, isAsync)
        manualPause()
        xaya(paramPosTips.tipCheckPos.x, paramPosTips.tipCheckPos.y, isAsync)
        manualPause()
        za(paramPosTips.tipCheckHeight, isAsync)
    }

    /**
     * 检测取枪头状态
     * @return 是否取枪头成功
     */
    fun checkTakeTipStatus(): Boolean
    {
//        var result = false
//        mDataCommunication.tipExist = false
//
//        // 检测5次，只要有1次成功则认为取枪头成功
//        for (i in 0 until 5)
//        {
//            doCmd("AA2", isAsync = false)
//            result = result || mDataCommunication.tipExist
//            if (result) break
//        }
//
//        LogHelper.info(mContext.getString(R.string.tip_take_check) + "：" + result)
//        return result

        var result = false
        for (i in 0 until 5)
        {
            result = result || PipetteCompat.driver.hasTip()
            if (result) break
        }
        LogHelper.info(mContext.getString(R.string.tip_take_check) + "：" + result)
        return result
    }

    /**
     * 检测退枪头状态
     * @return 是否退枪头成功
     */
    fun checkReleaseTipStatus(): Boolean
    {
//        var result = true
//        mDataCommunication.tipExist = false
//
//        // 检测3次，全部成功则认为退枪头成功
//        for (i in 0 until 3)
//        {
//            doCmd("AA2", isAsync = false)
//            result = result && !mDataCommunication.tipExist
//            if (!result) break
//        }
//
//        LogHelper.info(mContext.getString(R.string.tip_release_check) + "：" + result)
//        return result

        var result = true
        for (i in 0 until 3)
        {
            result = result && !PipetteCompat.driver.hasTip()
            if (!result) break
        }
        LogHelper.info(mContext.getString(R.string.tip_release_check) + "：" + result)
        return result
    }

    /**
     * XS指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun xs(length: Double, isAsync: Boolean)
    {
        val xStep = (length * paramScale.x).toInt()
        doCmd("XS$xStep", isAsync)
    }

    /**
     * XA指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun xa(length: Double, isAsync: Boolean)
    {
        val xStep = (length * paramScale.x).toInt()
        doCmd("XA$xStep", isAsync)
    }

    /**
     * YS指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun ys(length: Double, isAsync: Boolean)
    {
        val yStep = (length * paramScale.y).toInt()
        doCmd("YS$yStep", isAsync)
    }

    /**
     * YA指令
     * @param  length 长度（mm）
     * @param isAsync 是否异步
     */
    fun ya(length: Double, isAsync: Boolean)
    {
        val yStep = (length * paramScale.y).toInt()
        doCmd("YA$yStep", isAsync)
    }

    /**
     *  XS、YS指令
     * @param  xLength
     * @param yLength
     * @param isAsync 是否异步
     */
    fun xsys(xLength: Double, yLength: Double, isAsync: Boolean)
    {
        val xStep = (xLength * paramScale.x).toInt()
        val yStep = (yLength * paramScale.y).toInt()

        doCmd("XS$xStep, YS$yStep", isAsync)
    }

    /**
     * XA、YA指令
     * @param  xLength
     * @param yLength
     * @param isAsync 是否异步
     */
    fun xaya(xLength: Double, yLength: Double, isAsync: Boolean)
    {
        val xStep = (xLength * paramScale.x).toInt()
        val yStep = (yLength * paramScale.y).toInt()

        doCmd("XA$xStep, YA$yStep", isAsync)
    }

    /**
     *  ZS、YS指令
     * @param  zLength
     * @param yLength
     * @param isAsync 是否异步
     */
    fun zsys(zLength: Double, yLength: Double, isAsync: Boolean)
    {
        val zStep = (zLength * paramScale.z).toInt()
        val yStep = (yLength * paramScale.y).toInt()

        doCmd("ZS$zStep, YS$yStep", isAsync)

        //        doCmd("YS$yStep,ZS$zStep", isAsync)
    }

    /**
     * ZS指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun zs(length: Double, isAsync: Boolean)
    {
        val zStep = (length * paramScale.z).toInt()
        doCmd("ZS$zStep", isAsync)
    }

    /**
     * ZA指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun za(length: Double, isAsync: Boolean)
    {
        val zStep = (length * paramScale.z).toInt()
        doCmd("ZA$zStep", isAsync)
    }

    /**
     * MA指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun ma(length: Double, isAsync: Boolean)
    {
        var mStep = (length * paramScale.m).toInt()
        doCmd("MA$mStep", isAsync)
    }

    /**
     * MS指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun ms(length: Double, isAsync: Boolean)
    {
        var mStep = (length * paramScale.m).toInt()
        doCmd("MS$mStep", isAsync)
    }

    /**
     * WA指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun wa(length: Double, isAsync: Boolean)
    {
        val mStep = (length * paramScale.w).toInt()
        doCmd("WA$mStep", isAsync)
    }

    /**
     * WS指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun ws(length: Double, isAsync: Boolean)
    {
        val zStep = (length * paramScale.z).toInt()
        doCmd("WS$zStep", isAsync)
    }

    /**
     *  WS、YS指令
     * @param  wLength
     * @param yLength
     * @param isAsync 是否异步
     */
    fun wsys(wLength: Double, yLength: Double, isAsync: Boolean)
    {
        val wStep = (wLength * paramScale.w).toInt()
        val yStep = (yLength * paramScale.y).toInt()

        doCmd("WS$wStep, YS$yStep", isAsync)
    }

    /**
     * PA指令
     * @param length 长度（mm）
     * @param isAsync 是否异步
     */
    fun pa(length: Double, isAsync: Boolean)
    {
//        var mStep = (length * paramScale.p).toInt()
//        doCmd("PA$mStep", isAsync)

        val volume = length.roundToInt()
        runPipette(isAsync) {
            when {
                volume > 0 -> PipetteCompat.driver.aspirate(volume)
                volume < 0 -> PipetteCompat.driver.dispense(-volume)
                else -> PipetteCompat.driver.dispense(0)
            }
        }
    }

    /**
     * PS指令（吸液前预吸空气）
     * @param volume 体积
     * @param isAsync 是否异步
     */
    fun psBeforeAbsorb(volume: Int, isAsync: Boolean)
    {
        runPipette(isAsync) {
            PipetteCompat.driver.firstAspirateBackAir(volume)
        }
    }

    /**
     * PS指令（吸液后气封）
     * @param volume 体积
     * @param isAsync 是否异步
     */
    fun psAfterAbsorb(volume: Int, isAsync: Boolean)
    {
        runPipette(isAsync) {
            PipetteCompat.driver.secondAspirateBackAir(volume)
        }
    }

    /**
     * PS指令
     * @param volume 体积
     * @param isAsync 是否异步
     */
    fun ps(volume: Int, isAsync: Boolean)
    {
//        val pStep = (volume * paramScale.p).toInt()
//        doCmd("NS$pStep", isAsync)

        runPipette(isAsync) {
            when {
                volume > 0 -> PipetteCompat.driver.aspirate(volume)
                volume < 0 -> PipetteCompat.driver.dispense(volume)
                else -> Unit
            }
        }
    }

    /**
     * NS指令
     * @param volume 体积
     * @param isAsync 是否异步
     */
    fun ns(volume: Int, isAsync: Boolean)
    {
        val pStep = (volume * paramScale.n).toInt()
        doCmd("NS$pStep", isAsync)
    }

    /**
     * X轴复位指令
     */
    fun xi(isAsync: Boolean)
    {
        doCmd("XI", isAsync)
    }

    /**
     * Y轴复位指令
     */
    fun yi(isAsync: Boolean)
    {
        doCmd("YI", isAsync)
    }

    /**
     * Z轴复位指令
     */
    fun zi(isAsync: Boolean)
    {
        doCmd("ZI", isAsync)
    }

    /**
     * M轴复位指令
     */
    fun mi(isAsync: Boolean)
    {
        doCmd("MI", isAsync)
    }

    /**
     * W轴复位指令
     */
    fun wi(isAsync: Boolean)
    {
        doCmd("WI", isAsync)
    }

    /**
     * P轴复位指令
     */
    fun pi(isAsync: Boolean)
    {
        doCmd("PI", isAsync)
    }

    /**
     * 发送指令
     * @param cmd 指令
     * @param isAsync 是否异步
     * @param timeOutMs 超时时间
     */
    fun doCmd(cmd: String, isAsync: Boolean, timeOutMs: Int = 30000, callback: ICommandResultCallback? = null): Boolean
    {
        if (cmd == "") return true

        if (!mDataCommunication!!.isConnectSuccess)
        {
            sleep(100)
            return true
        }

        // 如果程序运行中，且开门报警，则弹窗提示
        if (MainActivity.isProcedureRunning && paramGeneralParams.alarmDoor == EOnOff.On && mSerialClientHumiture.isAlarmDoor)
        {
            ViewUtils.pauseRunningDialog() // 模拟“暂停”按钮
            DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, MainActivity.mContext!!.getString(R.string.info_pls_close_door_and_continue), MainActivity.mContext!!.getString(R.string.yes), MainActivity.mContext!!.getString(R.string.no), true, true, object : WarnPromptListener
            {
                override fun clickedOk()
                {
                    ViewUtils.continueRunningDialog() // 模拟“继续”按钮
                }

                override fun clickedCancel()
                {
                    ViewUtils.closeRunningDialog() // 模拟“停止”按钮
                }
            }).show((MainActivity.mContext as AppCompatActivity).supportFragmentManager, null)
            manualPause()
        }

        LogHelper.info("将要执行指令：$cmd")

        // 已经过去的时间
        var timePassedMS = 0
        var result = false
        // 异步，不需要计算是否超时
        if (isAsync)
        {
            result = true
            if (callback != null)
            {
                mDataCommunication!!.doCmdAsync(cmd, timeOutMs, object : ICommandResultCallback
                {
                    override fun success()
                    {
                        callback?.success()
                    }

                    override fun fail(ex: Exception)
                    {
                        callback?.fail(ex)
                    }
                })
            }
            else
            {
                mDataCommunication!!.doCmdAsync(cmd, timeOutMs, null)
            }
        }
        // 同步，需要计算超时
        else
        {
            thread(isDaemon = true) {
                if (callback != null)
                {
                    result = mDataCommunication!!.doCmd(cmd, timeOutMs, object : ICommandResultCallback
                    {
                        override fun success()
                        {
                            callback?.success()
                        }

                        override fun fail(ex: Exception)
                        {
                            callback?.fail(ex)
                        }
                    })
                }
                else
                {
                    result = mDataCommunication!!.doCmd(cmd, timeOutMs, null)
                }
            }

            while (timePassedMS < timeOutMs)
            {
                if (result) break
                sleep(10)
                timePassedMS += 10
            }

            // 超时
            if (!result)
            {
                // 弹出超时提示框
                ViewUtils.pauseRunningDialog() // 模拟“暂停”按钮
                DialogFragment_Warn_Prompt.newInstance().setDialogContent(null, MainActivity.mContext!!.getString(R.string.info_cmd_timeOut_1) + cmd + MainActivity.mContext!!.getString(R.string.info_cmd_timeOut_2), MainActivity.mContext!!.getString(R.string.yes), MainActivity.mContext!!.getString(R.string.no), true, true, object : WarnPromptListener
                {
                    override fun clickedOk()
                    {
                        ViewUtils.continueRunningDialog() // 模拟“继续”按钮
                    }

                    override fun clickedCancel()
                    {
                        ViewUtils.closeRunningDialog() // 模拟“停止”按钮
                    }
                }).show((MainActivity.mContext as AppCompatActivity).supportFragmentManager, null)
                manualPause()
            }
        }

        LogHelper.info("已经执行指令：$cmd")

        return result
    }

    /**
     * 暂停
     * @param stopCallback 停止回调函数
     */
    @Throws(ManualStopException::class)
    fun manualPause(stopCallback: InfoPromptListener? = null)
    {
        while (isManualPause)
        {
            if (isManualStop)
            {
                stopCallback?.clicked()
                throw ManualStopException()
            }
            try
            {
                sleep(10)
            }
            catch (e: InterruptedException)
            {
                throw InterruptedException()
            }
        }
    }

    /**
     * 运行移液器操作
     * @param isAsync 是否异步执行
     * @param block 执行的操作
     */
    private fun runPipette(isAsync: Boolean, block: () -> Unit)
    {
        if (isAsync)
        {
            thread(isDaemon = true) { block() }
        }
        else
        {
            block()
        }
    }
}