package com.muxiaoqiu.accounting.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.muxiaoqiu.accounting.data.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE bookId = :bookId AND year = :year ORDER BY month ASC")
    fun getByBookAndYear(bookId: Long, year: Int): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget): Long

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE budgets SET alertEnabled = :enabled WHERE id = :id")
    suspend fun setAlertEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE budgets SET alertThreshold = :threshold WHERE id = :id")
    suspend fun setAlertThreshold(id: Long, threshold: Float)

    @Query("UPDATE budgets SET amount = :amount WHERE id = :id")
    suspend fun setAmount(id: Long, amount: Double)

    @Query("SELECT * FROM budgets WHERE bookId = :bookId AND year = :year AND month IS NULL AND categoryName IS NULL LIMIT 1")
    suspend fun getYearBudget(bookId: Long, year: Int): Budget?

    @Query("SELECT * FROM budgets WHERE bookId = :bookId AND year = :year AND month = :month AND categoryName IS NULL LIMIT 1")
    suspend fun getMonthBudget(bookId: Long, year: Int, month: Int): Budget?

    @Query("SELECT * FROM budgets WHERE bookId = :bookId AND year = :year AND month = :month AND categoryName = :categoryName LIMIT 1")
    suspend fun getCategoryBudget(bookId: Long, year: Int, month: Int, categoryName: String): Budget?
}
