package com.superh2.library.myEntityJson

/**
 *@Description 二维码位置
 *@Author Noddy
 *@Time 2018/5/17 16:34
 */
class PosBarcode
{
    // 64个玻片x、y位置
    var slides = ArrayList<Position>(64)

    // 玻片X步长
    var stepLengthX: Double = 28.5

    // 玻片Y步长
    var stepLengthY: Double = 80.0
}