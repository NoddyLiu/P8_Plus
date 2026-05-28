package com.superh2.library.myEntityJson

import com.superh2.library.myEnum.EOnOff

/**
 *@Description 工程师模式参数设置
 *@Author Noddy
 *@Time 2018/5/21 13:34
 */
class GeneralParams
{
    // Tip头预吸空气（吸液前）
    var additionalAirInTipBeforeAbsorb:Int = 0

    // Tip头隔离空气（吸液后）
    var additionalAirInTip:Int = 0

    // Tip头隔离空气（喷液后）
    var additionalAirInTipAfterDispense:Int = 0

    // 样品吹打次数
    var numberOfSampleStir:Int = 1

    // 样品吹打体积
    var volumnOfSampleStir:Int = 10

    // 样品吹打速度
    var speedOfSampleStir:Int = 1

    // 试管底座手动检测
    var tubeBaseManualCheck= EOnOff.On

    // 预喷雾时间
    var preSprayTime = 5
    // 预喷雾加液步数
    var preSprayLiquidAddition = 2000
    // 预喷雾加液反抽步数（新逻辑不再使用）
    var preSprayLiquidAdditionReverse = 1000
    // 喷雾加液速度
    var sprayLiquidAdditionSpeedInit = 100
    var sprayLiquidAdditionSpeedTarget = 500
    var sprayLiquidAdditionSpeedAccelerated = 2

    // 扫码完毕后是否直接运行
    var runImmAfterScanned = EOnOff.Off

    // 喷液最大速度
    var dispenseSpeedMaxInit = 100
    var dispenseSpeedMaxTarget = 2500
    var dispenseSpeedMaxAccelerated = 20

    // 喷雾时Y轴最大速度
    var ySpeedMaxWhenSprayingInit = 10
    var ySpeedMaxWhenSprayingTarget = 100
    var ySpeedMaxWhenSprayingAccelerated = 1

    // 二维码识别长度
    var qrCodeScanLength = 20

    // 分散指数（18组温湿度）
    var DispersancyIndexEnable = false
    var DispersancyIndexIndex = 0
    var DispersancyIndexList = ArrayList<Humiture>(18)

    // 开门报警（运行中弹出框提示）
    var alarmDoor = EOnOff.Off

    // 枪头检测报警（运行中弹出框提示，取枪头后和退退枪头后检测枪头状态，如果异常，则报警）
    var alarmTipCheck = EOnOff.On

    // 温湿度稳定差值（用于温湿度稳定n分钟后，程序自启动）
    var humitureStabilityThreshold = 0.5f
}