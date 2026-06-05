package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.data.entity.Budget
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    bookId: Long,
    viewModel: BudgetViewModel,
    categoryViewModel: CategoryViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(bookId) {
        viewModel.setBook(bookId)
    }

    val budgetsWithSpent by viewModel.budgetsWithSpent.collectAsState(initial = emptyList())
    val expenseCategories by categoryViewModel.expenseCategories.collectAsState(initial = emptyList())

    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)

    var editDialog by remember { mutableStateOf<BudgetEditInfo?>(null) }
    var addCategoryDialog by remember { mutableStateOf(false) }
    var thresholdDialog by remember { mutableStateOf<Budget?>(null) }
    var deleteConfirm by remember { mutableStateOf<Budget?>(null) }

    val yearBudget = budgetsWithSpent.find { it.budget.month == null && it.budget.categoryName == null }
    val monthBudget = budgetsWithSpent.find { it.budget.month == currentMonth && it.budget.categoryName == null }
    val categoryBudgets = budgetsWithSpent.filter { it.budget.categoryName != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算设置", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Year Budget ──
            SectionTitle("年度预算（${currentYear}年）")
            Spacer(modifier = Modifier.height(8.dp))
            BudgetCard(
                label = "${currentYear}年总支出预算",
                bws = yearBudget,
                onClickEdit = { existing ->
                    editDialog = BudgetEditInfo(
                        budget = existing?.budget,
                        defaultAmount = existing?.budget?.amount?.toString() ?: "",
                        isYear = true
                    )
                },
                onToggleAlert = { budget ->
                    viewModel.toggleAlert(budget.id, !budget.alertEnabled)
                },
                onSetThreshold = { thresholdDialog = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Month Budget ──
            SectionTitle("当月预算（${currentMonth + 1}月）")
            Spacer(modifier = Modifier.height(8.dp))
            BudgetCard(
                label = "${currentMonth + 1}月总支出预算",
                bws = monthBudget,
                onClickEdit = { existing ->
                    editDialog = BudgetEditInfo(
                        budget = existing?.budget,
                        defaultAmount = existing?.budget?.amount?.toString() ?: "",
                        isMonth = true,
                        month = currentMonth
                    )
                },
                onToggleAlert = { budget ->
                    viewModel.toggleAlert(budget.id, !budget.alertEnabled)
                },
                onSetThreshold = { thresholdDialog = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Category Budgets ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("分类预算")
                IconButton(onClick = { addCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加分类预算", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (categoryBudgets.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂无分类预算，点击右上角 + 添加",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                categoryBudgets.forEach { bws ->
                    val emoji = bws.budget.categoryName?.let { CATEGORY_EMOJI[it] } ?: "📋"
                    BudgetCard(
                        label = "$emoji ${bws.budget.categoryName ?: ""}",
                        bws = bws,
                        onClickEdit = { existing ->
                            editDialog = BudgetEditInfo(
                                budget = existing?.budget,
                                defaultAmount = existing?.budget?.amount?.toString() ?: "",
                                isCategory = true,
                                month = currentMonth,
                                categoryName = existing?.budget?.categoryName ?: ""
                            )
                        },
                        onToggleAlert = { budget ->
                            viewModel.toggleAlert(budget.id, !budget.alertEnabled)
                        },
                        onSetThreshold = { thresholdDialog = it },
                        showDelete = true,
                        onDelete = { deleteConfirm = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Edit / Create Budget Dialog ──
    editDialog?.let { info ->
        var amountText by remember { mutableStateOf(info.defaultAmount) }
        AlertDialog(
            onDismissRequest = { editDialog = null },
            shape = MaterialTheme.shapes.large,
            title = {
                val title = when {
                    info.isYear -> "设置年度预算"
                    info.isMonth -> "设置${info.month?.let { it + 1 }}月预算"
                    info.isCategory -> "设置${info.categoryName}预算"
                    else -> "设置预算"
                }
                Text(title, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amountText = it },
                    label = { Text("预算金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: return@Button
                        when {
                            info.isYear -> viewModel.setYearBudget(amt)
                            info.isMonth -> viewModel.setMonthBudget(info.month ?: currentMonth, amt)
                            info.isCategory -> {
                                val catName = info.categoryName ?: return@Button
                                viewModel.setCategoryBudget(info.month ?: currentMonth, catName, amt)
                            }
                        }
                        editDialog = null
                    },
                    enabled = amountText.isNotEmpty() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                    shape = MaterialTheme.shapes.medium
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editDialog = null }) { Text("取消") }
            }
        )
    }

    // ── Add Category Budget Dialog ──
    if (addCategoryDialog) {
        var selectedCat by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addCategoryDialog = false },
            shape = MaterialTheme.shapes.large,
            title = { Text("添加分类预算", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text("选择分类", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    val gridItems = expenseCategories + null
                    gridItems.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item ->
                                val cat = item
                                val isSel = cat != null && cat.name == selectedCat
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(2f)
                                        .clickable { if (cat != null) selectedCat = cat.name },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (cat != null) {
                                            Text(
                                                "${cat.icon} ${cat.name}",
                                                fontSize = 13.sp,
                                                color = if (isSel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amountText = it },
                        label = { Text("预算金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: return@Button
                        if (selectedCat.isNotEmpty()) {
                            viewModel.setCategoryBudget(currentMonth, selectedCat, amt)
                            addCategoryDialog = false
                        }
                    },
                    enabled = amountText.isNotEmpty() && selectedCat.isNotEmpty() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                    shape = MaterialTheme.shapes.medium
                ) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { addCategoryDialog = false }) { Text("取消") }
            }
        )
    }

    // ── Threshold Dialog ──
    thresholdDialog?.let { budget ->
        var sliderValue by remember { mutableStateOf(budget.alertThreshold) }
        AlertDialog(
            onDismissRequest = { thresholdDialog = null },
            shape = MaterialTheme.shapes.large,
            title = { Text("设置提醒阈值", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "消费占比达到 ${(sliderValue * 100).toInt()}% 时提醒",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(0.5f, 0.7f, 0.8f, 0.9f, 1.0f).forEach { preset ->
                            val isPreset = sliderValue == preset
                            Surface(
                                modifier = Modifier.clickable { sliderValue = preset },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPreset) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "${(preset * 100).toInt()}%",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = if (isPreset) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPreset) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0.5f..1.0f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAlertThreshold(budget.id, sliderValue)
                        thresholdDialog = null
                    },
                    shape = MaterialTheme.shapes.medium
                ) { Text("确定") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.toggleAlert(budget.id, false)
                        thresholdDialog = null
                    }) { Text("关闭提醒") }
                    TextButton(onClick = { thresholdDialog = null }) { Text("取消") }
                }
            }
        )
    }

    // ── Delete Confirm Dialog ──
    deleteConfirm?.let { budget ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            shape = MaterialTheme.shapes.large,
            title = { Text("删除分类预算") },
            text = { Text("确定删除「${budget.categoryName ?: ""}」的预算吗？", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBudget(budget.id)
                        deleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = MaterialTheme.shapes.medium
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun BudgetCard(
    label: String,
    bws: BudgetWithSpent?,
    onClickEdit: (BudgetWithSpent?) -> Unit,
    onToggleAlert: (Budget) -> Unit,
    onSetThreshold: (Budget) -> Unit,
    showDelete: Boolean = false,
    onDelete: ((Budget) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bws != null) {
                        // Alert toggle
                        IconButton(onClick = { onToggleAlert(bws.budget) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                if (bws.budget.alertEnabled) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                contentDescription = if (bws.budget.alertEnabled) "提醒已开启" else "提醒已关闭",
                                tint = if (bws.budget.alertEnabled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Threshold button
                        if (bws.budget.alertEnabled) {
                            TextButton(onClick = { onSetThreshold(bws.budget) }) {
                                Text(
                                    "${(bws.budget.alertThreshold * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            if (bws != null) {
                Spacer(modifier = Modifier.height(8.dp))
                // Amount line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "已花 ¥${"%.2f".format(bws.spent)} / 预算 ¥${"%.2f".format(bws.budget.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${"%.1f".format(bws.percentage * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (bws.percentage >= 1f) MaterialTheme.colorScheme.error
                        else if (bws.percentage >= 0.9f) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                val barColor = when {
                    bws.percentage >= 1f -> MaterialTheme.colorScheme.error
                    bws.percentage >= 0.9f -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.primary
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onClickEdit(bws) }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {}
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(bws.percentage.coerceIn(0f, 1f))
                            .fillMaxHeight(),
                        color = barColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {}
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text("未设置", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (showDelete && bws != null && onDelete != null) {
                    TextButton(onClick = { onDelete(bws.budget) }) {
                        Text("删除", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
                TextButton(onClick = { onClickEdit(bws) }) {
                    Text(if (bws != null) "编辑金额" else "点击设置", fontSize = 13.sp)
                }
            }
        }
    }
}

private data class BudgetEditInfo(
    val budget: Budget?,
    val defaultAmount: String,
    val isYear: Boolean = false,
    val isMonth: Boolean = false,
    val isCategory: Boolean = false,
    val month: Int? = null,
    val categoryName: String? = null
)
