package com.superh2.library.myEntityJson

import com.superh2.library.myEnum.EAngle
import com.superh2.library.myEnum.EOnOff
import com.superh2.library.myEnum.ESpreadingMode

/**
 *@Description 方法参数设置
 *@Author Noddy
 *@Time 2018/5/21 11:34
 */
class MethodParams
{
    // 组名
    var groupName = "Group 1"

    // 滴片模式
    var spreadingMode = ESpreadingMode.Dry

    // 是否扫二维码
    var barcode = EOnOff.On

    // 玻片数量（扫码时使用）
    var slideQuantity = 64
    // 玻片是否自动摆放（自动：吸头吸取玻片摆放到盘位）（扫码时使用）
    var slideAutomatic = EOnOff.Off

    // 试管数量（不扫码时使用）
    var tubeQuantity:Int = 1
    // 滴片数量（不扫码时使用）
    var slidePerTube:Int = 1

    // 滴片点数
    var spotPerSlide:Int = 1

    // 玻片角度
    var angleOfSlide = EAngle.Five

    // 滴片法具体设置
    var paramsSlideMode:MethodParamsSlideMode = MethodParamsSlideMode()
}