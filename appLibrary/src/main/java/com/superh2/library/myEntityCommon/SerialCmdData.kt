package com.superh2.library.myEntityCommon

import com.superh2.library.myEnum.ESerialOperation
import com.superh2.library.myInterface.ICommandResultCallback

/**
 *@Description 串口指令数据实体类
 * @param data 指令数据
 * @param serialOperation 指令类型
 * @param callback 回调函数
 *@Author liu_ja
 *@Time 2021/9/29 14:19
 */
class SerialCmdData(var data: ByteArray, var serialOperation: ESerialOperation,var callback: ICommandResultCallback? =  null)