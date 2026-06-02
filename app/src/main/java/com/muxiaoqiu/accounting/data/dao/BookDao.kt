package com.muxiaoqiu.accounting.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.muxiaoqiu.accounting.data.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Book>>

    @Insert
    suspend fun insert(book: Book): Long

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM transactions WHERE bookId = :bookId")
    suspend fun deleteTransactionsByBook(bookId: Long)
}
