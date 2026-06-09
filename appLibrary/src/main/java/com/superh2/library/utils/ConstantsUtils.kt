package com.superh2.library.utils

import com.superh2.library.myEnum.EDirection

/**
 * Created by Liuja on 2018/4/23.
 */

object ConstantsUtils
{
    /**
     * 程序保存文件夹名
     */
    const val SAVE_FOLDER = "p8"

    /**
     * 文件夹名
     */
    const  val FOLDER_MAINTENANCE = "maintenance"
    const val FOLDER_LOG = "log"
    const val FOLDER_ERROR = "error"
    const val FOLDER_SLIDE_BARCODE = "slide_barcode"

    /**
     * json文件名
     */
    val FILE_METHOD_PARAMS = "method_parameters.json" // 现方法参数
    val FILE_METHOD_PARAMS_GROUP = "method_parameters_group.json" // 方法参数组
    val FILE_SCALE = "scale.json" // 步转毫米scale值
    val FILE_TUBES_POS = "tubes_pos.json" // 试管位置
    val FILE_TIPS_POS = "tips_pos.json" // 枪头位置
    val FILE_SPRAY_POS = "spray_pos.json" // 喷雾位置
    val FILE_DISPENSE_POS = "dispense_pos.json" // 滴液位置
    val FILE_BARCODE_POS = "barcode_pos.json" // 二维码位置
    val FILE_FIXATIVE_POS = "fixative_pos.json" // 固定液位置
    val FILE_SLIDE_POS = "slide_pos.json" // 玻片位置
    val FILE_OTHER_POS = "other_pos.json" // 其他位置
    val FILE_GENERAL_PARAMETERS = "general_parameters.json" // 通用参数设置

    /**
     * 移液头量程
     */
    val HEAD_RANGE = 180

    /**
     * 移液头0位在左上还是右上（默认右上）
     */
    val HEAD_POS = EDirection.Right
}
