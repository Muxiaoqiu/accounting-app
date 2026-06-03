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

@OptIn(ExperimentalCoroutinesApi::class)
class AccountingViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _bookId = MutableStateFlow(-1L)

    private val cal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(cal.get(Calendar.YEAR))
    // null = full year, 0-11 = specific month
    private val _selectedMonth = MutableStateFlow<Int?>(cal.get(Calendar.MONTH))

    val selectedYear: StateFlow<Int> = _selectedYear
    val selectedMonth: StateFlow<Int?> = _selectedMonth

    val periodLabel: StateFlow<String> = combine(_selectedYear, _selectedMonth) { year, month ->
        if (month == null) "${year}年" else "${year}年${month + 1}月"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // transactions filtered by selected period
    val transactions = combine(_bookId, _selectedYear, _selectedMonth) { id, year, month ->
        if (id == -1L) null else Triple(id, year, month)
    }.flatMapLatest { triple ->
        if (triple == null) flowOf(emptyList())
        else {
            val (id, year, month) = triple
            val (start, end) = getTimeRange(year, month)
            dao.getTransactionsInRange(id, start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense = combine(_bookId, _selectedYear, _selectedMonth) { id, year, month ->
        if (id == -1L) null else Triple(id, year, month)
    }.flatMapLatest { triple ->
        if (triple == null) flowOf(null)
        else {
            val (id, year, month) = triple
            val (start, end) = getTimeRange(year, month)
            dao.getExpenseInRange(id, start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalIncome = combine(_bookId, _selectedYear, _selectedMonth) { id, year, month ->
        if (id == -1L) null else Triple(id, year, month)
    }.flatMapLatest { triple ->
        if (triple == null) flowOf(null)
        else {
            val (id, year, month) = triple
            val (start, end) = getTimeRange(year, month)
            dao.getIncomeInRange(id, start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setBook(bookId: Long) {
        _bookId.value = bookId
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun selectMonth(month: Int?) {
        _selectedMonth.value = month
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

    private fun getTimeRange(year: Int, month: Int?): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        if (month == null) {
            // Full year
            cal.set(year, 0, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.YEAR, 1)
            val end = cal.timeInMillis
            return Pair(start, end)
        } else {
            // Specific month
            cal.set(year, month, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis
            return Pair(start, end)
        }
    }
}
