package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.BookDao
import com.muxiaoqiu.accounting.data.entity.Book
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(private val bookDao: BookDao) : ViewModel() {

    val books = bookDao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createBook(name: String) {
        viewModelScope.launch {
            bookDao.insert(Book(name = name))
        }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch {
            bookDao.deleteTransactionsByBook(id)
            bookDao.delete(id)
        }
    }
}
