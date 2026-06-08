package com.superh2.p8.serial

import android.util.Log
import com.example.x6.serial.SerialPort
import com.superh2.library.myInterface.IScannerResultCallback
import com.superh2.library.utils.CRC16Utils
import com.superh2.library.utils.DataHelper
import com.superh2.library.utils.LogHelper
import com.superh2.p8.MainActivity.Companion.mContext
import com.superh2.p8.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @Description 大肯ADP串口通信客户端
 * @Author liu_ja
 * @CreateDate 2026/05/28 14:02
 */
class SerialClientAdp
{
    private val TAG = "SerialClientAdp"

    private val SERIAL_PORT = "ttyS4"
    private var serialttyS: SerialPort? = null
    private var ttySInputStream: InputStream? = null
    private var ttySOutputStream: OutputStream? = null

    // 是否已经连接
    var isConnected = false

    // adp地址
    var address = 1

    // 互斥锁：确保同一时刻只有一条指令在总线上收发，杜绝多线程并发导致的报文污染
    private val mutex = Mutex()

    /**
     * 打开串口
     */
    fun openSerial()
    {
        try
        {
            serialttyS = SerialPort(File("/dev/${SERIAL_PORT}"), 115200, 0)
            ttySInputStream = serialttyS!!.inputStream
            ttySOutputStream = serialttyS!!.outputStream
        }
        catch (e: java.lang.Exception)
        {
            LogHelper.error("大肯ADP机器状态：" + e.message)
            isConnected = false
        }
        catch (e: SecurityException)
        {
            LogHelper.error("大肯ADP机器状态：" + e.message)
            isConnected = false
        }
        catch (e: Exception)
        {
            LogHelper.error("大肯ADP机器状态：" + e.message)
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
     * 核心收发挂起函数：执行指令，等待响应，处理超时及校验
     * @param cmd 指令字符
     * @param data 指令附带的 HEX 数据 (无数据则传空字符串)
     * @param timeoutMs 超时时间，默认 2000 毫秒
     * @return 剥离了帧头、地址、指令符、CRC 和尾部后的纯核心响应数据
     */
    suspend fun executeCommand(cmd: String, data: String = "", timeoutMs: Long = 2000L): String
    {
        return mutex.withLock {
            withTimeout(timeoutMs) {
                val out = ttySOutputStream ?: throw IOException("串口未打开")
                val input = ttySInputStream ?: throw IOException("串口未打开")

                // 1. 发送前，清空接收缓冲区中的残余脏数据，防止错位
                while (input.available() > 0)
                {
                    input.read()
                }

                // 2. 拼接发送帧 (不含CRC和尾符)
                val addrStr = address.toHex(2)
                val frameWithoutCrc = ">$addrStr$cmd$data"

                // 3. 计算 CRC 并转换为 4位 十六进制字符串 (低字节在前)
                val crc = calCrc(frameWithoutCrc.toByteArray(Charsets.US_ASCII))
                val crcStr = String.format("%02X%02X", crc and 0xFF, (crc ushr 8) and 0xFF)

                // 4. 加上 \r\n 并发送
                val fullFrame = "$frameWithoutCrc$crcStr\r\n"

                out.write(fullFrame.toByteArray(Charsets.US_ASCII))
                out.flush()
                Log.v(TAG, "发送报文: ${fullFrame.trim()}")

                // 5. 非阻塞式读取响应
                val buffer = ByteArrayOutputStream()
                var prevByte = -1

                // isActive 保证如果 withTimeout 超时，能安全跳出循环
                while (isActive)
                {
                    if (input.available() > 0)
                    {
                        val currentByte = input.read()
                        if (currentByte == -1) throw IOException("串口连接已断开")
                        buffer.write(currentByte)
                        // 侦测结束符 \r\n (0x0D, 0x0A)
                        if (prevByte == 0x0D && currentByte == 0x0A) break
                        prevByte = currentByte
                    }
                    else
                    {
                        // 缓冲区暂无数据，挂起协程 5 毫秒让出 CPU（这使得线程不会被阻塞卡死）
                        delay(5)
                    }
                }

                // 6. 校验响应帧
                val response = String(buffer.toByteArray(), Charsets.US_ASCII).trim()
                Log.v(TAG, "收到报文: $response")

                if (!response.startsWith(">")) throw IOException("非法帧头: $response")
                if (response.length < 7) throw IOException("接收帧太短: $response")

                // 提取并对比 CRC
                val resFrameWithoutCrc = response.substring(0, response.length - 4)
                val resCrcStr = response.substring(response.length - 4)
                val expectedCrc = calCrc(resFrameWithoutCrc.toByteArray(Charsets.US_ASCII))
                val expectedCrcStr = String.format("%02X%02X", expectedCrc and 0xFF, (expectedCrc ushr 8) and 0xFF)

                if (!resCrcStr.equals(expectedCrcStr, ignoreCase = true))
                {
                    throw IOException("CRC 校验失败 - 期望:$expectedCrcStr 实际:$resCrcStr")
                }

                // 对比地址与命令符
                val resAddr = response.substring(1, 3).toInt(16)
                if (resAddr != address) throw IOException("地址不匹配")

                val resCmd = response.substring(3, 3 + cmd.length)
                if (resCmd != cmd) throw IOException("命令不匹配 - 期望:$cmd 实际:$resCmd")

                // 成功，返回核心数据部分
                response.substring(3 + cmd.length, response.length - 4)
            }
        }
    }

    /** 标准 CRC16_MODBUS 算法 */
    private fun calCrc(bytes: ByteArray): Int
    {
        var crc = 0xFFFF
        for (b in bytes)
        {
            crc = crc xor (b.toInt() and 0xFF)
            for (i in 0 until 8)
            {
                crc = if ((crc and 1) != 0) (crc ushr 1) xor 0xA001 else crc ushr 1
            }
        }
        return crc
    }

    // ==========================================
    // 十六进制 编码与解码 扩展方法 (极其重要，安全处理负数)
    // ==========================================
    private fun Int.toHex(len: Int): String
    {
        val masked = when (len)
        {
            1 -> this and 0xF
            2 -> this and 0xFF
            4 -> this and 0xFFFF
            else -> this // len为8时直接格式化32位
        }
        return String.format("%0${len}X", masked)
    }

    private fun String.parseHexInt8(): Int = this.toInt(16)
    private fun String.parseHexInt16(): Int = this.toInt(16)
    private fun String.parseHexInt16Signed(): Int = this.toInt(16).toShort().toInt()
    private fun String.parseHexInt32(): Long = this.toLong(16)


    // ==========================================
    // 业务数据结构体定义
    // ==========================================
    data class VolumeInfo(val usedNl: Long, val unusedNl: Long)
    data class Thresholds(val suckBlock: Int, val spitBlock: Int, val detect: Int, val suckEmpty: Int)
    data class MonitorStatus(val suckSpit: Int, val suckEmpty: Int)
    data class InitSuckBackParams(val firstSuck: Int, val ejectTip: Int, val spitCut: Int)
    data class CalibrationSegment(val targetUl: Int, val compensationNl: Int)
    data class RecipeAndSeq(val recipe: Int, val seq: Int)


    // ==========================================
    // 完整 46 条功能指令实现 (根据手册及校验码严格对应)
    // ==========================================

    // 1. A 读取程序版本
    suspend fun readVersion(): String = executeCommand("A")

    // 2. B 设置吐液速度
    suspend fun setSpitSpeed(speed: Int)
    {
        executeCommand("B", speed.toHex(4))
    }

    // 3. b 读取吐液速度
    suspend fun readSpitSpeed(): Int = executeCommand("b").parseHexInt16()

    // 4. C 设置电容探测阈值
    suspend fun setCapacitanceThreshold(threshold: Int)
    {
        executeCommand("C", threshold.toHex(4))
    }

    // 5. c 读取电容探测阈值
    suspend fun readCapacitanceThreshold(): Int = executeCommand("c").parseHexInt16()

    // 6. d 查询运行状态 (00运行中, 01到达预定位置, 02异常撞击, 03检测到液位, 05超限, 06吸堵, 07吐堵, 09前段吸空, 0A后段吸空)
    suspend fun queryStatus(): Int = executeCommand("d").parseHexInt8()

    // 7. E 查询已使用和剩余容积
    suspend fun queryVolume(): VolumeInfo
    {
        val res = executeCommand("E")
        return VolumeInfo(res.substring(0, 8).parseHexInt32(), res.substring(8, 16).parseHexInt32())
    }

    // 8. F 吸吐混匀指令 (返回 01可执行 02不可执行)
    suspend fun mixLiquid(volumeUl: Int, count: Int): Int = executeCommand("F", "${volumeUl.toHex(4)}${count.toHex(4)}").parseHexInt8()

    // 9. f 查询吸吐混匀剩余次数
    suspend fun queryRemainingMixCount(): Int = executeCommand("f").parseHexInt16()

    // 10. G 空气泵初始化
    suspend fun initializePump()
    {
        executeCommand("G")
    }

    // 11. g 询问复位回原点状态 (00回零中 01成功 02失败 03未回零)
    suspend fun queryInitStatus(): Int = executeCommand("g").parseHexInt8()

    // 12. H 设置报警/探测阈值
    suspend fun setThresholds(recipe: Int, seq: Int, suckBlock: Int, spitBlock: Int, detect: Int, suckEmpty: Int)
    {
        val data = "${recipe.toHex(4)}${seq.toHex(1)}${suckBlock.toHex(4)}${spitBlock.toHex(4)}${detect.toHex(4)}${suckEmpty.toHex(4)}"
        executeCommand("H", data)
    }

    // 13. h 读取报警/探测阈值
    suspend fun readThresholds(recipe: Int, seq: Int): Thresholds
    {
        val res = executeCommand("h", "${recipe.toHex(4)}${seq.toHex(1)}")
        return Thresholds(res.substring(5, 9).parseHexInt16Signed(), res.substring(9, 13).parseHexInt16Signed(), res.substring(13, 17).parseHexInt16Signed(), res.substring(17, 21).parseHexInt16Signed())
    }

    // 14. I 气压监测开关
    suspend fun setPressureMonitor(suckSpitSwitch: Int, suckEmptySwitch: Int)
    {
        executeCommand("I", "${suckSpitSwitch.toHex(2)}${suckEmptySwitch.toHex(2)}")
    }

    // 15. i 读取气压监测开关
    suspend fun readPressureMonitor(): MonitorStatus
    {
        val res = executeCommand("i")
        return MonitorStatus(res.substring(0, 2).parseHexInt8(), res.substring(2, 4).parseHexInt8())
    }

    // 16. J 设置首次回吸等参数
    suspend fun setInitSuckBack(firstSuck: Int, nc1: Int, nc2: Int, ejectTip: Int, nc3: Int, spitCut: Int)
    {
        executeCommand("J", "${firstSuck.toHex(4)}${nc1.toHex(4)}${nc2.toHex(4)}${ejectTip.toHex(4)}${nc3.toHex(4)}${spitCut.toHex(4)}")
    }

    // 17. j 读取首次回吸等参数
    suspend fun readInitSuckBack(): InitSuckBackParams
    {
        val res = executeCommand("j")
        return InitSuckBackParams(res.substring(0, 4).parseHexInt16(), res.substring(12, 16).parseHexInt16(), res.substring(20, 24).parseHexInt16())
    }

    // 18. K 设置6段补偿值 (根据手册范例，参数实为32位即8字符的Hex)
    suspend fun setSixSegmentComp(recipe: Int, seq: Int, isSpit: Boolean, segments: List<CalibrationSegment>)
    {
        require(segments.size == 6) { "必须恰好提供 6 段校准数据" }
        val sb = StringBuilder()
        sb.append(recipe.toHex(4)).append(seq.toHex(1)).append(if (isSpit) "1" else "0")
        segments.forEach { sb.append(it.targetUl.toHex(8)).append(it.compensationNl.toHex(8)) }
        executeCommand("K", sb.toString())
    }

    // 19. k 读取6段补偿值
    suspend fun readSixSegmentComp(recipe: Int, seq: Int, isSpit: Boolean): List<CalibrationSegment>
    {
        val res = executeCommand("k", "${recipe.toHex(4)}${seq.toHex(1)}${if (isSpit) "1" else "0"}")
        val list = mutableListOf<CalibrationSegment>()
        var offset = 6
        for (i in 0 until 6)
        {
            list.add(CalibrationSegment(res.substring(offset, offset + 8).parseHexInt32().toInt(), res.substring(offset + 8, offset + 16).parseHexInt32().toInt()))
            offset += 16
        }
        return list
    }

    // 20. M 首次回吸空气柱动作
    suspend fun firstSuckBackAir(): Int = executeCommand("M").parseHexInt8()

    // 21. N 气压探测液位动作 (01气压探测 02电容探测 00关闭探测)
    suspend fun detectLevel(mode: Int): Int = executeCommand("N", mode.toHex(2)).parseHexInt8()

    // 22. n 吸液动作指令
    suspend fun suckLiquid(volumeUl: Int): Int = executeCommand("n", volumeUl.toHex(4)).parseHexInt8()

    // 23. O 设置配方类 (大写)
    suspend fun setRecipe(recipe: Int, seq: Int)
    {
        executeCommand("O", "${recipe.toHex(4)}${seq.toHex(1)}")
    }

    // 24. o 读取配方类 (小写)
    suspend fun readRecipe(): RecipeAndSeq
    {
        val res = executeCommand("o")
        return RecipeAndSeq(res.substring(0, 4).parseHexInt16(), res.substring(4, 5).parseHexInt8())
    }

    // 25. P 二次回吸空气柱动作
    suspend fun secondSuckBackAir(): Int = executeCommand("P").parseHexInt8()

    // 26. p 吐液动作
    suspend fun spitLiquid(volumeUl: Int): Int = executeCommand("p", volumeUl.toHex(4)).parseHexInt8()

    // 27. Q 退TIP动作
    suspend fun ejectTip()
    {
        executeCommand("Q")
    }

    // 28. q 查询TIP有无 (01有，02无)
    suspend fun checkTipStatus(): Int = executeCommand("q").parseHexInt8()

    // 29. R 设置回程差补偿值
    suspend fun setReturnDiffComp(comp: Int)
    {
        executeCommand("R", comp.toHex(4))
    }

    // 30. r 读取回程差补偿值
    suspend fun readReturnDiffComp(): Int = executeCommand("r").parseHexInt16()

    // 31. T 修改设备ID
    suspend fun changeAddress(newAddress: Int)
    {
        executeCommand("T", newAddress.toHex(2))
    }

    // 32. U 保存当前配置的所有参数
    suspend fun saveAllParams()
    {
        executeCommand("U")
    }

    // 33. V 设置复位速度
    suspend fun setResetSpeed(speed: Int)
    {
        executeCommand("V", speed.toHex(4))
    }

    // 34. v 读取电机复位速度
    suspend fun readResetSpeed(): Int = executeCommand("v").parseHexInt16()

    // 35. W 设置电流参数
    suspend fun setCurrentParam(currentMa: Int)
    {
        executeCommand("W", currentMa.toHex(4))
    }

    // 36. w 读取电流参数
    suspend fun readCurrentParam(): Int = executeCommand("w").parseHexInt16()

    // 37. = 设备重启 (说明书打印缺失，由CRC反推得知)
    suspend fun rebootDevice()
    {
        executeCommand("=")
    }

    // 38. 2 设置切断速度
    suspend fun setCutSpeed(speed: Int)
    {
        executeCommand("2", speed.toHex(4))
    }

    // 39. 3 读取切断速度
    suspend fun readCutSpeed(): Int = executeCommand("3").parseHexInt16()

    // 40. 4 设置吸液速度
    suspend fun setSuckSpeed(speed: Int)
    {
        executeCommand("4", speed.toHex(4))
    }

    // 41. 5 读取吸液速度
    suspend fun readSuckSpeed(): Int = executeCommand("5").parseHexInt16()

    // 42. 6 读取当前气压值 (有符号，范围 -8192~8192)
    suspend fun readCurrentPressure(): Int = executeCommand("6").parseHexInt16Signed()

    // 43. + 设置吸吐液比例
    suspend fun setSuckSpitRatio(ratio: Int)
    {
        executeCommand("+", ratio.toHex(2))
    }

    // 44. - 读取吸吐液比例 (说明书打印缺失，由发送实例 CRC 反推得知)
    suspend fun readSuckSpitRatio(): Int = executeCommand("-").parseHexInt8()

    // 45. S 设置电容适应阈值
    suspend fun setCapacitanceAdaptThreshold(threshold: Int)
    {
        executeCommand("S", threshold.toHex(4))
    }

    // 46. s 读取电容适应阈值
    suspend fun readCapacitanceAdaptThreshold(): Int = executeCommand("s").parseHexInt16()
}