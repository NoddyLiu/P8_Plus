package com.superh2.p8.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Description: 运行状态DAO
 * Author: liu_ja
 * Created On: 2025/8/13 14:29
 */
@Dao
interface RunStateDao
{
    @Insert
    suspend fun insert(state: RunStateEntity): Long

    @Update
    suspend fun update(state: RunStateEntity)

    @Query("SELECT * FROM run_states ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRunState(): RunStateEntity?

    @Query("DELETE FROM run_states WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(state: RunStateEntity)

    @Query("DELETE FROM run_states")
    suspend fun clearAll()
}