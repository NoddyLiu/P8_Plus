package com.superh2.p8.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Description: 运行状态实体类
 * Author: liu_ja
 * Created On: 2025/8/13 14:06
 */
@Entity(tableName = "run_states")
data class RunStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),

    // 是否为扫码模式
    val isBarcodeMode: Boolean,

    // 是否被中断，默认false（新状态默认isInterrupted为false，等程序完成所有扫码动作才置true，因为默认扫码动作不在异常中断恢复范围内）
    var isInterrupted: Boolean = false,

    /**
     * 两种模式共用
     */
    // 试管信息JSON
    val scannedTubesJson: String? = null,
    // 运行到第几个玻片（当前玻片索引）
    val slideIndex: Int = 0,

    /**
     * 扫码模式
     */
    // 玻片信息JSON
    val scannedSlidesJson: String? = null,

    /**
     * 非扫码模式
     */
//    val slideTotalIndex: Int = 0,   // 总玻片索引
    // 运行到第几个试管（当前试管索引）
    val tubeIndex: Int = 0)     // 当前试管索引