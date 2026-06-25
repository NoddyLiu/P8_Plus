package com.superh2.library.utils

import com.superh2.library.myEntityJson.*
import com.superh2.library.utils.JsonParamHelper.loadBarcodePos
import com.superh2.library.utils.JsonParamHelper.loadCollectPos
import com.superh2.library.utils.JsonParamHelper.loadDispensePos
import com.superh2.library.utils.JsonParamHelper.loadFixativePos
import com.superh2.library.utils.JsonParamHelper.loadGeneralParams
import com.superh2.library.utils.JsonParamHelper.loadMethodParamsGroup
import com.superh2.library.utils.JsonParamHelper.loadOtherPos
import com.superh2.library.utils.JsonParamHelper.loadScale
import com.superh2.library.utils.JsonParamHelper.loadSlidePos
import com.superh2.library.utils.JsonParamHelper.loadSprayPos
import com.superh2.library.utils.JsonParamHelper.loadTipsPos
import com.superh2.library.utils.JsonParamHelper.loadTubesPos

/**
 *@Description 本地参数获取类
 *@Author Noddy
 *@Time 2018/10/20 16:10
 */
object ParamsHelper
{
//    // 方法参数设置
//    var paramMethodParams: MethodParams = MethodParams() // 现参数
    // 方法参数组
    var paramMethodParamsGroup:MethodParamsGroup = MethodParamsGroup() // 20组参数
    // scale值
    var paramScale: Scale = Scale()
    // 试管位置
    var paramPosTubes: PosTubes = PosTubes()
    // 枪头位置
    var paramPosTips: PosTips = PosTips()
    // 喷雾位置
    var paramPosSpray: PosSpray = PosSpray()
    // 滴液位置
    var paramPosDispense: PosDispense = PosDispense()
    // 二维码位置
    var paramPosBarcode: PosBarcode = PosBarcode()
    // 固定液位置
    var paramPosFixative: PosFixative = PosFixative()
    // 玻片位置
    var paramPosSlide:PosSlide = PosSlide()
    // 收集架位置
    var paramPosCollect:PosCollect = PosCollect()
    // 其他位置
    var paramPosOther:PosOther = PosOther()
    // 工程师参数设置
    var paramGeneralParams: GeneralParams = GeneralParams()

    /**
     * 加载所有参数
     */
    fun loadAllParams()
    {
        // 20组参数设置
        loadMethodParamsGroup()

        // Scale值
        loadScale()
        // 试管位置
        loadTubesPos()
        // 枪头位置
        loadTipsPos()
        // 喷雾位置
        loadSprayPos()
        // 滴液位置
        loadDispensePos()
        // 二维码位置
        loadBarcodePos()
        // 固定液位置
        loadFixativePos()
        // 玻片位置
        loadSlidePos()
        // 收集架位置
        loadCollectPos()
        // 其他位置
        loadOtherPos()
        // 通用参数
        loadGeneralParams()
    }
}