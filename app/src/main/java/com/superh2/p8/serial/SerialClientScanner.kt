package com.superh2.p8.serial

import android.util.Log
import com.example.x6.serial.SerialPort
import com.superh2.library.myInterface.IScannerResultCallback
import com.superh2.library.utils.CRC16Utils
import com.superh2.library.utils.DataHelper
import com.superh2.library.utils.LogHelper
import com.superh2.p8.MainActivity.Companion.mContext
import com.superh2.p8.R
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * @Description 扫码模块串口通信客户端
 * @Author liu_ja
 * @CreateDate 2020/05/05 14:02
 */
class SerialClientScanner
{
    private val TAG = "SerialClientScanner"

    private val SERIAL_PORT = "ttyS3"

    private var serialttyS: SerialPort? = null
    private var ttySInputStream: InputStream? = null
    private var ttySOutputStream: OutputStream? = null

    // 是否心跳测试指令
    private var isHeartBeat = false
    // 应答ACK
    private val ACK_NORMAL = byteArrayOf(0x04, 0xD0.toByte(), 0x00, 0x00, 0xFF.toByte(), 0x2C)

    // 是否已经连接
    var isConnected = false

    // 最新接收、发送
    var lastReceiveTime: Long = 0
    var lastSendTime: Long = 0
    // 最新接收信息
    var lastReceiveDecodeStr = ""

    // 超时
    var BEAT_TIMEOUT = 3000L
    var DECODE_TIMEOUT = 2000L

    /**
     * 打开串口
     */
    fun openSerial()
    {
        try
        {
            serialttyS = SerialPort(File("/dev/${SERIAL_PORT}"), 9600, 0)
            ttySInputStream = serialttyS!!.inputStream
            ttySOutputStream = serialttyS!!.outputStream

            // 监听接收事件
            val thread = Thread { dataReceive() }
            thread.priority = 8
            thread.start()

            // 心跳测试
            heartBeat(object : IScannerResultCallback
            {
                override fun success(info: String)
                {
                    isConnected = true
                    Log.w(TAG, "心跳测试成功")
                }

                override fun timeOut()
                {
                    isConnected = false
                    Log.w(TAG, "心跳测试超时")
                }
            })
        }
        catch (e: java.lang.Exception)
        {
            LogHelper.error("机器状态：" + e.message)
            isConnected = false
        }
        catch (e: SecurityException)
        {
            LogHelper.error("机器状态：" + e.message)
            isConnected = false
        }
        catch (e: Exception)
        {
            LogHelper.error("机器状态：" + e.message)
            isConnected = false
        }
    }

    /**
     * 关闭串口
     */
    fun closeSerial()
    {
        try
        {
            ttySInputStream?.close()
            ttySOutputStream?.close()
            serialttyS?.close()
            isConnected = false
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 心跳测试（用于判断是否连接正常）
     */
    fun heartBeat(scannerResultCallback: IScannerResultCallback)
    {
        isHeartBeat = true
        // 用开机提示音测试是否有心跳
        doCmd("心跳测试", byteArrayOf(0x08, 0xC6.toByte(), 0x04, 0x08, 0x00, 0xF2.toByte(), 0x0D, 0x01, 0xFE.toByte(), 0x26))
        checkReceiveData(BEAT_TIMEOUT, scannerResultCallback)
    }

    /**
     * 开始解码
     */
    fun decodeStart(scannerResultCallback: IScannerResultCallback)
    {
        isHeartBeat = false
        doCmd("开始解码", byteArrayOf(0x04, 0xE4.toByte(), 0x04, 0x00, 0xFF.toByte(), 0x14))
        checkReceiveData(DECODE_TIMEOUT, scannerResultCallback)
    }

    /**
     * 停止解码
     */
    fun decodeStop(scannerResultCallback: IScannerResultCallback)
    {
        isHeartBeat = false
        doCmd("停止解码", byteArrayOf(0x04, 0xE4.toByte(), 0x04, 0x00, 0xFF.toByte(), 0x13))
        checkReceiveData(DECODE_TIMEOUT, scannerResultCallback)
    }

    /**
     * 发送指令
     * @param tag 标签
     * @param cmd 指令
     */
    fun doCmd(tag: String, cmd: ByteArray): Boolean
    {
        lastReceiveDecodeStr = ""
        lastSendTime = System.currentTimeMillis()
        ttySOutputStream?.write(cmd)
        LogHelper.info("扫码+ ${tag}-发送报文：${CRC16Utils.conver16HexStr(cmd)}")
        return true
    }

    /**
     * 数据接收
     */
    private fun dataReceive()
    {
        var inputs = ByteArray(1024)
        var bytesReadThisTime = 0

        while (true)
        {
            bytesReadThisTime = ttySInputStream?.read(inputs)!!
            val bytes: ByteArray = inputs.copyOfRange(0, bytesReadThisTime)
            // 判断 bytes 是否为空
            if (bytes.isEmpty())
            {
                continue
            }

            var receivedStr = DataHelper.byteArray2String(bytes)
            lastReceiveTime = System.currentTimeMillis()
//            LogHelper.info("扫码-响应报文：${receivedStr}")
//            Log.d("扫码-响应报文", receivedStr)

            // 判断是应答还是解码数据
            if (bytes.contentEquals(ACK_NORMAL))
            {
                isConnected = true
                lastReceiveDecodeStr = mContext.getString(R.string.response)
            }
            else lastReceiveDecodeStr = receivedStr
        }
    }

    /**
     * 检查接收数据
     * @param timeOutMS 超时时间
     * @param callback 回调函数
     */
    private fun checkReceiveData(timeOutMS: Long, callback: IScannerResultCallback)
    {
        var lastTick = System.currentTimeMillis()
        while (System.currentTimeMillis() - lastTick < timeOutMS)
        {
            Thread.sleep(10)
            if (lastReceiveTime > lastSendTime && lastReceiveDecodeStr.isNotEmpty())
            {
                if (isHeartBeat)
                {
                    callback.success("")
                    return
                }
                else if (!lastReceiveDecodeStr.contentEquals(mContext.getString(R.string.response)))
                {
                    callback.success(lastReceiveDecodeStr)
                    return
                }
            }
        }

        callback.timeOut()
    }
}