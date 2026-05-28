package com.superh2.library.myEntityJson

/**
 *@Description 固定液位置
 *@Author Noddy
 *@Time 2018/5/22 14:34
 */
class PosFixative
{
    // 64个玻片x、y（第一滴y位置，默认有3滴）位置
    var slides = ArrayList<Position>(64)

    // 两点之间y轴间距
    var yDistance: Double = 0.0

    // 固定液量程
    var heightRange: Double = 0.0

    // 固定液高度
    var fixativeHeight:Int =0

    // 玻片X步长
    var stepLengthX: Double = 28.5

    // 玻片Y步长
    var stepLengthY: Double = 80.0

    // 测试点位置
    var testPos = Position()
}