package com.superh2.library.myInterface

import com.superh2.library.myEntityJson.Humiture

/**
 *@Description 分散指数设置执行结果回调接口
 *@Author liu_ja
 *@Time 2023/12/23 13:34
 */

interface IDispersancyIndexCallback
{
    /**
     * 确定
     */
    fun confirm(humiture: Humiture)

    /**
     * 取消
     */
    fun cancel()
}
