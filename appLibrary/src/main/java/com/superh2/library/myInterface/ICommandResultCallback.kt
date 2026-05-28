package com.superh2.library.myInterface

/**
 * 指令执行结果回调接口
 * Created by Noddy on 2018/4/23.
 */

interface ICommandResultCallback
{
    /**
     * 执行成功
     */
    fun success()

    /**
     * 执行失败
     * @param ex 异常类型
     */
    fun fail(ex: Exception)
}
