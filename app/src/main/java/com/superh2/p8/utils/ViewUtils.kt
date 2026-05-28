package com.superh2.p8.utils

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.View
import com.superh2.p8.MainActivity
import com.superh2.p8.MainActivity.Companion.isManualPause
import com.superh2.p8.MainActivity.Companion.isManualStop
import com.superh2.p8.MainActivity.Companion.mHandlerMain
import com.superh2.p8.MainActivity.Companion.waitDialog
import android.text.TextUtils
import android.util.Log
import com.superh2.p8.MyAccessibilityService


/**
 *@Description view工具类
 *@Author Noddy
 *@Time 2018/6/29 10:26
 */
object ViewUtils
{
    private const val ACCESSIBILITY_ENABLED = 1

    /** 自启动是否已经打开
     * @param context
     */
    fun isAutoRunOn(context: Context): Boolean
    {
        var accessibilityEnabled = 0
        val service = context.packageName + "/" + MyAccessibilityService::class.java.canonicalName
        try
        {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.applicationContext.contentResolver,
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED)
        }
        catch (e: Settings.SettingNotFoundException)
        {
            Log.e("AU", "Error finding setting, default accessibility to not found: " + e.message)
        }

        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

        if (accessibilityEnabled == ACCESSIBILITY_ENABLED)
        {
            val settingValue = Settings.Secure.getString(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue != null)
            {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext())
                {
                    val accessibilityService = mStringColonSplitter.next()

                    if (accessibilityService.equals(service, ignoreCase = true))
                    {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * 运行中自动弹出警告提示并暂停
     * @param content
     * @param isContinueBtnVisible 继续按钮是否可见
     */
    fun showWarningDialogAutoPause(content: String, isContinueBtnVisible: Boolean)
    {
        // 分开两个Runnable，先模拟点击"暂停"，再更改setMessage（必须设置delay时间），避免setMessage内容更改不了
        mHandlerMain.post {
            waitDialog?.getButton(DialogInterface.BUTTON_NEUTRAL)?.performClick() // 模拟点击“暂停”按钮
        }

        // 马上暂停，因为模拟点击“暂停”按钮会有时间延迟，并不是马上暂停
        isManualPause = true

        mHandlerMain.postDelayed({
            waitDialog?.setCancelable(true)
            waitDialog?.setMessage(content)
            if (!isContinueBtnVisible)
                waitDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.visibility = View.INVISIBLE
        }, 150)
    }

    /**
     * 暂停运行程序
     */
    fun pauseRunningDialog()
    {
        mHandlerMain.post {
            waitDialog?.getButton(DialogInterface.BUTTON_NEUTRAL)?.performClick() // 模拟点击“暂停”按钮
        }

        // 马上暂停，因为模拟点击“暂停”按钮会有时间延迟，并不是马上暂停
        isManualPause = true
    }

    /**
     * 继续运行程序
     */
    fun continueRunningDialog()
    {
        mHandlerMain.post {
            waitDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.performClick() // 模拟点击“继续”按钮
        }
    }

    /**
     * 停止运行程序
     */
    fun closeRunningDialog()
    {
        mHandlerMain.post {
            isManualPause = true
            mHandlerMain.postDelayed({
                isManualStop = true
                mHandlerMain.post(MainActivity.rnHideWaitDialog)
            }, 50)
        }
    }

    /**
     * 退出当前Launcher，但不会删除默认的Launcher
     * @param context
     */
    fun exitCurrentLauncher(context: Context)
    {
        val pm = context.packageManager
        val mockUpComponent = ComponentName(MainActivity::class.java!!.getPackage().name, MainActivity::class.java!!.name)
        pm.setComponentEnabledSetting(mockUpComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(startMain)

        pm.setComponentEnabledSetting(mockUpComponent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)
    }
}