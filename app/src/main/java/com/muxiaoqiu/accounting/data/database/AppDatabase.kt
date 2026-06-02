package com.muxiaoqiu.accounting.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.muxiaoqiu.accounting.data.dao.TransactionDao
import com.muxiaoqiu.accounting.data.entity.Transaction

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "accounting.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
