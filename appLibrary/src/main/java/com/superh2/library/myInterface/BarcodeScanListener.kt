package com.superh2.library.myInterface

/**
 *@Description 扫描枪回调事件
 *@Author Noddy
 *@Time 2018/6/8 17:40
 */
interface BarcodeScanListener
{
    fun onBarcodeScanCallback(barcode:String)
}