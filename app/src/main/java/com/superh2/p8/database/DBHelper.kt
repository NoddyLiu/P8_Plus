package com.superh2.p8.database

import com.superh2.library.utils.LogHelper
import com.superh2.p8.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Description:数据库帮助类
 * Author: liu_ja
 * Created On: 2025/9/15 15:21
 */
object DBHelper
{
    /**
     * 数据库相关
     */
    private lateinit var db: AppDatabase
    private lateinit var runStateDao: RunStateDao
    private var currentRunStateId: Long = -1
    // 最新状态
    private var latestRunState: RunStateEntity? = null

    // 自定义协程作用域
    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 初始化数据库
     */
    fun initDB()
    {
        // 初始化数据库
        db = AppDatabase.getDatabase(MainActivity.mContext)
        runStateDao = db.runStateDao()
    }

    /**
     * 获取最新运行状态
     */
    fun getLatestRunState(): RunStateEntity?
    {
        val job = coroutineScope.launch {
            try
            {
                latestRunState = runStateDao.getLatestRunState()
                latestRunState?.let { currentRunStateId = it.id }
            }
            catch (e: Exception)
            {
                LogHelper.error("数据库获取最新运行状态失败: ${e.message}")
            }
        }
        // 等待协程完成
        runBlocking {
            job.join()
        }
        return latestRunState
    }

    /**
     * 插入新状态
     * @param isBarcodeMode 是否扫码模式
     * @param isInterrupted 是否被中断
     */
    fun insertRunState(isBarcodeMode: Boolean, isInterrupted: Boolean)
    {
        val job = coroutineScope.launch {
            try
            {
                val newId = runStateDao.insert(RunStateEntity(isBarcodeMode = isBarcodeMode, isInterrupted = isInterrupted))
                currentRunStateId = newId
            }
            catch (e: Exception)
            {
                LogHelper.error("数据库插入运行状态失败: ${e.message}")
            }
        }
        // 等待协程完成
        runBlocking {
            job.join()
        }
    }

    /**
     * 更新运行状态
     */
    fun updateRunState(state: RunStateEntity)
    {
        val job = coroutineScope.launch {
            try
            {
                // 更新现有状态
                if (currentRunStateId > 0)
                {
                    runStateDao.update(state)
                    latestRunState = getLatestRunState()
                }
            }
            catch (e: Exception)
            {
                LogHelper.error("数据库更新运行状态失败: ${e.message}")
            }
        }
        // 等待协程完成
        runBlocking {
            job.join()
        }
    }

    /**
     * 标记程序isInterrupted状态
     * @param isInterrupted 是否异常中断
     */
    fun markRunCompleted(isInterrupted: Boolean)
    {
        val job = coroutineScope.launch {
            try
            {
                if (currentRunStateId > 0)
                {
                    runStateDao.getLatestRunState()?.let { state ->
                        val newState = state.copy(isInterrupted = isInterrupted)
                        runStateDao.update(newState)
                    }
                }
            }
            catch (e: Exception)
            {
                LogHelper.error("数据库标记isInterrupted状态失败: ${e.message}")
            }
        }
        // 等待协程完成
        runBlocking {
            job.join()
        }
    }
}