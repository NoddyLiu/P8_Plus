package com.superh2.library.utils

/**
 *@Description 数据工具转换类
 *@Author Noddy
 *@Time 2018/12/11 11:39
 */
object DataHelper
{
    /**
     * 字节数组转16进制字符串（包含空格）
     * @param data
     * @return
     */
    fun byte2HexWithSpace(data: ByteArray): String
    {
        return data.joinToString(" ") { String.format("%02x", it) }
    }

    /**
     * 字节数组转16进制字符串（不含空格）
     * @param data
     * @return
     */
    fun byte2HexNoSpace(data: ByteArray): String
    {
        return data.joinToString("") { String.format("%02x", it) }
    }

    /**
     * 16进制字符串转字节数组
     * @param hexStr
     * @return
     */
    fun hex2Byte(hexStr: String): ByteArray
    {
        var temp = hexStr.split(' ')
        val tempLength = temp.size
        var buffer = ByteArray(tempLength)
        for (i in 0 until tempLength)
        {
            buffer[i] = temp[i].toByte(16)
        }
        return buffer
    }


    /**
     *  整数转为16进制字符
     *  @param data 整数
     *  @return
     */
    fun int2Hex(data: Int): String
    {
        var strValidate = ""
        when
        {
            data <= 15 -> strValidate = "000" + data.toString(16).toUpperCase()
            data <= 255 -> strValidate = "00" + data.toString(16).toUpperCase()
            data <= 4095 -> strValidate =
                "0" + data.toString(16).toUpperCase().substring(0, 1) + "" + data.toString(16).toUpperCase().substring(1)
            data <= 65535 -> strValidate =
                data.toString(16).toUpperCase().substring(0, 2) + "" + data.toString(16).toUpperCase().substring(2)
        }
        return strValidate
    }

    /**
     *  两个字节转为整形
     *  @param b1 高位
     *  @param b2 低位
     *  @return
     */
    fun twoByte2Int(b1: Int, b2: Int): Int
    {
        return (b1 shl 8) or b2
    }

    /**
     *  多个字节转为整形
     *  @param bytes
     *  @return
     */
    fun bytes2Int(bytes: ByteArray): Int
    {
        var result = 0
        for (i in bytes.indices)
        {
            result = result or (bytes[i].toInt() shl 8 * i)
        }
        return result
    }

    /**
     *  整形转为两个字节
     *  @param data
     *  @return 0:高位；1:低位
     */
    fun int2TwoByte(data: Int): ByteArray
    {
        var result = ByteArray(2)
        result[0] = (data shr 8).toByte()
        result[1] = (data and 0xff).toByte()
        return result
    }

    /**
     *  整形转为4个字节
     *  @param data
     */
    fun int2FourByte(data: Int): ByteArray
    {
        var result = ByteArray(4)
        result[0] = ((data shr 24) and 0xff).toByte()
        result[1] = ((data shr 16) and 0xff).toByte()
        result[2] = ((data shr 8) and 0xff).toByte()
        result[3] = (data and 0xffff).toByte()
        return result
    }

    /**
     * 字节某位是否为1
     * @param data 数据
     * @param bitPos 数据位
     * @return true:1;false:0
     */
    fun isChecked(data: Int, bitPos: Int): Boolean
    {
        return (data and (1 shl bitPos)) == (1 shl bitPos)
    }

    /**
     * Float转为指定小数位
     * @param value 数值
     * @param digit 多少位
     */
    fun float2Decimal(value:Float ,digit:Int):Float
    {
        return  String.format("%." + digit + "f", value).toFloat()
    }

    /**
     * byte数组转为字符串
     * @param byteArray byte数组
     * @return 字符串
     */
    fun byteArray2String(byteArray: ByteArray): String
    {
        return String(byteArray, Charsets.UTF_8)
    }
}