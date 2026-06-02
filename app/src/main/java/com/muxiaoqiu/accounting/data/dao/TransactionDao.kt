package com.muxiaoqiu.accounting.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.muxiaoqiu.accounting.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getAll(bookId: Long): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE bookId = :bookId AND type = 0")
    fun getTotalExpense(bookId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE bookId = :bookId AND type = 1")
    fun getTotalIncome(bookId: Long): Flow<Double?>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}
