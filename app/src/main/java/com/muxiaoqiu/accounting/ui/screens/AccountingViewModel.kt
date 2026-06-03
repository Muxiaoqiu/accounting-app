package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.TransactionDao
import com.muxiaoqiu.accounting.data.entity.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeFilter { MONTH, YEAR }

@OptIn(ExperimentalCoroutinesApi::class)
class AccountingViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _bookId = MutableStateFlow(-1L)
    val bookId: StateFlow<Long> = _bookId

    private val _timeFilter = MutableStateFlow(TimeFilter.MONTH)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter

    val transactions = _bookId.flatMapLatest { id ->
        if (id == -1L) flowOf(emptyList())
        else dao.getAll(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense = combine(_bookId, _timeFilter) { id, filter ->
        if (id == -1L) null else Pair(id, getTimeRange(filter))
    }.flatMapLatest { pair ->
        if (pair == null) flowOf(null)
        else dao.getExpenseInRange(pair.first, pair.second.first, pair.second.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalIncome = combine(_bookId, _timeFilter) { id, filter ->
        if (id == -1L) null else Pair(id, getTimeRange(filter))
    }.flatMapLatest { pair ->
        if (pair == null) flowOf(null)
        else dao.getIncomeInRange(pair.first, pair.second.first, pair.second.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setBook(bookId: Long) {
        _bookId.value = bookId
    }

    fun toggleTimeFilter() {
        _timeFilter.value = if (_timeFilter.value == TimeFilter.MONTH) TimeFilter.YEAR else TimeFilter.MONTH
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

    private fun getTimeRange(filter: TimeFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (filter) {
            TimeFilter.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                val end = cal.timeInMillis
                Pair(start, end)
            }
        }
    }
}
