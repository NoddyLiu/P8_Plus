package com.superh2.library.utils

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.superh2.library.myEntityCommon.Barcode
import com.superh2.library.myEntityJson.*
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import com.superh2.library.utils.ParamsHelper.paramMethodParamsGroup
import com.superh2.library.utils.ParamsHelper.paramPosBarcode
import com.superh2.library.utils.ParamsHelper.paramPosDispense
import com.superh2.library.utils.ParamsHelper.paramPosFixative
import com.superh2.library.utils.ParamsHelper.paramPosOther
import com.superh2.library.utils.ParamsHelper.paramPosSlide
import com.superh2.library.utils.ParamsHelper.paramPosSpray
import com.superh2.library.utils.ParamsHelper.paramPosTips
import com.superh2.library.utils.ParamsHelper.paramPosTubes
import com.superh2.library.utils.ParamsHelper.paramScale

/**
 *@Description json加载类
 *@Author Noddy
 *@Time 2018/6/29 10:26
 */
object JsonParamHelper
{
    /**
     * 方法参数组
     */
    fun loadMethodParamsGroup()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_METHOD_PARAMS_GROUP)
            if (json != null && json != "") paramMethodParamsGroup = JSON.parseObject<MethodParamsGroup>(json, object : TypeReference<MethodParamsGroup>()
            {}.type)
            else
            {
                for (i in 1..20)
                {
                    var mp = MethodParams()
                    mp.groupName = "Group " + i.toString()
                    paramMethodParamsGroup.methodParamsGroup.add(mp)
                }
                FileUtils.saveMethodParametersGroup(paramMethodParamsGroup, false)
            }
        }
        catch (ex: Exception)
        {
            Log.i("loadMethodParamsGroup", ex.message)
        }
    }

    /**
     * scale值
     */
    fun loadScale()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_SCALE)
            if (json != null && json != "") paramScale = JSON.parseObject<Scale>(json, object : TypeReference<Scale>()
            {}.type)
            else FileUtils.saveScale(paramScale, false)
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 试管位置
     */
    fun loadTubesPos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_TUBES_POS)
            if (json != null && json != "") paramPosTubes = JSON.parseObject<PosTubes>(json, object : TypeReference<PosTubes>()
            {}.type)
            else
            {
                for (i in 0..63)
                {
                    var pos = Position()
                    paramPosTubes.tubes.add(pos)
                }
                FileUtils.saveTubesPos(paramPosTubes, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 枪头位置
     */
    fun loadTipsPos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_TIPS_POS)
            if (json != null && json != "") paramPosTips = JSON.parseObject<PosTips>(json, object : TypeReference<PosTips>()
            {}.type)
            else
            {
                for (i in 0..95)
                {
                    var pos = Position()
                    paramPosTips.tips.add(pos)
                }
                FileUtils.saveTipsPos(paramPosTips, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 喷雾位置
     */
    fun loadSprayPos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_SPRAY_POS)
            if (json != null && json != "") paramPosSpray = JSON.parseObject<PosSpray>(json, object : TypeReference<PosSpray>()
            {}.type)
            else
            {
                for (i in 0..63)
                {
                    var pos = Position()
                    paramPosSpray.slides.add(pos)
                }
                FileUtils.saveSprayPos(paramPosSpray, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 滴液位置
     */
    fun loadDispensePos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_DISPENSE_POS)
            if (json != null && json != "") paramPosDispense = JSON.parseObject<PosDispense>(json, object : TypeReference<PosDispense>()
            {}.type)
            else
            {
                for (i in 0..63)
                {
                    var pos = Position()
                    paramPosDispense.slides.add(pos)
                }
                FileUtils.saveDispensePos(paramPosDispense, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 滴液位置
     */
    fun loadBarcodePos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_BARCODE_POS)
            if (json != null && json != "") paramPosBarcode = JSON.parseObject<PosBarcode>(json, object : TypeReference<PosBarcode>()
            {}.type)
            else
            {
                for (i in 0..63)
                {
                    var pos = Position()
                    paramPosBarcode.slides.add(pos)
                }
                FileUtils.saveBarcodePos(paramPosBarcode, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 固定液位置
     */
    fun loadFixativePos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_FIXATIVE_POS)
            if (json != null && json != "") paramPosFixative = JSON.parseObject<PosFixative>(json, object : TypeReference<PosFixative>()
            {}.type)
            else
            {
                for (i in 0..63)
                {
                    var pos = Position()
                    paramPosFixative.slides.add(pos)
                }
                FileUtils.saveFixativePos(paramPosFixative, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     *  玻片位置
     */
    fun loadSlidePos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_SLIDE_POS)
            if (json != null && json != "") paramPosSlide = JSON.parseObject<PosSlide>(json, object : TypeReference<PosSlide>()
            {}.type)
            else
            {
                for (i in 0..63)
                {
                    var pos = Position()
                    paramPosSlide.slides.add(pos)
                }
                FileUtils.saveSlidePos(paramPosSlide, false)
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 其他位置
     */
    fun loadOtherPos()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_OTHER_POS)
            if (json != null && json != "") paramPosOther = JSON.parseObject<PosOther>(json, object : TypeReference<PosOther>()
            {}.type)
            else FileUtils.saveOtherPos(paramPosOther, false)
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 通用参数设置
     */
    fun loadGeneralParams()
    {
        try
        {
            val json = FileUtils.getJsonFromSD(ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_GENERAL_PARAMETERS)
            if (json != null && json != "")
            {
                paramGeneralParams = JSON.parseObject<GeneralParams>(json, object : TypeReference<GeneralParams>()
                {}.type)
                // 分散指数
                if (paramGeneralParams.DispersancyIndexList.count() == 0)
                {
                    for (i in 0..17)
                    {
                        paramGeneralParams.DispersancyIndexList.add(Humiture())
                    }
                }
            }
            else
            {
                FileUtils.saveGeneralParameters(paramGeneralParams, false)
                // 分散指数
                if (paramGeneralParams.DispersancyIndexList.count() == 0)
                {
                    for (i in 0..17)
                    {
                        paramGeneralParams.DispersancyIndexList.add(Humiture())
                    }
                }
            }
        }
        catch (ex: Exception)
        {
        }
    }

    /**
     * 二维码列表转JSON
     */
    fun barcodesToJson(barcodes: List<Barcode>): String
    {
        return JSON.toJSONString(barcodes, true)
    }

    /**
     * JSON转二维码列表
     */
    fun jsonToBarcodes(json: String): List<Barcode>
    {
        return if (json.isNotEmpty()) JSON.parseArray(json, Barcode::class.java)
        else emptyList()
    }

}