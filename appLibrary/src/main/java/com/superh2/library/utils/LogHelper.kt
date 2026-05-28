package com.superh2.library.utils

import android.content.Context
import android.util.Log
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.util.*


/**
 * Description: 日志帮助类
 * Author: liu_ja
 * Created On: 2023/3/22 11:11
 */
object LogHelper
{
    lateinit var mContext: Context
    lateinit var log: Logger

    /**
     * 初始化日志参数
     */
    fun configLog()
    {
        val logConfig = LogConfig()
        /** 设置Log等级，生产环境下调用setLogToProduce()，发布后调用setLogToRelease()  */
        logConfig.setLogToProduce(logConfig)
        logConfig.fileName = LogConfig.LOG_FILE_PATH
        logConfig.setLevel("org.apache", Level.INFO)
        logConfig.maxFileSize = 1024 * 50.toLong()
        logConfig.isImmediateFlush = true
        logConfig.configure()
    }


    /**
     * 初始化日志帮助类
     * @param context 上下文
     */
    fun init(context: Context)
    {
        mContext = context
        log = Logger.getLogger(LogConfig.LOG_FILE_PATH)
    }

    /**
     * 写入普通日志文件的数据
     * @param str 需要写入的数据
     */
    fun writeLog(str: Any)
    {
        if (str.toString().toUpperCase(Locale.ROOT).startsWith("AT")) return
        try
        {
            log.info(str)
        }
        catch (e: Exception)
        {
            Log.e("error", "Write failure !!! $e")
        }
    }

    /**
     * 信息日志title
     * @param info
     */
    fun infoTitle(info: String)
    {
        writeLog("---------------------------------------------------------------------------------------------------")
        writeLog("---------------------------------------------------------------------------------------------------")
        writeLog("---------------------------------------------------------------------------------------------------")
        writeLog("--------------------------------------$info--------------------------------------")
    }

    /**
     * 信息日志tail
     * @param info
     */
    fun infoTail(info: String)
    {
        writeLog("--------------------------------------$info--------------------------------------")
        writeLog("---------------------------------------------------------------------------------------------------")
        writeLog("---------------------------------------------------------------------------------------------------")
        writeLog("---------------------------------------------------------------------------------------------------")
        writeLog("--------------------------------------$info--------------------------------------")
    }

    /**
     * 信息日志
     * @param info
     */
    fun info(info: String)
    {
        writeLog(info)
    }

    /**
     * 错误日志
     * @param error
     */
    fun error(error:String)
    {
        writeLog("--------------------------------------错误信息：--------------------------------------")
        writeLog("错误    ：$error")
    }
}