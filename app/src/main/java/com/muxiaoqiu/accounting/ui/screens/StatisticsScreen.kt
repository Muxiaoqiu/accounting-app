package com.muxiaoqiu.accounting.ui.screens

import android.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    bookId: Long,
    viewModel: StatisticsViewModel,
    categoryViewModel: CategoryViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(bookId) {
        viewModel.setBook(bookId)
    }

    val trendData by viewModel.trendData.collectAsState(initial = emptyList())
    val pieData by viewModel.pieData.collectAsState(initial = emptyList())
    val trendPeriod by viewModel.trendPeriod.collectAsState(initial = TrendPeriod.MONTH)
    val pieType by viewModel.pieType.collectAsState(initial = 0)

    val expenseCategories by categoryViewModel.expenseCategories.collectAsState(initial = emptyList())
    val incomeCategories by categoryViewModel.incomeCategories.collectAsState(initial = emptyList())
    val categoryIconMap = remember(expenseCategories, incomeCategories) {
        (expenseCategories + incomeCategories).associate { it.name to it.icon }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("趋势", "分类")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计", style = MaterialTheme.typography.titleLarge) },
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
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TrendTab(trendPeriod = trendPeriod, data = trendData, onPeriodChange = { viewModel.setTrendPeriod(it) })
                1 -> CategoryTab(pieType = pieType, data = pieData, categoryIconMap = categoryIconMap, onTypeChange = { viewModel.setPieType(it) })
            }
        }
    }
}

@Composable
private fun TrendTab(trendPeriod: TrendPeriod, data: List<TrendPoint>, onPeriodChange: (TrendPeriod) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TrendPeriod.entries.forEach { period ->
                val label = when (period) {
                    TrendPeriod.WEEK -> "周"
                    TrendPeriod.MONTH -> "月"
                    TrendPeriod.YEAR -> "年"
                }
                val selected = period == trendPeriod
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onPeriodChange(period) },
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (data.isEmpty() || data.all { it.expense == 0.0 && it.income == 0.0 }) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            TrendChart(data = data, modifier = Modifier.fillMaxWidth().height(280.dp))
        }
    }
}

@Composable
private fun TrendChart(data: List<TrendPoint>, modifier: Modifier = Modifier) {
    val expenseColor = Color(0xFFE57373)
    val incomeColor = Color(0xFF81C784)
    val surplusColor = Color(0xFF64B5F6)

    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(color = expenseColor, label = "支出")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = incomeColor, label = "收入")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = surplusColor, label = "结余")
        }

        Spacer(modifier = Modifier.height(8.dp))

        val density = LocalDensity.current
        val labelPaint = remember(density) {
            Paint().apply {
                color = textColor.toArgb()
                textSize = with(density) { 10.sp.toPx() }
                textAlign = Paint.Align.CENTER
            }
        }
        val lineStroke = with(density) { 2.dp.toPx() }
        val dotRadius = with(density) { 3.dp.toPx() }

        val maxVal = remember(data) {
            val m = data.maxOf { max(it.expense, max(it.income, abs(it.surplus))) }
            if (m == 0.0) 1.0 else m
        }

        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            val leftPad = 48.dp.toPx()
            val rightPad = 16.dp.toPx()
            val topPad = 16.dp.toPx()
            val bottomPad = 28.dp.toPx()
            val chartW = w - leftPad - rightPad
            val chartH = h - topPad - bottomPad

            // Grid lines + Y labels
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPad + chartH * i / gridLines
                drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(leftPad, y), Offset(w - rightPad, y), strokeWidth = 1f)

                val label = String.format("%.0f", maxVal * (gridLines - i) / gridLines)
                drawContext.canvas.nativeCanvas.drawText(label, leftPad - 8.dp.toPx(), y + 4.dp.toPx(), labelPaint)
            }

            // Zero line
            val zeroY = topPad + chartH
            val stepX = chartW / (data.size - 1).coerceAtLeast(1)

            // X labels
            data.forEachIndexed { index, point ->
                if (point.label.isNotEmpty()) {
                    val x = leftPad + stepX * index
                    drawContext.canvas.nativeCanvas.drawText(point.label, x, h - 2.dp.toPx(), labelPaint)
                }
            }

            fun yPos(value: Double): Float {
                val ratio = (value / maxVal).toFloat().coerceIn(0f, 1f)
                return topPad + chartH * (1f - ratio)
            }

            fun drawLine(values: List<Double>, color: Color) {
                val path = Path()
                values.forEachIndexed { i, v ->
                    val x = leftPad + stepX * i
                    val y = yPos(v)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color, style = Stroke(width = lineStroke))

                // Dots
                values.forEachIndexed { i, v ->
                    val x = leftPad + stepX * i
                    val y = yPos(v)
                    drawCircle(color, dotRadius, Offset(x, y))
                }
            }

            drawLine(data.map { it.expense }, expenseColor)
            drawLine(data.map { it.income }, incomeColor)
            drawLine(data.map { it.surplus }, surplusColor)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color, radius = 5.dp.toPx())
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CategoryTab(
    pieType: Int,
    data: List<CategorySlice>,
    categoryIconMap: Map<String, String>,
    onTypeChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 4.dp).clickable { onTypeChange(0) },
                shape = MaterialTheme.shapes.medium,
                color = if (pieType == 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "支出",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = if (pieType == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (pieType == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier.padding(horizontal = 4.dp).clickable { onTypeChange(1) },
                shape = MaterialTheme.shapes.medium,
                color = if (pieType == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "收入",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = if (pieType == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (pieType == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (data.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val total = remember(data) { data.sumOf { it.amount } }
            val density = LocalDensity.current
            val labelPaint = remember(density) {
                Paint().apply {
                    color = Color.Black.copy(alpha = 0.7f).toArgb()
                    textSize = with(density) { 12.sp.toPx() }
                    textAlign = Paint.Align.CENTER
                }
            }

            // Pie chart
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val diameter = min(size.width, size.height) * 0.8f
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val centerX = topLeft.x + diameter / 2f
                val centerY = topLeft.y + diameter / 2f

                var startAngle = -90f
                data.forEach { slice ->
                    val sweepAngle = slice.percentage * 360f
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )
                        if (slice.percentage >= 0.05f) {
                            val midAngle = startAngle + sweepAngle / 2f
                            val rad = Math.toRadians(midAngle.toDouble())
                            val labelRadius = diameter * 0.35f
                            val lx = centerX + labelRadius * kotlin.math.cos(rad).toFloat()
                            val ly = centerY + labelRadius * kotlin.math.sin(rad).toFloat()
                            drawContext.canvas.nativeCanvas.drawText(
                                "${(slice.percentage * 100).toInt()}%",
                                lx, ly + 4.dp.toPx(), labelPaint
                            )
                        }
                    }
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Text(
                "合计: ¥ %.2f".format(total),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            data.forEach { slice ->
                val emoji = categoryIconMap[slice.name] ?: CATEGORY_EMOJI[slice.name] ?: "📋"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(slice.color, radius = 6.dp.toPx())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = slice.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥ %.2f".format(slice.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " ${(slice.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
