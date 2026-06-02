package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    viewModel: CategoryViewModel,
    onBack: () -> Unit
) {
    var currentType by remember { mutableIntStateOf(0) }
    val expenses by viewModel.expenseCategories.collectAsState(initial = emptyList())
    val incomes by viewModel.incomeCategories.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val categories = if (currentType == 0) expenses else incomes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("类别管理", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加类别", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Tab Row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Tab(
                    selected = currentType == 0,
                    onClick = { currentType = 0 },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "支出",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (currentType == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                }
                Tab(
                    selected = currentType == 1,
                    onClick = { currentType = 1 },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "收入",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (currentType == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                }
            }

            // ── Category List ──
            ReorderableCategoryList(
                items = categories,
                type = currentType,
                onDelete = { viewModel.deleteCategory(it) },
                onReorder = { from, to -> viewModel.reorder(currentType, from, to) }
            )
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = MaterialTheme.shapes.large,
            title = { Text("添加类别") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("类别名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCategory(name.trim(), currentType, categories.size)
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank(),
                    shape = MaterialTheme.shapes.medium
                ) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReorderableCategoryList(
    items: List<CategoryEntity>,
    type: Int,
    onDelete: (Long) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragAccumulated by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    val estimatedItemHeight = with(density) { 72.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, category ->
                val isDragged = index == draggedIndex
                val emoji = CATEGORY_EMOJI[category.name] ?: "📋"

                SwipeToDismissBox(
                    state = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onDelete(category.id)
                                true
                            } else false
                        }
                    ),
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.error)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text("删除", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragged) 2f else 0f)
                            .offset(y = if (isDragged) with(density) { dragAccumulated.toDp() } else 0.dp)
                            .shadow(
                                elevation = if (isDragged) 8.dp else 1.dp,
                                shape = MaterialTheme.shapes.medium
                            )
                            .pointerInput(index) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedIndex = index
                                        dragAccumulated = 0f
                                    },
                                    onDrag = { change, offset ->
                                        change.consume()
                                        dragAccumulated += offset.y

                                        val targetIdx =
                                            (index + (dragAccumulated / estimatedItemHeight).roundToInt())
                                                .coerceIn(0, items.size - 1)
                                        if (targetIdx != draggedIndex) {
                                            onReorder(draggedIndex, targetIdx)
                                            draggedIndex = targetIdx
                                            dragAccumulated = 0f
                                        }
                                    },
                                    onDragEnd = {
                                        draggedIndex = -1
                                        dragAccumulated = 0f
                                    },
                                    onDragCancel = {
                                        draggedIndex = -1
                                        dragAccumulated = 0f
                                    }
                                )
                            },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDragged)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.DragHandle,
                                contentDescription = "长按拖动排序",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
