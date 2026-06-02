package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.data.entity.Transaction
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI

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
                title = {
                    Text("记一笔", style = MaterialTheme.typography.titleLarge)
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Expense/Income Toggle ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpense = true; selectedCategory = "" },
                    shape = MaterialTheme.shapes.medium,
                    color = if (isExpense)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "支出",
                        modifier = Modifier.padding(vertical = 14.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal,
                        color = if (isExpense)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpense = false; selectedCategory = "" },
                    shape = MaterialTheme.shapes.medium,
                    color = if (!isExpense)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "收入",
                        modifier = Modifier.padding(vertical = 14.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isExpense)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Amount Input ──
            Text(
                text = "金额",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                    placeholder = { Text("0.00", style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.outline)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Category Selection ──
            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            categories.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { category ->
                        val isSelected = selectedCategory == category
                        val emoji = CATEGORY_EMOJI[category] ?: "📋"
                        val bgColor = if (isSelected) {
                            if (isExpense) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f)
                                .clickable { selectedCategory = category },
                            shape = MaterialTheme.shapes.medium,
                            color = bgColor
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // fill remaining spots if row has less than 4
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Note ──
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Save Button ──
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    "保存",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
