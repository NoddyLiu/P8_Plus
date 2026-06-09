package com.superh2.p8.serial

import com.superh2.library.myInterface.IPipetteDriver
import com.superh2.library.utils.ParamsHelper.paramGeneralParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Description: 移液器驱动
 * Author: liu_ja
 * Created On: 2026/6/9 11:03
 */
class AdpPipetteDriver(private val client: SerialClientAdp) : IPipetteDriver
{
    override fun init() = runBlocking {
        client.initializePump()
        waitUntilInitialized()
    }

    /**
     * 设置吸液速度
     * 等级1~12
     */
    override fun setAspirateSpeed(level: Int) = runBlocking {
        client.setAspirateSpeed(mapSpeedLevel(level))
    }

    /**
     * 设置吐液速度
     * 等级1~12
     */
    override fun setDispenseSpeed(level: Int) = runBlocking {
        client.setDispenseSpeed(mapSpeedLevel(level))
    }

    /**
     * 预吸空气
     */
    override fun firstAspirateBackAir(volumeUl: Int) = runBlocking {
        require(volumeUl >= 0) { "预吸空气体积不能小于 0" }
        if (volumeUl == 0) return@runBlocking
        runMotion("预吸空气") { client.firstAspirateBackAir() }
    }

    /**
     * 吸液
     */
    override fun aspirate(volumeUl: Int) = runBlocking {
        require(volumeUl >= 0) { "吸液体积不能小于 0" }
        if (volumeUl == 0) return@runBlocking
        runMotion("吸液") { client.aspirate(volumeUl) }
    }

    /**
     * 气封
     */
    override fun secondAspirateBackAir(volumeUl: Int) = runBlocking {
        require(volumeUl >= 0) { "气封体积不能小于 0" }
        if (volumeUl == 0) return@runBlocking
        runMotion("气封") { client.secondAspirateBackAir() }
    }

    /**
     * 吐液
     */
    override fun dispense(volumeUl: Int) = runBlocking {
        require(volumeUl >= 0) { "吐液体积不能小于 0" }
        runMotion("吐液") { client.dispense(volumeUl) }
    }

    /**
     * 退枪头
     */
    override fun releaseTip()= runBlocking {
        client.releaseTip()
        waitUntilIdle("退枪头")
    }

    /**
     * 检查是否有枪头
     * 0x01 有枪头
     * 0x02 无枪头
     */
    override fun hasTip(): Boolean = runBlocking {
        client.checkTipStatus() == 0x01
    }

    /**
     * 根据速度等级（1~12）转化为ADP的速度范围（100μl ~ dispenseSpeedMaxTarget μl）
     */
    private fun mapSpeedLevel(level: Int): Int
    {
        val safeLevel = level.coerceIn(1, 12)
        val minSpeed = 100
        val maxSpeed = paramGeneralParams.dispenseSpeedMaxTarget.coerceAtLeast(minSpeed)
        return minSpeed + ((maxSpeed - minSpeed) * (safeLevel - 1) / 11f).toInt()
    }

    private suspend fun runMotion(actionName: String, start: suspend () -> Int) {
        val result = start()
        if (result == 0x02) {
            throw IOException("$actionName 不可执行")
        }
        waitUntilIdle(actionName)
    }

    /**
     * 等待ADP完成初始化，轮询查询状态直到完成或超时
     */
    private suspend fun waitUntilInitialized(timeoutMs: Long = 15_000L)
    {
        withTimeout(timeoutMs.milliseconds) {
            while (true)
            {
                when (val status = client.queryInitStatus())
                {
                    0x00 -> delay(100.milliseconds)
                    0x01 -> return@withTimeout
                    0x02 -> throw IOException("ADP 初始化失败")
                    0x03 -> throw IOException("ADP 未回零")
                    else -> throw IOException("ADP 初始化状态异常: ${status.toString(16)}")
                }
            }
        }
    }

    /**
     * 等待ADP完成当前动作，轮询查询状态直到完成或超时
     */
    private suspend fun waitUntilIdle(actionName: String, timeoutMs: Long = 30_000L)
    {
        withTimeout(timeoutMs.milliseconds) {
            while (true)
            {
                when (val status = client.queryStatus())
                {
                    0x00 -> delay(100.milliseconds)
                    0x01 -> return@withTimeout
                    else -> throw IOException("$actionName 执行异常，状态码=${status.toString(16)}")
                }
            }
        }
    }
}