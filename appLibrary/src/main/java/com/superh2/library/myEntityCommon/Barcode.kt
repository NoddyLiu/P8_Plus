package com.superh2.library.myEntityCommon

/**
 *@Description 二维码实体类
 * @param barcode 二维码值
 * @param posIndex 所在位置
 * @param isWrong 二维码是否不能识别
 *@Author Noddy
 *@Time 2018/6/20 14:19
 */
class Barcode(var barcode: String, var posIndex: Int = -1, var isWrong:Boolean =  false)