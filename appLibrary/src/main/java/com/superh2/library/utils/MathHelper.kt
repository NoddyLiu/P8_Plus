package com.superh2.library.utils

import com.superh2.library.myEnum.EAngle
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.tan

/**
 *@Description
 *@Author Noddy
 *@Time 2018/10/16 9:40
 */
object MathHelper
{
    /**
     * 通过tan计算直角三角形垂直边高度
     * @param angle 直角边对角角度
     * @param yDistance 底角边长度
     */
    fun calcHeightFromRightTriangle(angle: EAngle, yDistance: Double): Double
    {
        return when (angle)
        {
            EAngle.Five -> yDistance * tan(Math.toRadians(5.0))
            EAngle.Ten -> yDistance * tan(Math.toRadians(10.0))
            else -> 0.0
        }
    }

    /**
     * 通过tan计算直角三角形垂直边差
     * @param angle1 角度1
     * @param angle1 角度2
     * @param yDistance 底角边长度
     */
    fun calcHeightOffsetCompareTwoAngle(angle1: EAngle, angle2: EAngle, yDistance: Double): Double
    {
        return abs(calcHeightFromRightTriangle(angle1, yDistance) - calcHeightFromRightTriangle(angle2, yDistance))
    }
}