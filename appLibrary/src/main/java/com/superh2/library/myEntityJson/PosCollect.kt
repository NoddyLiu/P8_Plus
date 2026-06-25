package com.superh2.library.myEntityJson

/**
 * Description: 玻片收集架位置(已滴片)
 * Author: liu_ja
 * Created On: 2026/6/24 15:12
 */
class PosCollect
{
    // 是否开启自动收集
    var isAutoCollect: Boolean = false

    // 收集架1 (第一片左下角)
    var rack1StartX: Double = 0.0
    var rack1StartY: Double = 0.0
    var rack1StartZ: Double = 0.0

    // 收集架2 (第一片左下角)
    var rack2StartX: Double = 0.0
    var rack2StartY: Double = 0.0
    var rack2StartZ: Double = 0.0

    // 通用参数
    var insertYDist: Double = 0.0  // 伸入Y距离（相对位置）
    var placeZDist: Double = 0.0   // 放置Z距离（相对位置）
    var stepX: Double = 0.0        // 每格X步长
    var stepZ: Double = 0.0        // 每格Z步长
}