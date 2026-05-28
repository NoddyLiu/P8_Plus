package com.superh2.p8.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Description: 数据库类
 * Author: liu_ja
 * Created On: 2025/8/13 15:10
 */
@Database(entities = [RunStateEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun runStateDao(): RunStateDao

    companion object
    {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase
        {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "run_state_db").fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}