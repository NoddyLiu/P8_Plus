package com.superh2.p8

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootBroadcastReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
//        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true))
//        {

        Log.i("BootBroadcastReceiver", "接收到启动receiver")

        // 开机启动的Activity  
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.addCategory(Intent.CATEGORY_HOME)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        activityIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        // 启动Activity  
        context.startActivity(activityIntent)
//        }
    }
}
