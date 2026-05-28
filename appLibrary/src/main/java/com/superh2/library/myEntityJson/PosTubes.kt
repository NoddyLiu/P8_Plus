package com.superh2.library.myEntityJson

/**
 *@Description 试管位置
 *@Author Noddy
 *@Time 2018/5/15 17:34
 */
class PosTubes
{
    // 64个试管x、y位置
    var tubes = ArrayList<Position>(64)

    // 吸液高度
    var suckHeight: Double = 0.0

    // 试管间步长
    var stepLength: Double = 22.0
}