package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(bookId) {
        viewModel.setBook(bookId)
    }

    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val expense by viewModel.totalExpense.collectAsState(initial = null)
    val income by viewModel.totalIncome.collectAsState(initial = null)

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = "记账", tint = MaterialTheme.colorScheme.onPrimary)
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
                SummaryCard(expense = expense ?: 0.0, income = income ?: 0.0)
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
                items(transactions, key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(expense: Double, income: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
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
private fun TransactionCard(transaction: Transaction, onDelete: () -> Unit) {
    val emoji = CATEGORY_EMOJI[transaction.category] ?: "📋"

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
    val cal = Calendar.getInstance()

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
