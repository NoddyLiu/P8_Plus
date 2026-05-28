package com.superh2.p8.serial

import android.content.Context
import android.util.Log
import com.example.x6.serial.SerialPort
import com.superh2.library.myEntityCommon.SerialCmdData
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myEnum.ESerialOperation
import com.superh2.library.myInterface.ICommandResultCallback
import com.superh2.library.utils.DataHelper
import com.superh2.library.utils.ParamsHelper
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread
import kotlin.math.abs


/**
 * Description: 温湿度串口通信客户端
 * Author: liu_ja
 * Created On: 2020/11/2 11:35
 */
class SerialClientHumiture
{
    val TAG = "SerialClientHumiture"

    var mContext: Context? = null

    private var serialttyS1: SerialPort? = null
    // 设备节点号
    private var serialttySNum = "ttyS2"
    // 输入输出
    var ttyS1InputStream: InputStream? = null
    var ttyS1OutputStream: OutputStream? = null
    // 是否已经连接串口
    var isConnected = false
    // 数据接收buffer
    var dataReceivedBuffer = ByteArray(1024)
    // 超时毫秒
    val TIME_OUT_MS = 150
    // 发送指令队列（FIFO）
    var cmdSendQueue: BlockingQueue<SerialCmdData> = LinkedBlockingDeque<SerialCmdData>()

    /**
     *  系统状态
     */
    // 系统是否启动
    var isSysStart = false
    // 系统是否温度演算
    var isTempAuto = false
    // 照明是否启动
    var isLightOn = true
    // 是否开门报警
    var isAlarmDoor = false

    /**
     *  数据
     */
    // 温度实时值
    var tempValueRealTime = 0.0f
    // 温度设定值
    var tempValueSet = 0.0f
    // 湿度实时值
    var humValueRealTime = 0.0f
    // 湿度设定值
    var humValueSet = 0.0f

    /**
     * 自启动
     */
    // 温度是否稳定
    private var isTempStable = false
    // 湿度是否稳定
    private var isHumStable = false
    // 温湿度稳定开始时间
    private var stableStartTime: Long = 0
    // 稳定时间，默认3分钟（单位：毫秒）
    private val stabilityDuration = 3 * 60 * 1000L


    /**
     * 打开串口
     */
    fun openSerial(): Boolean
    {
        try
        {
            serialttyS1 = SerialPort(File("/dev/$serialttySNum"), 115200, 0)
            ttyS1InputStream = serialttyS1?.inputStream
            ttyS1OutputStream = serialttyS1?.outputStream
            isConnected = true

            // 开启发送指令线程
            thread {
                sendData()
            }
        }
        catch (e: IOException)
        {
            e.printStackTrace()
            isConnected = false
        }
        catch (e: SecurityException)
        {
            e.printStackTrace()
            isConnected = false
        }

        return isConnected
    }

    /**
     * 添加指令进入发送队列
     */
    private fun addCmd2Queue(serialCmdData: SerialCmdData)
    {
        cmdSendQueue.add(serialCmdData)
    }

    /**
     *  查询数据
     */
    fun queryData(so: ESerialOperation, callback: ICommandResultCallback?)
    {
        // 温度实时值
        if (so == ESerialOperation.TempRealTime)
        {
            addCmd2Queue(SerialCmdData(byteArrayOf(0x02, 0x30, 0x31, 0x52, 0x53, 0x44, 0x2C, 0x30, 0x32, 0x2C, 0x30, 0x30, 0x30, 0x31, 0x43, 0x35, 0x0D, 0x0A), ESerialOperation.TempRealTime, callback))
        }
        // 温度设定值
        if (so == ESerialOperation.TempSet)
        {
            addCmd2Queue(SerialCmdData(byteArrayOf(0x02, 0x30, 0x31, 0x52, 0x53, 0x44, 0x2C, 0x30, 0x32, 0x2C, 0x30, 0x30, 0x30, 0x31, 0x43, 0x35, 0x0D, 0x0A), ESerialOperation.TempSet, callback))
        }
        // 湿度实时值
        if (so == ESerialOperation.HumRealTime)
        {
            addCmd2Queue(SerialCmdData(byteArrayOf(0x02, 0x30, 0x31, 0x52, 0x53, 0x44, 0x2C, 0x30, 0x32, 0x2C, 0x30, 0x30, 0x30, 0x35, 0x43, 0x39, 0x0D, 0x0A), ESerialOperation.HumRealTime, callback))
        }
        // 湿度设定值
        if (so == ESerialOperation.HumSet)
        {
            addCmd2Queue(SerialCmdData(byteArrayOf(0x02, 0x30, 0x31, 0x52, 0x53, 0x44, 0x2C, 0x30, 0x32, 0x2C, 0x30, 0x30, 0x30, 0x35, 0x43, 0x39, 0x0D, 0x0A), ESerialOperation.HumSet, callback))
        }
        // 开门报警状态
        if (so == ESerialOperation.AlarmDoor)
        {
            // 地址：D0019
            addCmd2Queue(SerialCmdData(byteArrayOf(0x02, 0x30, 0x31, 0x52, 0x53, 0x44, 0x2C, 0x30, 0x31, 0x2C, 0x30, 0x30, 0x31, 0x39, 0x43, 0x44, 0x0D, 0x0A), ESerialOperation.AlarmDoor, callback))
        }
    }

    /**
     *  查询系统状态
     */
    fun queryStatus(callback: ICommandResultCallback?)
    {
        addCmd2Queue(SerialCmdData(byteArrayOf(0x02, 0x30, 0x31, 0x52, 0x53, 0x44, 0x2C, 0x30, 0x31, 0x2C, 0x30, 0x30, 0x31, 0x30, 0x43, 0x34, 0x0D, 0x0A), ESerialOperation.SysStatus, callback))
    }

    /**
     * 关闭串口
     */
    fun closeSerial()
    {
        try
        {
            serialttyS1?.close()
            isConnected = false
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 发送数据到下位机
     */
    private fun sendData()
    {
        // 轮询队列
        while (true)
        {
            var serialCmdData = cmdSendQueue.take()
            if (null != serialCmdData)
            {
                var data = serialCmdData.data
                var callback = serialCmdData.callback
                var serialOperation = serialCmdData.serialOperation

                // 连接下位机成功
                if (isConnected)
                {
                    try
                    {
                        Log.i(TAG, "正在发送指令到串口")

                        // 发送数据
                        ttyS1OutputStream?.write(data)

                        // 接收数据
                        var totalWaitMs = 0 // 一共等待的毫秒数
                        while (true)
                        {
                            if (ttyS1InputStream!!.available() <= 0)
                            {
                                totalWaitMs += 10
                                Thread.sleep(10)

                                if (totalWaitMs >= TIME_OUT_MS)
                                {
                                    callback?.fail(TimeoutException())
                                    return
                                }
                            }
                            else break
                        }

                        // 延迟150ms，预防读取数据不全
                        Thread.sleep(150)

                        // 读取数据
                        val num = ttyS1InputStream!!.read(dataReceivedBuffer)
                        if (num > 0)
                        {
                            val buffer2 = dataReceivedBuffer.copyOfRange(0, num)
                            Log.d(TAG, "收到的数据：" + DataHelper.byte2HexWithSpace(buffer2))

                            /**
                             * 解析数据
                             */
                            when (serialOperation)
                            {
                                // 温度实时值
                                ESerialOperation.TempRealTime ->
                                {
                                    // 先转为16进制ASCII字符串，再转为整形，再*0.01
                                    tempValueRealTime = DataHelper.float2Decimal(String(buffer2.copyOfRange(10, 14)).toInt(16) * 0.01f, 2)
                                    callback?.success()
                                }
                                // 温度设定值
                                ESerialOperation.TempSet ->
                                {
                                    // 先转为16进制ASCII字符串，再转为整形，再*0.01
                                    tempValueSet = DataHelper.float2Decimal(String(buffer2.copyOfRange(15, 19)).toInt(16) * 0.01f, 2)
                                    callback?.success()
                                }
                                // 湿度实时值
                                ESerialOperation.HumRealTime ->
                                {
                                    // 先转为16进制ASCII字符串，再转为整形，再*0.01
                                    humValueRealTime = DataHelper.float2Decimal(String(buffer2.copyOfRange(10, 14)).toInt(16) * 0.1f, 1)
                                    callback?.success()
                                }
                                // 湿度设定值
                                ESerialOperation.HumSet ->
                                {
                                    // 先转为16进制ASCII字符串，再转为整形，再*0.01
                                    humValueSet = DataHelper.float2Decimal(String(buffer2.copyOfRange(15, 19)).toInt(16) * 0.1f, 1)
                                    callback?.success()
                                }
                                // 设置温湿度值返回
                                ESerialOperation.SetTemp, ESerialOperation.SetHum ->
                                {
                                    if (buffer2[7] == 0x4F.toByte() && buffer2[8] == 0x4B.toByte()) callback?.success()
                                    else callback?.fail(Exception())
                                }
                                // 系统状态
                                ESerialOperation.SysStatus ->
                                {
                                    isSysStart = DataHelper.isChecked(buffer2[13].toInt(), 1)
                                    isTempAuto = DataHelper.isChecked(buffer2[12].toInt(), 1)
                                    callback?.success()
                                }
                                // 温度自动演算
                                ESerialOperation.TempAuto ->
                                {
                                    if (buffer2[7] == 0x4F.toByte() && buffer2[8] == 0x4B.toByte()) callback?.success()
                                    else callback?.fail(Exception())
                                }
                                //                        // 照明灯
                                //                        ESerialOperation.Light ->
                                //                        {
                                //                            isLightOn = DataConvert.isChecked(buffer2[13].toInt(), 1)
                                //                            callback?.success()
                                //                        }
                                // 开门报警
                                ESerialOperation.AlarmDoor ->
                                {
                                    isAlarmDoor = DataHelper.isChecked(buffer2[13].toInt(), 0)
                                    callback?.success()
                                }
                            }
                        }
                    }
                    catch (ioEx: IOException)
                    {
                        try
                        {
                            ttyS1InputStream?.close()
                            this.closeSerial()
                        }
                        catch (ex: Exception)
                        {
                        }
                        finally
                        {
                        }
                    }
                }
                else
                {
                    // 重新打开串口连接
                    openSerial()
                    return
                }
            }
        }
    }

    /**
     * 设置温湿度
     * @param value 湿度值
     * @param mSerialOperation
     * @param callback 回调函数
     */
    fun setHumiture(value: Float, mSerialOperation: ESerialOperation, callback: ICommandResultCallback?)
    {
        // 温度*100，湿度*10
        var inPutValue = if (mSerialOperation == ESerialOperation.SetTemp) (value * 100).toInt()
        else (value * 10).toInt()

        // 构造帧
        var frameArray = ByteArray(23)
        frameArray[0] = 0x02
        frameArray[1] = 0x30
        frameArray[2] = 0x31
        frameArray[3] = 0x57
        frameArray[4] = 0x53
        frameArray[5] = 0x44
        frameArray[6] = 0x2C
        frameArray[7] = 0x30
        frameArray[8] = 0x31
        frameArray[9] = 0x2C
        frameArray[10] = 0x30
        frameArray[11] = 0x31
        frameArray[12] = 0x30
        if (mSerialOperation == ESerialOperation.SetTemp) frameArray[13] = 0x32
        else frameArray[13] = 0x33
        frameArray[14] = 0x2C
        // 数值
        var valueArray = DataHelper.int2Hex(inPutValue).toByteArray(StandardCharsets.US_ASCII)
        frameArray[15] = valueArray[0]
        frameArray[16] = valueArray[1]
        frameArray[17] = valueArray[2]
        frameArray[18] = valueArray[3]
        // 总数
        var addValue = 0
        for (i in 1 until 19)
        {
            addValue += frameArray[i]
        }
        val addValueLowByte = addValue and 0xFF

        // SUM
        var sumArray = addValueLowByte.toString(16).toUpperCase().toByteArray(StandardCharsets.US_ASCII)
        frameArray[19] = sumArray[0]
        frameArray[20] = sumArray[1]

        frameArray[21] = 0x0D
        frameArray[22] = 0x0A

        addCmd2Queue(SerialCmdData(frameArray, mSerialOperation, callback))
    }

    /**
     * 系统开关
     * @param onOff
     */
    fun powerToggle(onOff: EOnOff)
    {
        // 构造帧
        var frameArray = ByteArray(23)
        // STX
        frameArray[0] = 0x02
        // 地址01
        frameArray[1] = 0x30
        frameArray[2] = 0x31
        // WRD
        frameArray[3] = 0x57
        frameArray[4] = 0x52
        frameArray[5] = 0x44
        // 逗号，
        frameArray[6] = 0x2C
        // 个数：1个
        frameArray[7] = 0x30
        frameArray[8] = 0x31
        // 逗号，
        frameArray[9] = 0x2C
        // D-Register寄存器 0101
        frameArray[10] = 0x30
        frameArray[11] = 0x31
        frameArray[12] = 0x30
        frameArray[13] = 0x31
        // 逗号，
        frameArray[14] = 0x2C
        // 数值
        frameArray[15] = 0x30
        frameArray[16] = 0x30
        frameArray[17] = 0x30
        if (onOff == EOnOff.On) frameArray[18] = 0x31
        else frameArray[18] = 0x34

        // 总数
        var addValue = 0
        for (i in 1 until 19)
        {
            addValue += frameArray[i]
        }
        val addValueLowByte = addValue and 0xFF

        // SUM
        var sumArray = addValueLowByte.toString(16).toUpperCase().toByteArray(StandardCharsets.US_ASCII)
        frameArray[19] = sumArray[0]
        frameArray[20] = sumArray[1]

        frameArray[21] = 0x0D
        frameArray[22] = 0x0A

        addCmd2Queue(SerialCmdData(frameArray, ESerialOperation.Toggle, null))
    }

    /**
     * 系统开关
     * @param onOff
     */
    fun lightToggle(onOff: EOnOff)
    {
        // 构造帧
        var frameArray = ByteArray(23)
        // STX
        frameArray[0] = 0x02
        // 地址01
        frameArray[1] = 0x30
        frameArray[2] = 0x31
        // WRD
        frameArray[3] = 0x57
        frameArray[4] = 0x52
        frameArray[5] = 0x44
        // 逗号，
        frameArray[6] = 0x2C
        // 个数：1个
        frameArray[7] = 0x30
        frameArray[8] = 0x31
        // 逗号，
        frameArray[9] = 0x2C
        // D-Register寄存器 0160
        frameArray[10] = 0x30
        frameArray[11] = 0x31
        frameArray[12] = 0x36
        frameArray[13] = 0x30
        // 逗号，
        frameArray[14] = 0x2C
        // 数值
        frameArray[15] = 0x30
        frameArray[16] = 0x30
        frameArray[17] = 0x30
        if (onOff == EOnOff.On) frameArray[18] = 0x31
        else frameArray[18] = 0x30

        // 总数
        var addValue = 0
        for (i in 1 until 19)
        {
            addValue += frameArray[i]
        }
        val addValueLowByte = addValue and 0xFF

        // SUM
        var sumArray = addValueLowByte.toString(16).toUpperCase().toByteArray(StandardCharsets.US_ASCII)
        frameArray[19] = sumArray[0]
        frameArray[20] = sumArray[1]

        frameArray[21] = 0x0D
        frameArray[22] = 0x0A

        addCmd2Queue(SerialCmdData(frameArray, ESerialOperation.Light, null))
    }

    /**
     * 检查温湿度稳定性
     */
    fun checkHumitureStability(): Boolean
    {
        var result = false

        // 判断温湿度是否在设定值±0.2范围内
        var  humitureStabilityThreshold = ParamsHelper.paramGeneralParams.humitureStabilityThreshold
        isTempStable = abs(tempValueRealTime - tempValueSet) <= humitureStabilityThreshold
        isHumStable = abs(humValueRealTime - humValueSet) <= humitureStabilityThreshold

        // 如果温湿度都稳定，开始计时
        if (isTempStable && isHumStable)
        {
            if (stableStartTime == 0L)
            {
                stableStartTime = System.currentTimeMillis()
            }
            else if (System.currentTimeMillis() - stableStartTime >= stabilityDuration)
            {
                // 如果稳定时间超过设定值，启动实验程序
                result = true
            }
        }
        else
        {
            // 如果不稳定，重置计时
            stableStartTime = 0L
        }

        return result
    }
}