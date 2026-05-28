package com.superh2.library.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Description: 时间工具类
 * Author: liu_ja
 * Created On: 2019/10/14 15:11
 */
object DateHelper
{
    /**
     * 获取当前日期时间
     */
    fun getNow(): String
    {
        if (android.os.Build.VERSION.SDK_INT >= 24)
        {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
        }
        else
        {
            var tms = Calendar.getInstance()
            return tms.get(Calendar.YEAR).toString() + "-" + (tms.get(Calendar.MONTH ) + 1).toString() + "-" + tms.get(Calendar.DAY_OF_MONTH).toString() + " " + tms.get(Calendar.HOUR_OF_DAY).toString() + ":" + tms.get(Calendar.MINUTE).toString() + ":" + tms.get(Calendar.SECOND).toString() + "." + tms.get(Calendar.MILLISECOND).toString()
        }
    }
}