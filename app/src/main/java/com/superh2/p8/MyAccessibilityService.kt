package com.superh2.p8

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 *@Description
 *@Author Noddy
 *@Time 2018/7/26 10:41
 */
class MyAccessibilityService: AccessibilityService()
{
    override fun onInterrupt()
    {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?)
    {
    }
}