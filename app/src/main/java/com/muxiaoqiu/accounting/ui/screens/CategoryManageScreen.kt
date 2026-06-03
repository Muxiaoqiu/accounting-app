package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Tab(selected = currentType == 0, onClick = { currentType = 0 }, modifier = Modifier.weight(1f)) {
                    Text("支出", modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (currentType == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
                }
                Tab(selected = currentType == 1, onClick = { currentType = 1 }, modifier = Modifier.weight(1f)) {
                    Text("收入", modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (currentType == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
                }
            }

            DraggableCategoryList(
                items = categories,
                type = currentType,
                onDelete = { viewModel.deleteCategory(it) },
                onMove = { from, to -> viewModel.moveCategory(currentType, from, to) }
            )
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false }, shape = MaterialTheme.shapes.large,
            title = { Text("添加类别") },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("类别名称") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCategory(name.trim(), currentType)
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank(), shape = MaterialTheme.shapes.medium
                ) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun DraggableCategoryList(
    items: List<CategoryEntity>,
    type: Int,
    onDelete: (Long) -> Unit,
    onMove: (Int, Int) -> Unit
) {
    var draggedIdx by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var confirmingId by remember { mutableLongStateOf(-1L) }
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 64.dp.toPx() }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, category ->
            val emoji = CATEGORY_EMOJI[category.name] ?: "📋"

            CategoryRow(
                emoji = emoji,
                name = category.name,
                isDragged = index == draggedIdx,
                isConfirming = confirmingId == category.id,
                dragOffset = if (index == draggedIdx) dragOffset else 0f,
                onDeleteClick = { confirmingId = category.id },
                onConfirmDelete = {
                    onDelete(category.id)
                    confirmingId = -1L
                },
                onDragStart = {
                    draggedIdx = index
                    dragOffset = 0f
                    confirmingId = -1L
                },
                onDrag = { offset ->
                    dragOffset += offset.y
                    val target = (draggedIdx + (dragOffset / itemHeightPx).roundToInt())
                        .coerceIn(0, items.size - 1)
                    if (target != draggedIdx) {
                        onMove(draggedIdx, target)
                        draggedIdx = target
                        dragOffset = 0f
                    }
                },
                onDragEnd = {
                    draggedIdx = -1
                    dragOffset = 0f
                },
                onDragCancel = {
                    draggedIdx = -1
                    dragOffset = 0f
                }
            )
        }
    }
}

@Composable
private fun CategoryRow(
    emoji: String,
    name: String,
    isDragged: Boolean,
    isConfirming: Boolean,
    dragOffset: Float,
    onDeleteClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val density = LocalDensity.current
    val contentOffsetX by animateDpAsState(if (isConfirming) (-72).dp else 0.dp, label = "slide")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragged) 2f else 0f)
            .offset(y = if (isDragged) with(density) { dragOffset.toDp() } else 0.dp)
            .shadow(elevation = if (isDragged) 8.dp else 1.dp, shape = MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isDragged) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.Delete, contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                modifier = Modifier.weight(1f).offset(x = contentOffsetX),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDrag = { change, offset ->
                                    change.consume()
                                    onDrag(offset)
                                },
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DragHandle, contentDescription = "长按拖动排序",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isConfirming) {
                Box(
                    modifier = Modifier
                        .width(72.dp).fillMaxHeight()
                        .background(MaterialTheme.colorScheme.error)
                        .clickable { onConfirmDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("删除", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
