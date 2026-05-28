package com.superh2.library.myInterface

/**
 * 扫码执行结果回调接口
 * @Author liu_ja
 * @CreateDate 2020/05/25 14:02
 */
interface IScannerResultCallback
{
    /**
     * 成功
     * @param info 扫描成功返回的信息
     */
    fun success(info:String)

    /**
     * 超时
     */
    fun timeOut()
}
