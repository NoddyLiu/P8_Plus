package com.superh2.library.myEntityJson

/**
 *@Description 滴液位置
 *@Author Noddy
 *@Time 2018/5/17 14:34
 */
class PosDispense
{
    // 64个玻片x、y（第一滴y位置，默认有3滴）位置
    var slides = ArrayList<Position>(64)

    // 两点之间y轴间距
    var yDistance: Double = 0.0

    // 滴液量程
    var heightRange: Double = 0.0

    // 滴液高度
    // 该参数在方法参数里面设置

    // 玻片X步长
    var stepLengthX: Double = 28.5

    // 玻片Y步长
    var stepLengthY: Double = 80.0
}