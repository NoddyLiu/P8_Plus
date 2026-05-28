package com.superh2.library.comm

import java.util.Arrays

/**
 * Created by Noddy on 2018/4/23.
 */
class Device
{
    var name: String? = null

    var canAddr: Int = 0

    var pos: Int = 0

    var lastReceiveTime: Long = 0

    var lastSendTime: Long = 0

    var LastSendData = ByteArray(13)
    var LastReceiveData1 = ByteArray(13)  // 设备收到指令后返回的应答
    var LastReceiveData2 = ByteArray(13)  // 设备报告执行状态
    private val packageIndex: Int = 0
    var LastCmdText = ""

    fun CleanReceiveData()
    {
        Arrays.fill(LastReceiveData1, java.lang.Byte.parseByte("0"))
        Arrays.fill(LastReceiveData2, java.lang.Byte.parseByte("0"))
    }
}
