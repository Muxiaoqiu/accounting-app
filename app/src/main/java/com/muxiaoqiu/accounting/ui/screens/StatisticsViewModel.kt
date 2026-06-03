package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class TrendPeriod { WEEK, MONTH, YEAR }

data class TrendPoint(
    val label: String,
    val expense: Double,
    val income: Double
) {
    val surplus: Double get() = income - expense
}

data class CategorySlice(
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

val PIE_COLORS = listOf(
    Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD),
    Color(0xFF7986CB), Color(0xFF64B5F6), Color(0xFF4FC3F7), Color(0xFF4DD0E1),
    Color(0xFF80CBC4), Color(0xFF81C784), Color(0xFFAED581), Color(0xFFFFD54F),
    Color(0xFFFFB74D), Color(0xFFFF8A65), Color(0xFFA1887F), Color(0xFF90A4AE)
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(private val dao: TransactionDao) : ViewModel() {

    private val _bookId = MutableStateFlow(-1L)
    private val _trendPeriod = MutableStateFlow(TrendPeriod.MONTH)
    private val _pieType = MutableStateFlow(0) // 0=expense, 1=income

    val trendPeriod: StateFlow<TrendPeriod> = _trendPeriod
    val pieType: StateFlow<Int> = _pieType

    val trendData: StateFlow<List<TrendPoint>> = combine(_bookId, _trendPeriod) { id, period ->
        if (id == -1L) null else Pair(id, period)
    }.flatMapLatest { pair ->
        if (pair == null) flowOf(emptyList())
        else {
            val (id, period) = pair
            val (start, end) = getTimeRange(period)
            dao.getTransactionsInRange(id, start, end).map { txns ->
                buildTrendData(txns, period, start)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pieData: StateFlow<List<CategorySlice>> = combine(_bookId, _trendPeriod, _pieType) { id, period, type ->
        if (id == -1L) null else Triple(id, period, type)
    }.flatMapLatest { triple ->
        if (triple == null) flowOf(emptyList())
        else {
            val (id, period, type) = triple
            val (start, end) = getTimeRange(period)
            dao.getTransactionsInRange(id, start, end).map { txns ->
                buildPieData(txns, type)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setBook(bookId: Long) {
        _bookId.value = bookId
    }

    fun setTrendPeriod(period: TrendPeriod) {
        _trendPeriod.value = period
    }

    fun setPieType(type: Int) {
        _pieType.value = type
    }

    private fun getTimeRange(period: TrendPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (period) {
            TrendPeriod.WEEK -> {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysSinceMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TrendPeriod.MONTH -> {
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
            TrendPeriod.YEAR -> {
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

    private fun buildTrendData(transactions: List<Transaction>, period: TrendPeriod, rangeStart: Long): List<TrendPoint> {
        return when (period) {
            TrendPeriod.WEEK -> buildWeekTrend(transactions, rangeStart)
            TrendPeriod.MONTH -> buildMonthTrend(transactions, rangeStart)
            TrendPeriod.YEAR -> buildYearTrend(transactions, rangeStart)
        }
    }

    private fun buildWeekTrend(transactions: List<Transaction>, weekStart: Long): List<TrendPoint> {
        val dayNames = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val cal = Calendar.getInstance()
        return (0..6).map { day ->
            cal.timeInMillis = weekStart
            cal.add(Calendar.DAY_OF_YEAR, day)
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            val dayTx = transactions.filter { it.timestamp in dayStart until dayEnd }
            TrendPoint(
                label = dayNames[day],
                expense = dayTx.filter { it.type == 0 }.sumOf { it.amount },
                income = dayTx.filter { it.type == 1 }.sumOf { it.amount }
            )
        }
    }

    private fun buildMonthTrend(transactions: List<Transaction>, monthStart: Long): List<TrendPoint> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = monthStart
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        return (1..daysInMonth).map { day ->
            cal.timeInMillis = monthStart
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            val dayTx = transactions.filter { it.timestamp in dayStart until dayEnd }
            TrendPoint(
                label = if (day % 5 == 1 || day == 1 || day == daysInMonth) "${day}日" else "",
                expense = dayTx.filter { it.type == 0 }.sumOf { it.amount },
                income = dayTx.filter { it.type == 1 }.sumOf { it.amount }
            )
        }
    }

    private fun buildYearTrend(transactions: List<Transaction>, yearStart: Long): List<TrendPoint> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = yearStart
        val year = cal.get(Calendar.YEAR)

        return (1..12).map { month ->
            cal.set(year, month - 1, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis
            val monthTx = transactions.filter { it.timestamp in monthStart until monthEnd }
            TrendPoint(
                label = "${month}月",
                expense = monthTx.filter { it.type == 0 }.sumOf { it.amount },
                income = monthTx.filter { it.type == 1 }.sumOf { it.amount }
            )
        }
    }

    private fun buildPieData(transactions: List<Transaction>, type: Int): List<CategorySlice> {
        val filtered = transactions.filter { it.type == type }
        val total = filtered.sumOf { it.amount }
        if (total == 0.0) return emptyList()

        return filtered
            .groupBy { it.category }
            .map { (name, list) ->
                val amount = list.sumOf { it.amount }
                CategorySlice(
                    name = name,
                    amount = amount,
                    percentage = (amount / total).toFloat(),
                    color = Color.Unspecified
                )
            }
            .sortedByDescending { it.amount }
            .mapIndexed { index, slice ->
                slice.copy(color = PIE_COLORS[index % PIE_COLORS.size])
            }
    }
}
