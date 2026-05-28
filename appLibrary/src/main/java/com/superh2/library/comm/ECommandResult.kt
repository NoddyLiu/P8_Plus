package com.superh2.library.comm

/**
 * 指令执行结果
 * Created by Noddy on 2018/4/23.
 */

enum class ECommandResult
{
    OK, // 指令执行成功
    SendFail, // 指令发送失败
    HaltExec, // 指令停止执行
    NoResponse, // 指令执行没有响应
    TimeOut // 指令执行超时
}
