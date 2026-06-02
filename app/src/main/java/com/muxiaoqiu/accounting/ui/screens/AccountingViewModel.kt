package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.TransactionDao
import com.muxiaoqiu.accounting.data.entity.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountingViewModel(private val dao: TransactionDao) : ViewModel() {

    val transactions: Flow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: Flow<Double?> = dao.getTotalExpense()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalIncome: Flow<Double?> = dao.getTotalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
