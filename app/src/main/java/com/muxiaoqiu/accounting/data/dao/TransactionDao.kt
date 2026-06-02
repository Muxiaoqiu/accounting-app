package com.muxiaoqiu.accounting.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.muxiaoqiu.accounting.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: Int): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 0")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 1")
    fun getTotalIncome(): Flow<Double?>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}
