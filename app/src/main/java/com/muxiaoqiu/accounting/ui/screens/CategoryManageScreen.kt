package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI
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

@Composable
private fun ReorderableCategoryList(
    items: List<CategoryEntity>,
    type: Int,
    onDelete: (Long) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    // Track drag state
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val itemHeight = 64 // dp per item (approximate)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.dp.toPx() }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, category ->
            val emoji = CATEGORY_EMOJI[category.name] ?: "📋"
            val offsetY = if (index == draggedIndex) dragOffset else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (index == draggedIndex) 1f else 0f)
                    .then(
                        if (index == draggedIndex) Modifier.offset(y = with(density) { offsetY.toDp() })
                        else Modifier
                    )
                    .shadow(
                        elevation = if (index == draggedIndex) 8.dp else 0.dp,
                        shape = MaterialTheme.shapes.medium
                    ),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ── Delete Button ──
                    IconButton(
                        onClick = { onDelete(category.id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ── Emoji + Name ──
                    Text(text = emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // ── Drag Handle ──
                    Icon(
                        Icons.Default.DragHandle,
                        contentDescription = "长按拖动排序",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(32.dp)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggedIndex = index; dragOffset = 0f },
                                    onDrag = { change, offset ->
                                        change.consume()
                                        dragOffset += offset.y
                                        val targetIndex = (index + (dragOffset / itemHeightPx).roundToInt())
                                            .coerceIn(0, items.size - 1)
                                        if (targetIndex != draggedIndex) {
                                            onReorder(draggedIndex, targetIndex)
                                            draggedIndex = targetIndex
                                            dragOffset = 0f
                                        }
                                    },
                                    onDragEnd = {
                                        draggedIndex = -1
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggedIndex = -1
                                        dragOffset = 0f
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}
