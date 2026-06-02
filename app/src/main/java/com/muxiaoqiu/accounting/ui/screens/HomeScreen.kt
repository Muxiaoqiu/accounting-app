package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.data.entity.Transaction
import com.muxiaoqiu.accounting.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AccountingViewModel,
    onAddClick: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val expense by viewModel.totalExpense.collectAsState()
    val income by viewModel.totalIncome.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加", tint = OnPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            SummaryBar(
                expense = expense ?: 0.0,
                income = income ?: 0.0
            )

            Text(
                text = "交易记录",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryBar(expense: Double, income: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("支出", expense, ExpenseRed)
            SummaryItem("收入", income, IncomeGreen)
            SummaryItem(
                "结余",
                income - expense,
                if (income - expense >= 0) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = OnPrimary.copy(alpha = 0.8f), fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "¥%.2f".format(amount),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (transaction.type == 0) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.take(2),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == 0) ExpenseRed else IncomeGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (transaction.note.isNotEmpty()) {
                    Text(
                        text = transaction.note,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == 0) "-" else "+"}¥%.2f".format(transaction.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.type == 0) ExpenseRed else IncomeGreen
                )
                Text(
                    text = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(transaction.timestamp)),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
