package com.superh2.library.myEnum

/**
 *@Description 串口操作类型
 *@Author Noddy
 *@Time 2020/11/20 1619
 */
enum class ESerialOperation
{
    TempRealTime, // 温度实时值
    TempSet, // 温度设定值
    HumRealTime, // 湿度实时值
    HumSet, // 湿度设定值
    SetTemp, // 设置温度值
    SetHum, // 设置湿度值
    Toggle, // 启停
    SysStatus, // 系统状态
    TempAuto, // 温度自动演算
    Light, // 照明灯
    AlarmDoor //  开门报警
}