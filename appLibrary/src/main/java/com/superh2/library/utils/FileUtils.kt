package com.superh2.library.utils

import android.os.Environment
import com.alibaba.fastjson.JSON
import com.superh2.library.myEntityJson.*
import java.io.*

/**
 * 文件操作工具类
 * Created by Liuja on 2018/4/23.
 */

object FileUtils
{
    private val TAG = "FileUtils"

    /**
     * 预处理文件夹和文件
     *
     * @param folderPath 文件夹路径
     * @param fileName   文件名
     */
    private fun preProcessFolderAndFile(folderPath: String, fileName: String): File?
    {
        var file: File? = null
        try
        {
            val folder = File(folderPath)
            if (!folder.exists())
            // 是否存在文件夹
                folder.mkdirs()

            val filePath = folderPath + File.separator + fileName // 文件路径
            file = File(filePath)
            if (!file.exists() && !file.isDirectory)
            // 是否存在文件
                file.createNewFile()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }

        return file
    }


    /**
     * 获取json
     * @param  folderName 文件夹名
     * @param fileName 文件名
     * @return
     */
    fun getJsonFromSD(folderName: String, fileName: String): String
    {
        var fileInputStream: FileInputStream? = null
        var inputStreamReader: InputStreamReader? = null
        var bufferedReader: BufferedReader? = null
        val jsonBuilder = StringBuilder()

        try
        {
            // 预处理文件夹和文件
            var filefolder = ""
            if (folderName == "")
                filefolder = Environment.getExternalStorageDirectory().toString() + File.separator + ConstantsUtils.SAVE_FOLDER // 文件路径(/sdcard/p8/)
            else
                filefolder = Environment.getExternalStorageDirectory().toString() + File.separator + ConstantsUtils.SAVE_FOLDER + File.separator + folderName // 文件路径(/sdcard/p8/......)

            val file = preProcessFolderAndFile(filefolder, fileName)

            fileInputStream = FileInputStream(file!!)
            inputStreamReader = InputStreamReader(fileInputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            for (line in bufferedReader.readLines())
            {
                jsonBuilder.append(line)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            try
            {
                if (null != bufferedReader)
                    bufferedReader.close()
                if (null != inputStreamReader)
                    inputStreamReader.close()
                if (null != fileInputStream)
                    fileInputStream.close()
            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }

        }

        return jsonBuilder.toString()
    }

    /**
     * 保存json
     * @param json
     * @param  folderName 文件夹名
     * @param fileName 文件名
     * @param isDeleteOld 是否删除旧版本
     * @return
     */
    fun saveJsonToSD(json: String, folderName: String, fileName: String, isDeleteOld: Boolean): Boolean
    {
        var result = true // 运行结果

        var fileWriter: FileWriter? = null
        var bufferedWriter: BufferedWriter? = null

        try
        {
            // 预处理文件夹和文件
            var filefolder = ""
            if (folderName == "")
                filefolder = Environment.getExternalStorageDirectory().toString() + File.separator + ConstantsUtils.SAVE_FOLDER // 文件路径(/sdcard/p8/)
            else
                filefolder = Environment.getExternalStorageDirectory().toString() + File.separator + ConstantsUtils.SAVE_FOLDER + File.separator + folderName // 文件路径(/sdcard/p8/......)

            // 用新文件替换旧文件的方式更改txt内容
            var file: File?

            file = preProcessFolderAndFile(filefolder, fileName) // 先删除旧文件
            if (isDeleteOld && file!!.delete())
            {
                file = preProcessFolderAndFile(filefolder, fileName) // 重新生成新文件
            }

            val fileDescriptor = FileOutputStream(file).fd
            fileWriter = FileWriter(fileDescriptor)
            bufferedWriter = BufferedWriter(fileWriter)

            bufferedWriter.write(json)
            bufferedWriter.flush()
            // 同步到物理存储中，避免数据丢失
            fileDescriptor.sync()

            bufferedWriter.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()

            result = false
        }
        finally
        {
            try
            {
                if (null != bufferedWriter)
                    bufferedWriter.close()
                if (null != fileWriter)
                    fileWriter.close()
            }
            catch (e: IOException)
            {
                e.printStackTrace()
                result = false
            }

        }

        return result
    }

    /**
     * 保存轴scale
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveScale(jsonObj: Scale, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_SCALE, isDeleteOld)
    }

    /**
     * 保存试管位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveTubesPos(jsonObj: PosTubes, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_TUBES_POS, isDeleteOld)
    }

    /**
     * 保存枪头位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveTipsPos(jsonObj: PosTips, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_TIPS_POS, isDeleteOld)
    }

    /**
     * 保存湿气吹风位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveHumidBlowPos(jsonObj: PosHumidBlow, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_HUMID_BLOW_POS, isDeleteOld)
    }

    /**
     * 保存喷雾位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveSprayPos(jsonObj: PosSpray, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_SPRAY_POS, isDeleteOld)
    }

    /**
     * 保存滴液位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveDispensePos(jsonObj: PosDispense, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_DISPENSE_POS, isDeleteOld)
    }

    /**
     * 保存二维码位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveBarcodePos(jsonObj: PosBarcode, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_BARCODE_POS, isDeleteOld)
    }

    /**
     * 保存固定液位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveFixativePos(jsonObj: PosFixative, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_FIXATIVE_POS, isDeleteOld)
    }

    /**
     * 保存玻片位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveSlidePos(jsonObj: PosSlide, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_SLIDE_POS, isDeleteOld)
    }

    /**
     * 保存其他位置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveOtherPos(jsonObj: PosOther, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_OTHER_POS, isDeleteOld)
    }


    /**
     * 保存方法参数组
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveMethodParametersGroup(jsonObj: MethodParamsGroup, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_METHOD_PARAMS_GROUP, isDeleteOld)
    }

    /**
     * 保存通用参数设置
     * @param jsonObj json实体类
     * @param isDeleteOld 是否删除旧版本
     */
    fun saveGeneralParameters(jsonObj: GeneralParams, isDeleteOld: Boolean): Boolean
    {
        return FileUtils.saveJsonToSD(JSON.toJSONString(jsonObj, true), ConstantsUtils.FOLDER_MAINTENANCE, ConstantsUtils.FILE_GENERAL_PARAMETERS, isDeleteOld)
    }
}
