package com.superh2.library.myEntityJson

/**
 *@Description 枪头位置
 *@Author Noddy
 *@Time 2018/5/16 14:34
 */
class PosTips
{
    // 96个枪头x、y位置
    var tips = ArrayList<Position>(96)

    // 预取枪头高度
    var preTakeTipHeight: Double = 0.0

    // 取枪头高度
    var takeTipHeight: Double = 0.0

    // 枪头盒间步长
    var stepLength: Double = 9.0

    // 退枪头x、y
    var releaseTipPos = Position()

    // 退枪头高度
    var releaseTipHeight: Double = 0.0

    // 退枪头提拉偏移
    var releaseTipOffset: Double = 0.0

    // 二维码通道1y
    var releaseTipChannel1Y: Double = 0.0

    // Tip盒点1
    var releaseTipBoxPoint1X: Double = 0.0
    var releaseTipBoxPoint1Y: Double = 0.0
    // Tip盒点2
    var releaseTipBoxPoint2X: Double = 0.0
    var releaseTipBoxPoint2Y: Double = 0.0

    // Tip头隔离空气高度
    var additionalAirHeight: Double = 0.0

    // 防漏挡板伸出位置
    var plateExtendedPos: Double = 0.0
    // 防漏挡板缩入位置
    var plateRetractedPos: Double = 0.0

    // 枪头检测位置
    var tipCheckPos = Position()
    var tipCheckHeight: Double = 0.0
}