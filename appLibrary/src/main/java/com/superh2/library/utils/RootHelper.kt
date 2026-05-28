package com.superh2.library.utils

import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Description: Root工具类
 * Author: liu_ja
 * Created On: 2021/08/02 15:11
 */
object RootHelper
{
    /**
     * 执行root指令
     */
    fun execRootCmd(cmd: String): String?
    {
        var result: String? = ""
        var dos: DataOutputStream? = null
        var dis: DataInputStream? = null
        try
        {
            val p = Runtime.getRuntime()
                    .exec("su") // Root processed android system has su command
            dos = DataOutputStream(p.outputStream)
            dis = DataInputStream(p.inputStream)
            dos.writeBytes("$cmd \n")
            dos.flush()
            dos.writeBytes("exit\n")
            dos.flush()
            var line: String? = null
            while (dis.readLine()
                        .also { line = it } != null
            )
            {
                Log.d("result", line)
                result += line
            }
            p.waitFor()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            if (dos != null)
            {
                try
                {
                    dos.close()
                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                }
            }
            if (dis != null)
            {
                try
                {
                    dis.close()
                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /**
     * 获取usb连接权限
     */
    fun getUsbPermission()
    {
        val commend = "chmod 777 /dev/bus/usb/ -R"
        execRootCmd(commend)
    }
}