package com.superh2.library.myEntityJson

/**
 *@Description 喷雾位置
 *@Author Noddy
 *@Time 2018/5/17 11:34
 */
class PosSpray
{
    // 64个玻片x、y位置
    var slides = ArrayList<Position>(64)

    // 两点之间y轴间距
    var yDistance: Double = 0.0

    // 喷雾量程
    var heightRange: Double = 0.0

    // 喷雾高度
    var sprayHeight: Int = 0

    // 玻片X步长
    var stepLengthX: Double = 28.5

    // 玻片Y步长
    var stepLengthY: Double = 80.0

    // 测试点位置
    var testPos = Position()
}