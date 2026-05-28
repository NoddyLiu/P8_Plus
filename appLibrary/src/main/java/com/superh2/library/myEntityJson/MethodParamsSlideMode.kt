package com.superh2.library.myEntityJson

/**
 *@Description 滴片法具体参数设置
 *@Author Noddy
 *@Time 2018/5/21 13:34
 */
class MethodParamsSlideMode
{
    // 雾化前固定
    var fixativeVolumnBeforeSpray:Int = 0

    // 雾化后等待
    var delayTimeAfterSpray:Float = 0.0f

    // 雾化后固定
    var fixativeVolumnAfterSpray:Int = 0

    // 固定液压力（1-12）
    var fixativeDispensePressure:Int = 1

    // 滴样体积
    var volumnPerDrop:Int = 10

    // 滴液高度
    var dispenseHeight:Int = 1

    // 滴液速度
    var dispenseSpeed:Int = 1

    // 滴样前等待时间
    var delayTimeBeforeSampleDropping:Float = 0.0f

    // 滴样后等待时间
    var delayTimeAfterSampleDropping:Float = 0.0f

    // 追加固定
    var fixativeVolumnPerDrop:Int = 0

    // 温度
    var temp:Float = 0.0f

    // 湿度
    var hum:Float = 0.0f

    // 喷雾时Y轴速度
    var ySpeedWhenSpray:Int = 1

    // 喷雾加液间隔（多少个玻片加液一次）
     var sprayLiquidAdditionInterval:Int = 32

    // 喷雾移动前固定时间（即在玻片开始位置喷雾n秒后再移动）
    var sprayLiquidTimeBeforeMove:Int = 0

//    // 吹风时间
//    var humidBlowTime:Int = 0
//
//    // 吹风风量
//    var humidBlowLevel:Int = 50
//
//    // 干燥机
//    var dryingMachine = EOnOff.On
}