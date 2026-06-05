package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.data.entity.Transaction
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bookId: Long,
    bookName: String,
    viewModel: AccountingViewModel,
    categoryViewModel: CategoryViewModel,
    budgetViewModel: BudgetViewModel,
    onAddClick: () -> Unit,
    onBack: () -> Unit,
    onStatsClick: () -> Unit,
    onBudgetClick: () -> Unit
) {
    LaunchedEffect(bookId) {
        viewModel.setBook(bookId)
        budgetViewModel.setBook(bookId)
    }

    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val expense by viewModel.totalExpense.collectAsState(initial = null)
    val income by viewModel.totalIncome.collectAsState(initial = null)
    val periodLabel by viewModel.periodLabel.collectAsState(initial = "")

    val expenseCategories by categoryViewModel.expenseCategories.collectAsState(initial = emptyList())
    val incomeCategories by categoryViewModel.incomeCategories.collectAsState(initial = emptyList())
    val categoryIconMap = remember(expenseCategories, incomeCategories) {
        (expenseCategories + incomeCategories).associate { it.name to it.icon }
    }

    val budgetAlerts by budgetViewModel.alerts.collectAsState(initial = emptyList())

    val groupedTransactions = remember(transactions) {
        groupTransactionsByDay(transactions)
    }

    var showPicker by remember { mutableStateOf(false) }
    val selectedYear by viewModel.selectedYear.collectAsState(initial = Calendar.getInstance().get(Calendar.YEAR))
    val selectedMonth by viewModel.selectedMonth.collectAsState(initial = Calendar.getInstance().get(Calendar.MONTH))

    if (showPicker) {
        PeriodPickerDialog(
            year = selectedYear,
            month = selectedMonth,
            onYearChanged = { viewModel.selectYear(it) },
            onMonthSelected = { month ->
                viewModel.selectMonth(month)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(bookName, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBudgetClick) {
                        Icon(
                            Icons.Outlined.AccountBalance,
                            contentDescription = "预算",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onStatsClick) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = "统计",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = onAddClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "记账", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Summary Card ──
            item {
                SummaryCard(
                    expense = expense ?: 0.0,
                    income = income ?: 0.0,
                    periodLabel = periodLabel,
                    onPickPeriod = { showPicker = true }
                )
            }

            // ── Budget Alerts ──
            if (budgetAlerts.isNotEmpty()) {
                item {
                    BudgetAlertCard(
                        alerts = budgetAlerts,
                        onClick = onBudgetClick
                    )
                }
            }

            // ── Section header ──
            item {
                Text(
                    "交易记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "还没有记录，点下方按钮记一笔吧",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { group ->
                    item(key = "header_${group.dateLabel}") {
                        DayHeader(dateLabel = group.dateLabel, expense = group.dayExpense, income = group.dayIncome)
                    }
                    items(group.transactions, key = { it.id }) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            categoryIconMap = categoryIconMap,
                            onDelete = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodPickerDialog(
    year: Int,
    month: Int?,  // null = full year
    onYearChanged: (Int) -> Unit,
    onMonthSelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onYearChanged(year - 1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "上一年")
                }
                Surface(
                    modifier = Modifier.clickable { onMonthSelected(null) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (month == null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        "${year}年",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (month == null) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { onYearChanged(year + 1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "下一年")
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // "Full year" option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMonthSelected(null) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (month == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "全年",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (month == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (month == null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Month grid
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..3) {
                            val m = row * 4 + col
                            val isSelected = m == month
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.5f)
                                    .clickable { onMonthSelected(m) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${m + 1}月",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    if (row < 2) Spacer(modifier = Modifier.height(10.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DayHeader(dateLabel: String, expense: Double, income: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = dateLabel,
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (expense > 0) {
            Text(
                text = " 支出 ¥%.2f".format(expense),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
        if (income > 0) {
            Text(
                text = " 收入 ¥%.2f".format(income),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun SummaryCard(
    expense: Double,
    income: Double,
    periodLabel: String,
    onPickPeriod: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    modifier = Modifier.clickable { onPickPeriod() },
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = periodLabel,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "支出",
                    amount = expense,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    amountColor = androidx.compose.ui.graphics.Color(0xFFFFCDD2)
                )
                SummaryItem(
                    label = "收入",
                    amount = income,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    amountColor = androidx.compose.ui.graphics.Color(0xFFC8E6C9)
                )
                SummaryItem(
                    label = "结余",
                    amount = income - expense,
                    icon = Icons.Outlined.AccountBalance,
                    amountColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    amountColor: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¥ %.2f".format(amount),
            color = amountColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun TransactionCard(transaction: Transaction, categoryIconMap: Map<String, String>, onDelete: () -> Unit) {
    val emoji = categoryIconMap[transaction.category]
        ?: CATEGORY_EMOJI[transaction.category]
        ?: "📋"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (transaction.type == 0)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (transaction.note.isNotEmpty()) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = formatRelativeTime(transaction.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == 0) "-" else "+"}¥ %.2f".format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.type == 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - TimeUnit.DAYS.toMillis(1)

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}分钟前"
        diff < TimeUnit.HOURS.toMillis(3) -> "${TimeUnit.MILLISECONDS.toHours(diff)}小时前"
        timestamp >= todayStart -> "今天"
        timestamp >= yesterdayStart -> "昨天"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun groupTransactionsByDay(transactions: List<Transaction>): List<TransactionGroup> {
    val dayNames = arrayOf("", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    return transactions
        .groupBy { tx ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = tx.timestamp
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
        .map { (key, list) ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, key.first)
            cal.set(Calendar.MONTH, key.second)
            cal.set(Calendar.DAY_OF_MONTH, key.third)
            val dayOfWeek = dayNames[cal.get(Calendar.DAY_OF_WEEK)]
            val label = "${key.second + 1}月${key.third}日 $dayOfWeek"
            val expense = list.filter { it.type == 0 }.sumOf { it.amount }
            val income = list.filter { it.type == 1 }.sumOf { it.amount }
            TransactionGroup(dateLabel = label, transactions = list, dayExpense = expense, dayIncome = income)
        }
}

private data class TransactionGroup(
    val dateLabel: String,
    val transactions: List<Transaction>,
    val dayExpense: Double,
    val dayIncome: Double
)
