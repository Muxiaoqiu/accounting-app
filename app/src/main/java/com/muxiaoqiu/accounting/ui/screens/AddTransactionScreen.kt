package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.muxiaoqiu.accounting.data.entity.Transaction
import com.muxiaoqiu.accounting.ui.theme.*

private val EXPENSE_CATEGORIES = listOf("餐饮", "交通", "购物", "住房", "娱乐", "医疗", "教育", "其他")
private val INCOME_CATEGORIES = listOf("工资", "兼职", "理财", "红包", "报销", "其他")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    bookId: Long,
    viewModel: AccountingViewModel,
    onBack: () -> Unit
) {
    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val categories = if (isExpense) EXPENSE_CATEGORIES else INCOME_CATEGORIES

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true; selectedCategory = "" },
                    label = { Text("支出") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false; selectedCategory = "" },
                    label = { Text("收入") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("金额") },
                prefix = { Text("¥") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("选择分类", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            categories.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    if (selectedCategory.isEmpty()) return@Button
                    viewModel.addTransaction(
                        Transaction(
                            bookId = bookId,
                            amount = amt,
                            category = selectedCategory,
                            note = note,
                            type = if (isExpense) 0 else 1
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty()
            ) {
                Text("保存")
            }
        }
    }
}
