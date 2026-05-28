package com.superh2.library.myInterface

/**
 * @description 重命名监听接口
 * @author Noddy
 * @date 2018/11/05
 */
interface RenameListener
{
    /** 确认
     * @param index Index
     * @param newValue 新值
     */
    fun confirm(index:Int, newValue:String)

    /**
     * 取消
     */
    fun cancel()

}
