package com.superh2.library.myException

import android.util.Log
import com.superh2.library.utils.ConstantsUtils
import com.superh2.library.utils.DateHelper
import com.superh2.library.utils.FileUtils
import com.superh2.library.utils.LogHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Description:
 * Author: liu_ja
 * Created On: 2019/7/17 15:17
 */
class MyCrashHandler : Thread.UncaughtExceptionHandler
{
    override fun uncaughtException(t: Thread, e: Throwable)
    {
        // 日志
        FileUtils.saveJsonToSD(DateHelper.getNow() + "\n" + e.message.toString() + "\n" + Log.getStackTraceString(e), ConstantsUtils.FOLDER_ERROR, SimpleDateFormat("yyyyMMdd").format(Date()), true)
        LogHelper.error(DateHelper.getNow() + "\n" + e.message.toString() + "\n" + Log.getStackTraceString(e))
    }
}