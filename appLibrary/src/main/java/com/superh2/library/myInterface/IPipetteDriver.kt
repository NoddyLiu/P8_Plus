package com.superh2.library.myInterface

/**
 * Description: 移液器驱动接口
 * Author: liu_ja
 * Created On: 2026/6/9 10:22
 */
interface IPipetteDriver
{
    fun init()
    fun setAspirateSpeed(level: Int)
    fun setDispenseSpeed(level: Int)
    fun firstAspirateBackAir(volumeUl: Int)
    fun aspirate(volumeUl: Int)
    fun secondAspirateBackAir(volumeUl: Int)
    fun dispense(volumeUl: Int)
    fun releaseTip()
    fun hasTip(): Boolean
}