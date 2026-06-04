package com.muxiaoqiu.accounting.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.muxiaoqiu.accounting.data.dao.BookDao
import com.muxiaoqiu.accounting.data.dao.BudgetDao
import com.muxiaoqiu.accounting.data.dao.CategoryDao
import com.muxiaoqiu.accounting.data.dao.TransactionDao
import com.muxiaoqiu.accounting.data.entity.Book
import com.muxiaoqiu.accounting.data.entity.Budget
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import com.muxiaoqiu.accounting.data.entity.Transaction

@Database(entities = [Transaction::class, Book::class, CategoryEntity::class, Budget::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN icon TEXT NOT NULL DEFAULT '📋'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        bookId INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        month INTEGER,
                        categoryName TEXT,
                        amount REAL NOT NULL,
                        alertEnabled INTEGER NOT NULL DEFAULT 0,
                        alertThreshold REAL NOT NULL DEFAULT 0.8
                    )
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "accounting.db"
                ).addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
