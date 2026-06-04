package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.BudgetDao
import com.muxiaoqiu.accounting.data.dao.TransactionDao
import com.muxiaoqiu.accounting.data.entity.Budget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetAlert(
    val budget: Budget,
    val spent: Double,
    val percentage: Float,
    val isOverBudget: Boolean
)

data class BudgetWithSpent(
    val budget: Budget,
    val spent: Double,
    val percentage: Float
)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _bookId = MutableStateFlow(-1L)
    private val cal = Calendar.getInstance()
    private val currentYear = cal.get(Calendar.YEAR)

    val budgets: StateFlow<List<Budget>> = _bookId.flatMapLatest { id ->
        if (id == -1L) flowOf(emptyList())
        else budgetDao.getByBookAndYear(id, currentYear)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<BudgetAlert>> = combine(_bookId, budgets) { id, list -> id to list }
        .flatMapLatest { (id, list) ->
            if (id == -1L || list.none { it.alertEnabled }) {
                flowOf(emptyList())
            } else {
                val enabled = list.filter { it.alertEnabled }
                if (enabled.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(enabled.map { budget ->
                        val (start, end) = getAlertTimeRange(budget)
                        val flow = if (budget.categoryName != null) {
                            transactionDao.getCategoryExpenseInRange(id, budget.categoryName!!, start, end)
                        } else {
                            transactionDao.getExpenseInRange(id, start, end)
                        }
                        flow.map { spent -> budget to (spent ?: 0.0) }
                    }) { results ->
                        results.mapNotNull { (budget, spent) ->
                            val pct = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceAtMost(2f) else 0f
                            if (pct >= budget.alertThreshold) {
                                BudgetAlert(budget, spent, pct, pct >= 1f)
                            } else null
                        }.sortedByDescending { it.percentage }
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetsWithSpent: StateFlow<List<BudgetWithSpent>> = combine(_bookId, budgets) { id, list -> id to list }
        .flatMapLatest { (id, list) ->
            if (id == -1L || list.isEmpty()) flowOf(emptyList())
            else {
                combine(list.map { budget ->
                    val (start, end) = getAlertTimeRange(budget)
                    val spentFlow = if (budget.categoryName != null)
                        transactionDao.getCategoryExpenseInRange(id, budget.categoryName!!, start, end)
                    else
                        transactionDao.getExpenseInRange(id, start, end)
                    spentFlow.map { spent -> budget to (spent ?: 0.0) }
                }) { results ->
                    results.map { (budget, spent) ->
                        val pct = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 2f) else 0f
                        BudgetWithSpent(budget, spent, pct)
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setBook(bookId: Long) {
        _bookId.value = bookId
    }

    fun setYearBudget(amount: Double) {
        viewModelScope.launch {
            val existing = budgetDao.getYearBudget(_bookId.value, currentYear)
            if (existing != null) {
                budgetDao.setAmount(existing.id, amount)
            } else {
                budgetDao.upsert(Budget(bookId = _bookId.value, year = currentYear, amount = amount))
            }
        }
    }

    fun setMonthBudget(month: Int, amount: Double) {
        viewModelScope.launch {
            val existing = budgetDao.getMonthBudget(_bookId.value, currentYear, month)
            if (existing != null) {
                budgetDao.setAmount(existing.id, amount)
            } else {
                budgetDao.upsert(
                    Budget(bookId = _bookId.value, year = currentYear, month = month, amount = amount)
                )
            }
        }
    }

    fun setCategoryBudget(month: Int, categoryName: String, amount: Double) {
        viewModelScope.launch {
            val existing = budgetDao.getCategoryBudget(_bookId.value, currentYear, month, categoryName)
            if (existing != null) {
                budgetDao.setAmount(existing.id, amount)
            } else {
                budgetDao.upsert(
                    Budget(
                        bookId = _bookId.value,
                        year = currentYear,
                        month = month,
                        categoryName = categoryName,
                        amount = amount
                    )
                )
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { budgetDao.delete(id) }
    }

    fun toggleAlert(id: Long, enabled: Boolean) {
        viewModelScope.launch { budgetDao.setAlertEnabled(id, enabled) }
    }

    fun setAlertThreshold(id: Long, threshold: Float) {
        viewModelScope.launch { budgetDao.setAlertThreshold(id, threshold) }
    }

    private fun getAlertTimeRange(budget: Budget): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return if (budget.month == null) {
            cal.set(budget.year, 0, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.YEAR, 1)
            val end = cal.timeInMillis
            Pair(start, end)
        } else {
            cal.set(budget.year, budget.month, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis
            Pair(start, end)
        }
    }
}
