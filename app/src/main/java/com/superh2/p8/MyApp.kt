package com.superh2.p8

import android.app.Application
import com.superh2.library.myException.MyCrashHandler

/**
 * Description:
 * Author: liu_ja
 * Created On: 2019/7/17 15:14
 */
class MyApp: Application()
{
    override fun onCreate()
    {
        super.onCreate()
        // 处理非捕获性异常
        var handler = MyCrashHandler()
        Thread.setDefaultUncaughtExceptionHandler(handler)
    }
}