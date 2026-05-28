package com.superh2.library.comm

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.superh2.library.R
import com.superh2.library.myException.NoResponseException
import com.superh2.library.myException.TimeOutException
import com.superh2.library.myInterface.ICommandResultCallback
import com.superh2.library.utils.LogHelper
import com.superh2.library.utils.ParamsHelper.paramScale
import org.apache.commons.lang3.StringUtils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.*
import java.util.ArrayList


/** 通信类
 * Created by Noddy on 2018/4/23.
 */
class DataCommunication(private val mContext: Context, private val mIsTcp: Boolean = true) : Runnable
{
    private val TAG = "DataCommunication"

    private val deviceName = arrayOf("X", "Y", "Z", "M", "N", "P", "Q", "V", "W", "T1", "T2", "PO1", "PO2", "PO3", "PO4", "PO5", "A", "O", "PO6", "PO7", "PO8", "XX", "YY", "ZZ", "MM", "NN", "PP", "QQ", "VV", "WW", "Z1", "Z2", "Z3", "Z4", "Z5", "Z6", "Z7", "Z8", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8")
    private val deviceAddr = intArrayOf(11, 12, 13, 16, 17, 21, 22, 23, 14, 31, 32, 41, 42, 43, 44, 45, 99, 50, 46, 47, 48, 1, 2, 3, 6, 7, 8, 9, 10, 4, -55, -54, -53, -52, -51, -50, -49, -48, -35, -34, -33, -32, -31, -30, -29, -28)
    private val deviceList = ArrayList<Device>()

    /**
     * 网络连接
     */
    lateinit var desAddress: InetAddress
    // CanNet地址
    private val mDesIP = "192.168.1.3"
    private var mDesPort = 4000

    // udp
    lateinit var udpSocket: DatagramSocket
    private val mLocalPort = 4000

    // tcp
    lateinit var tcpSocket: Socket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream

    // 网络连接是否成功
    var isConnectSuccess = false

    // 是否停止连接
    var isStop = false

    var totalReciveBytes = 0
    private val socketRecBuffer = ByteArray(1024)
    private var buffPos = 0

    private val deviceToControl = ArrayList<Device>()

    // 指令执行结果
    private var result: ECommandResult? = null

    // 上次发送指令时间
    private var lastCmdSendTimeMillis = 0L

    // 供前端使用
    var isTubePutOk: Boolean = false // 试管是否摆放好
//    var tempBase = 0 // 底板温度
//    var tempSteam = 0 // 蒸汽温度
    var tempEnvironment = 0 // 环境温度
    var humRelative = 0 // 相对湿度

    /**
     * 枪头检测传感器
     */
    // 枪头是否存在
    var tipExist = false

    /**
     * 喷雾加液液位已满
     * 置true为两种情况：
     * 1、加液已到液位传感器报警
     * 2、加液走完所有步数，但未到液位传感器报警（即原来的逻辑）
     */
    var isSprayWaterAddDone = false


    /**
     * 获取本地ip
     *
     * @return
     * @throws SocketException
     */
    private // 遍历10次，如果都没获取到192.168地址，就返回null
    // 获取ipv4地址
    val localIPAddress: String?
        @Throws(SocketException::class) get()
        {
            val loopCount = 10
            for (i in 0 until loopCount)
            {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements())
                {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements())
                    {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address)
                        {
                            val ip = inetAddress.getHostAddress().toString()
                            if (ip.startsWith("192.168")) return ip
                        }
                    }
                }

                try
                {
                    Thread.sleep(500)
                }
                catch (e: InterruptedException)
                {
                    e.printStackTrace()
                }

            }
            return null
        }

    override fun run()
    {
        for (i in deviceName.indices)
        {
            val device = Device()
            device.name = deviceName[i]
            device.canAddr = deviceAddr[i]
            device.pos = 0
            deviceList.add(device)
        }

        // socket初始化
        if (canNetThread())
        // 连接成功
        {
            isConnectSuccess = true
        }
        else
        // 连接失败
        {
            isConnectSuccess = false
            val activity = mContext as Activity
            activity.runOnUiThread { Toast.makeText(mContext, mContext.getString(R.string.info_network_connection_failed), Toast.LENGTH_LONG).show() }
            return
        }


        val thread = Thread(Runnable { thSocketReceive() })
        thread.priority = 8
        thread.start()
    }

    private fun canNetThread(): Boolean
    {
        try
        {
            desAddress = InetAddress.getByName(this.mDesIP)
        }
        catch (e2: UnknownHostException)
        {
            e2.printStackTrace()
            LogHelper.error(e2.stackTraceToString())
        }

        try
        {
            // Tcp
            if (mIsTcp)
            {
                mDesPort += 1
                tcpSocket = Socket(desAddress, mDesPort)
                tcpSocket.keepAlive = true
                inputStream = tcpSocket.getInputStream()
                outputStream = tcpSocket.getOutputStream()
            }
            // Udp
            else
            {
                val localIp = localIPAddress
                if (localIp != null)
                // 本地ip、端口
                    udpSocket = DatagramSocket(mLocalPort, InetAddress.getByName(localIp))
                else return false
            }
        }
        catch (ex: UnknownHostException)
        {
            return false
        }
        catch (e: SocketException)
        {
            e.printStackTrace()
            LogHelper.error(e.stackTraceToString())
            return false
        }

        return true
    }

    /**
     * 关闭连接
     */
    fun closeConnection()
    {
        if (this::udpSocket.isInitialized && !mIsTcp)
        {
            udpSocket.close()
        }
        else if (this::tcpSocket.isInitialized)
        {
            inputStream.close()
            outputStream.close()
            tcpSocket.close()
        }
    }

    /**
     * 指令发送(单指令)，同步处理
     *
     * @param cText
     * @param timeOutMS 接收执行返回超时
     * @param callback  执行结果回调函数
     * @return
     */
    @Synchronized
    fun doCmd(cText: String?, timeOutMS: Int, callback: ICommandResultCallback?): Boolean
    {
        // 预防指令间发送过于频繁，造成指令返回应答异常
        if (System.currentTimeMillis() - lastCmdSendTimeMillis < 50)
            Thread.sleep(50)

        val cmdText: String?
        try
        {
            cmdText = cmdPreProcess(cText)
            if (cmdText.isNullOrEmpty())
            // 空指令
            {
                callback?.success()
                return true
            }

            if (cmdText.toUpperCase().startsWith("WAIT"))
            // 暂停指令
            {
                val waitTime = Integer.parseInt(cmdText.substring(4).trim { it <= ' ' })
                Thread.sleep((waitTime * 100).toLong())
                callback?.success()
                return true
            }

            lastCmdSendTimeMillis = System.currentTimeMillis()
            val thSendCmd = Thread(Runnable { result = thSendCmd(timeOutMS, cmdText) })
            thSendCmd.start()
            thSendCmd.join()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            // 在这里判断指令执行情况，交给前端做处理
            when (result)
            {
                ECommandResult.TimeOut -> // 指令执行超时
                {
                    callback?.fail(TimeOutException())
                    return false
                }
                ECommandResult.NoResponse -> // 指令执行没有响应
                {
                    callback?.fail(NoResponseException())
                    return false
                }
                else ->// 指令执行成功
                {
                    callback?.success()
                    return true
                }
            }
        }
    }

    /**
     * 指令发送(单指令)，异步处理
     *
     * @param cText
     * @param timeOutMS 接收执行返回超时
     * @param callback  执行结果回调函数
     */
    fun doCmdAsync(cText: String, timeOutMS: Int, callback: ICommandResultCallback?)
    {
        // 预防指令间发送过于频繁，造成指令返回应答异常
        if (System.currentTimeMillis() - lastCmdSendTimeMillis < 50)
            Thread.sleep(50)

        val cmdText: String?
        try
        {
            cmdText = cmdPreProcess(cText)
            if (cmdText.isNullOrEmpty())
            // 空指令
            {
                callback?.success()
                return
            }

            if (cmdText.toUpperCase().startsWith("WAIT"))
            // 暂停指令
            {
                val waitTime = Integer.parseInt(cmdText.substring(4).trim { it <= ' ' })
                Thread.sleep((waitTime * 100).toLong())
                callback?.success()
                return
            }

            lastCmdSendTimeMillis = System.currentTimeMillis()
            val thSendCmd = Thread(Runnable {
                result = thSendCmd(timeOutMS, cmdText)
                // 在这里判断指令执行情况，交给前端做处理
                if (null != callback)
                {
                    when (result)
                    {
                        ECommandResult.TimeOut -> // 指令执行超时
                        {
                            callback?.fail(TimeOutException())
                        }
                        ECommandResult.NoResponse -> // 指令执行没有响应
                        {
                            callback?.fail(NoResponseException())
                        }
                        else ->// 指令执行成功
                        {
                            callback?.success()
                        }
                    }
                }
            })
            thSendCmd.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    /**
     * 指令发送前对文本预处理
     *
     * @param cmdTextOrg
     * @return
     */
    private fun cmdPreProcess(cmdTextOrg: String?): String
    {
        var cmdTextOrg = cmdTextOrg

        cmdTextOrg = cmdTextOrg!!.trim { it <= ' ' }
        // 找// 或 --
        val pos: Int
        var pos1 = cmdTextOrg.indexOf("//")
        var pos2 = cmdTextOrg.indexOf("--")
        pos1 = if (pos1 >= 0) pos1 else Integer.MAX_VALUE
        pos2 = if (pos2 >= 0) pos2 else Integer.MAX_VALUE
        pos = Math.min(pos1, pos2)
        return if (pos == Integer.MAX_VALUE) cmdTextOrg else cmdTextOrg.substring(0, pos).trim { it <= ' ' }
    }

    fun thSendCmd(timeOutMS: Int, cmdText: String?): ECommandResult
    {
        isStop = false

        var lastTick: Long
        var byteMessage: ByteArray? = null
        var cmdList: Array<String>?

        cmdList = cmdText!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        buffPos = 0

        deviceToControl.clear()
        var device: Device? = null
        for (oneCmd in cmdList)
        {
            try
            {
                if (oneCmd == "")
                    continue
                Thread.sleep(5)
                byteMessage = getSendData(oneCmd.trim { it <= ' ' }.toUpperCase())
            }
            catch (ex: Exception)
            {
                ex.message
            }

            for (subDevice in deviceList)
            {
                if (subDevice.canAddr == byteMessage!![4].toInt())
                {
                    device = subDevice
                    break
                }
            }
            deviceToControl.add(device!!)

            device!!.CleanReceiveData()
            device.lastSendTime = System.currentTimeMillis()
            device.LastCmdText = oneCmd

            try
            {
                if (mIsTcp)
                {
                    outputStream.write(byteMessage)
                }
                else
                {
                    val packet = DatagramPacket(byteMessage, byteMessage!!.size, desAddress, mDesPort)
                    udpSocket?.send(packet)
                }
            }
            catch (e: IOException)
            {
                Log.d(TAG, "执行结果：发送失败！ 地址: "+device.canAddr)
                e.printStackTrace()
                return ECommandResult.SendFail
            }

            try
            {
                if (oneCmd.trim { it <= ' ' }.length >= 2 && oneCmd.trim { it <= ' ' }.substring(1, 1).toUpperCase() == "T")
                //如果是温度指令，则延时后跳过检测
                {
                    Thread.sleep(50)
                    continue
                }

                // 指令是否接收成功
                lastTick = System.currentTimeMillis()
                var isAns = false
                while (System.currentTimeMillis() - lastTick < 1000)
                {
                    Thread.sleep(10)
                    if (isStop)
                        return ECommandResult.HaltExec
                    if (isDeviceAnswer(device))
                    {
                        isAns = true
                        break
                    }
                }

                if (!isAns)
                {
                    Log.d(TAG, "执行结果：发送失败！ 原因: " + device.canAddr+" 设备没有回应")
                    isStop = true
                    return ECommandResult.NoResponse
                }
                Thread.sleep(20)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

        }

        // 指令是否执行成功
        lastTick = System.currentTimeMillis()
        var isOk = false
        while (System.currentTimeMillis() - lastTick < timeOutMS + cmdList.size * 10000)
        {
            if (isStop)
                return ECommandResult.HaltExec

            try
            {
                Thread.sleep(2)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            if (isAllMotionCmdFinished(deviceToControl))
            {
                isOk = true
                break
            }
        }

        if (!isOk)
        {
            Log.d(TAG, "执行结果：指令执行超时！地址：" +device!!.canAddr)
            isStop = true
            return ECommandResult.TimeOut
        }
        else
        {
            Log.d(TAG, "执行结果：指令执行成功！地址：" +device!!.canAddr)
        }
        return ECommandResult.OK
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getSendData(onecmd: String): ByteArray
    {
        var onecmd = onecmd
        onecmd = onecmd.trim { it <= ' ' }.toUpperCase()

        // 如果值中含有小数点，则数字单位为毫米，需要转换成步数
        if (onecmd.contains("."))
        {
            val axi = onecmd.substring(0, 1).toUpperCase()
            val actType = onecmd.substring(1, 2).toUpperCase()
            if (actType == "S" || actType == "A")
            {
                val value = onecmd.substring(2).toDouble()
                var step = 0.0
                when (axi)
                {
                    "X" -> step = value * paramScale.x
                    "Y" -> step = value * paramScale.y
                    "Z" -> step = value * paramScale.z
                    "M" -> step = value * paramScale.m
                    "W" -> step = value * paramScale.w
                    "P" -> step = value * paramScale.p
                }
                onecmd = "$axi$actType $step"
            }
        }

        // 取命令的设备名称
        var dname = ""
        if (!onecmd.contains("@"))
        {
            if (onecmd.substring(0, 1) == "F")
            {
                dname = onecmd.substring(1, 2)
                onecmd = "FX" + onecmd.substring(2)
            }
            else
            {
                dname = onecmd.substring(0, 1)
                onecmd = "X" + onecmd.substring(1)
            }
        }
        else
        {
            val tmp = onecmd.indexOf("@")
            dname = onecmd.substring(tmp + 1).trim { it <= ' ' }
            onecmd = onecmd.substring(0, tmp).trim { it <= ' ' }
        }

        var fcount = onecmd.length / 8
        if (onecmd.length != fcount * 8)
            fcount++

        val buff = ByteArray(13 * fcount)
        val byteP = onecmd.toByteArray(charset("UTF-8"))

        var cIndex = 0

        for (findex in 0 until fcount)
        {
            if (findex == fcount - 1)
            {
                if (onecmd.length == fcount * 8)
                    buff[findex * 13 + 0] = (0x80 + 8).toByte()
                else
                    buff[findex * 13 + 0] = (0x80 + onecmd.length % 8).toByte()
            }
            else
                buff[findex * 13 + 0] = (0x80 + 8).toByte()

            buff[findex * 13 + 1] = 0
            var device: Device? = null

            buff[findex * 13 + 2] = ((fcount shl 4) + (findex + 1)).toByte()
            buff[findex * 13 + 3] = 0
            try
            {
                buff[findex * 13 + 4] = getCanAddrByName(dname).toByte()

                val addr = getCanAddrByName(dname).toByte()
                buff[findex * 13 + 4] = addr
                for (deviceSub in deviceList)
                {
                    if (deviceSub.canAddr == addr.toInt())
                    {
                        device = deviceSub
                        break
                    }
                }
            }
            catch (e: Exception)
            {
                Log.d(TAG, "Can地址不正确，请检查是否为空（黄色文本框）！")
                e.printStackTrace()
            }

            for (i in 5..12)
            {
                buff[findex * 13 + i] = if (cIndex < onecmd.length) byteP[cIndex] else 0.toByte()
                cIndex++
            }

            if (findex == 0)
                System.arraycopy(buff, 0, device!!.LastSendData, 0, 13) //每次发送记录第一个Can帧
        }

        return buff
    }

    private fun getCanAddrByName(name: String): Int
    {
        try
        {
            for (device in deviceList)
            {
                if (device.name == name)
                    return device.canAddr
            }
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }

        return 0
    }

    /**
     * 接收线程
     */
    private fun thSocketReceive()
    {
        var device: Device? = null

        try
        {
            if (null != tcpSocket && mIsTcp)
            {
//                tcpSocket.soTimeout = 1000
            }
            else if (null != udpSocket)
            {
                udpSocket.soTimeout = 1000
            }
        }
        catch (ex: SocketException)
        {
            println("SocketException ex:$ex")
        }

        var bytesReadThisTime = 0

        while (isConnectSuccess)
        {
            try
            {
                if (mIsTcp)
                {
                    if (!tcpSocket.isClosed) bytesReadThisTime = inputStream.read(socketRecBuffer)
                }
                else
                {
                    val packet = DatagramPacket(socketRecBuffer, socketRecBuffer.size)
                    bytesReadThisTime = packet.length
                    udpSocket?.receive(packet)
                }

                totalReciveBytes += bytesReadThisTime
                for (findex in 0 until bytesReadThisTime / 13)
                {
                    try
                    {
                        val addr = socketRecBuffer[findex * 13 + 3]
                        for (subDevice in deviceList)
                        {
                            if (subDevice.canAddr == addr.toInt())
                            {
                                device = subDevice
                                break
                            }
                        }

                        device!!.lastReceiveTime = System.currentTimeMillis()

                        if (socketRecBuffer[findex * 13 + 5].toInt() == 0)
                            System.arraycopy(socketRecBuffer, findex * 13, device.LastReceiveData1, 0, 13)
                        else
                        {
                            System.arraycopy(socketRecBuffer, findex * 13, device.LastReceiveData2, 0, 13)
                            // 试管摆放检测
                            if (device.canAddr == 50 && device.LastReceiveData2[6].toChar() == 'D')
                            {
                                isTubePutOk = device.LastReceiveData2[7].toChar() == '1'
                            }
//                            // 底板温度
//                            else if (device.canAddr == 99 && device.LastReceiveData2[6].toChar() == 'T')
//                            {
//                                tempBase = (device.LastReceiveData2[7] - '0'.toByte()) * 100 + (device.LastReceiveData2[8] - '0'.toByte()) * 10 + (device.LastReceiveData2[9] - '0'.toByte())
//                            }
//                            // 蒸汽温度
//                            else if (device.canAddr == 99 && device.LastReceiveData2[6].toChar() == 'U')
//                            {
//                                tempSteam = (device.LastReceiveData2[7] - '0'.toByte()) * 100 + (device.LastReceiveData2[8] - '0'.toByte()) * 10 + (device.LastReceiveData2[9] - '0'.toByte())
//                            }
                            // 环境温度
                            else if (device.canAddr == 99 && device.LastReceiveData2[6].toChar() == 'V')
                            {
                                tempEnvironment = (device.LastReceiveData2[7] - '0'.toByte()) * 100 + (device.LastReceiveData2[8] - '0'.toByte()) * 10 + (device.LastReceiveData2[9] - '0'.toByte())
                            }
                            // 相对湿度
                            else if (device.canAddr == 99 && device.LastReceiveData2[6].toChar() == 'W')
                            {
                                humRelative = (device.LastReceiveData2[7] - '0'.toByte()) * 10 + (device.LastReceiveData2[8] - '0'.toByte())
                            }
                            // 枪头是否存在
                            else if (device.LastCmdText == "AA2" && device.LastReceiveData2[5].toInt().toChar() == 'A' && device.LastReceiveData2[6].toInt().toChar() == 'A')
                            {
                                Log.i(TAG, "枪头状态返回：${device.LastReceiveData2.joinToString("，") { it.toInt().toChar().toString()}}")

                                tipExist = device.LastReceiveData2[7].toInt().toChar() == '1'

                                Log.i(TAG, "枪头状态---是否存在${device.LastCmdText}：${tipExist}")
                            }
                            // 喷雾加液完成
                            else if ((device.LastReceiveData2[5].toChar() == '-'  && device.LastReceiveData2[6].toChar() == '1') || device.LastReceiveData2[6].toChar() == 'S')
                            {
                                isSprayWaterAddDone = true
                                Log.i(TAG, "喷雾加液完成---${isSprayWaterAddDone}")
                            }
                        }
                    }
                    catch (err: Exception)
                    {
                    }

                }

            }
            catch (ex: Exception)
            {
                //  buffPos = 0;
            }

        }
    }

    private fun isDeviceAnswer(device: Device): Boolean
    {
        return device.lastReceiveTime > device.lastSendTime && device.LastReceiveData1[0].toInt() != 0 && device.LastReceiveData1[5].toInt() == 0
    }

    private fun isAllMotionCmdFinished(deviceL: List<Device>): Boolean
    {
//        for (device in deviceL)
//        {
//            if (isActionCmd(device.LastCmdText))
//            {
//                if (!isDeviceFinishAnswer(device))
//                    return false
//            }
//        }

        // edited by noddy 2018-07-25
        val copyDeviceL = deviceL.toMutableList()
        for (device in copyDeviceL)
        {
            if (device != null && isActionCmd(device.LastCmdText))
            {
                if (!isDeviceFinishAnswer(device))
                    return false
            }
        }
        return true
    }

    private fun isDeviceFinishAnswer(device: Device): Boolean
    {
        return device.lastReceiveTime > device.lastSendTime && device.LastReceiveData2[0].toInt() != 0 && device.LastReceiveData2[6] == device.LastSendData[6]
    }

    /**
     * 是否动作类型指令，执行这类指令要等待返回完成通讯包
     *
     * @param oneCmd
     * @return
     */
    private fun isActionCmd(oneCmd: String): Boolean
    {
        if (oneCmd.trim { it <= ' ' }.substring(0, 1).toUpperCase() == "F")
            return false

        val tmp = oneCmd.trim { it <= ' ' }.substring(1, 2).toUpperCase()
        return tmp == "I" || tmp == "S" || tmp == "A" || tmp == "O" || tmp == "V" || oneCmd.toLowerCase().startsWith("a") || oneCmd.toLowerCase().startsWith("pp") || oneCmd.toLowerCase().startsWith("po")
//        return tmp == "I" || tmp == "S" || tmp == "A" || tmp == "O" || tmp == "V" || oneCmd.toLowerCase().startsWith("pp") || oneCmd.toLowerCase().startsWith("po")
    }

}
