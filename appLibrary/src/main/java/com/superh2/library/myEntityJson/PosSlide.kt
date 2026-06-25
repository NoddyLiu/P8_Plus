package com.superh2.library.myEntityJson

/**
 *@Description 吸玻片位置(未滴片)
 *@Author liu_ja
 *@Time 2021/08/06 14:34
 */
class PosSlide
{
    /**
     * 吸玻片
     */
    // 吸玻片位置
    var suckPos = Position()

    // 吸玻片高度（玻片架只有1个玻片的吸片高度）
    var suckHeight: Double = 0.0

    // 玻片厚度
    var thickness: Double = 1.2

    // 行走高度
    var walkingHeight: Double = 0.0

    /**
     * 放玻片
     */
    // 64个玻片x、y位置
    var slides = ArrayList<Position>(64)

    // 放玻片高度
    var releaseHeight:Double =0.0

    // 玻片X步长
    var stepLengthX: Double = 28.5

    // 玻片Y步长
    var stepLengthY: Double = 80.0
}