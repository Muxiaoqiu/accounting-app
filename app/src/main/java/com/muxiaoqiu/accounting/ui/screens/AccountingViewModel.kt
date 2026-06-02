package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.TransactionDao
import com.muxiaoqiu.accounting.data.entity.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AccountingViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _bookId = MutableStateFlow(-1L)
    val bookId: StateFlow<Long> = _bookId

    val transactions = _bookId.flatMapLatest { id ->
        if (id == -1L) kotlinx.coroutines.flow.flowOf(emptyList())
        else dao.getAll(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense = _bookId.flatMapLatest { id ->
        if (id == -1L) kotlinx.coroutines.flow.flowOf(null)
        else dao.getTotalExpense(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalIncome = _bookId.flatMapLatest { id ->
        if (id == -1L) kotlinx.coroutines.flow.flowOf(null)
        else dao.getTotalIncome(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setBook(bookId: Long) {
        _bookId.value = bookId
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.insert(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            dao.delete(id)
        }
    }
}
