package com.muxiaoqiu.accounting.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.ui.theme.CATEGORY_EMOJI
import kotlin.math.abs
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Period selector (affects both charts) ──
            PeriodSelector(
                selected = trendPeriod,
                onSelect = { viewModel.setTrendPeriod(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Trend chart ──
            SectionTitle("趋势")
            Spacer(modifier = Modifier.height(8.dp))

            if (trendData.isEmpty() || trendData.all { it.expense == 0.0 && it.income == 0.0 }) {
                EmptyChart()
            } else {
                TrendChart(data = trendData, modifier = Modifier.fillMaxWidth().height(260.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // ── Pie chart ──
            SectionTitle("分类占比")
            Spacer(modifier = Modifier.height(12.dp))

            // Type toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = pieType == 0,
                    onClick = { viewModel.setPieType(0) },
                    label = { Text("支出") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.error
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = pieType == 1,
                    onClick = { viewModel.setPieType(1) },
                    label = { Text("收入") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pieData.isEmpty()) {
                EmptyChart()
            } else {
                PieChart(pieData, modifier = Modifier.fillMaxWidth().height(200.dp))

                Spacer(modifier = Modifier.height(12.dp))

                val total = pieData.sumOf { it.amount }
                Text(
                    "合计: ¥ %.2f".format(total),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                pieData.forEach { slice ->
                    CategoryRow(slice, categoryIconMap)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
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
private fun EmptyChart() {
    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
        Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Composable
private fun PeriodSelector(selected: TrendPeriod, onSelect: (TrendPeriod) -> Unit) {
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
            FilterChip(
                selected = period == selected,
                onClick = { onSelect(period) },
                label = { Text(label, fontWeight = if (period == selected) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendDot(color = expenseColor, label = "支出")
            Spacer(modifier = Modifier.width(20.dp))
            LegendDot(color = incomeColor, label = "收入")
            Spacer(modifier = Modifier.width(20.dp))
            LegendDot(color = surplusColor, label = "结余")
        }

        Spacer(modifier = Modifier.height(4.dp))

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
            val rightPad = 12.dp.toPx()
            val topPad = 12.dp.toPx()
            val bottomPad = 24.dp.toPx()
            val chartW = w - leftPad - rightPad
            val chartH = h - topPad - bottomPad

            for (i in 0..4) {
                val y = topPad + chartH * i / 4
                drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(leftPad, y), Offset(w - rightPad, y), strokeWidth = 1f)
                val label = String.format("%.0f", maxVal * (4 - i) / 4)
                drawContext.canvas.nativeCanvas.drawText(label, leftPad - 6.dp.toPx(), y + 4.dp.toPx(), labelPaint)
            }

            val stepX = chartW / (data.size - 1).coerceAtLeast(1)
            data.forEachIndexed { index, point ->
                if (point.label.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        point.label,
                        leftPad + stepX * index,
                        h - 2.dp.toPx(),
                        labelPaint
                    )
                }
            }

            fun yPos(value: Double): Float {
                val ratio = (value / maxVal).toFloat().coerceIn(0f, 1f)
                return topPad + chartH * (1f - ratio)
            }

            fun drawDataLine(values: List<Double>, color: Color) {
                val path = Path()
                values.forEachIndexed { i, v ->
                    val x = leftPad + stepX * i
                    val y = yPos(v)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color, style = Stroke(width = lineStroke))
                values.forEachIndexed { i, v ->
                    drawCircle(color, dotRadius, Offset(leftPad + stepX * i, yPos(v)))
                }
            }

            drawDataLine(data.map { it.expense }, expenseColor)
            drawDataLine(data.map { it.income }, incomeColor)
            drawDataLine(data.map { it.surplus }, surplusColor)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color, radius = 5.dp.toPx())
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PieChart(data: List<CategorySlice>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val labelPaint = remember(density) {
        Paint().apply {
            color = Color.White.toArgb()
            textSize = with(density) { 11.sp.toPx() }
            textAlign = Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier) {
        val diameter = min(size.width, size.height) * 0.85f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val cx = topLeft.x + diameter / 2f
        val cy = topLeft.y + diameter / 2f

        var startAngle = -90f
        data.forEach { slice ->
            val sweep = slice.percentage * 360f
            if (sweep > 0f) {
                drawArc(slice.color, startAngle, sweep, true, topLeft, arcSize)
                if (slice.percentage >= 0.05f) {
                    val mid = startAngle + sweep / 2f
                    val rad = Math.toRadians(mid.toDouble())
                    val lr = diameter * 0.32f
                    val lx = cx + lr * kotlin.math.cos(rad).toFloat()
                    val ly = cy + lr * kotlin.math.sin(rad).toFloat()
                    drawContext.canvas.nativeCanvas.drawText(
                        "${(slice.percentage * 100).toInt()}%", lx, ly + 4.dp.toPx(), labelPaint
                    )
                }
            }
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryRow(slice: CategorySlice, iconMap: Map<String, String>) {
    val emoji = iconMap[slice.name] ?: CATEGORY_EMOJI[slice.name] ?: "📋"
    val pctText = "${(slice.percentage * 100).toInt()}%"

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Amount on the left
            Text(
                "¥ %.2f".format(slice.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                slice.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                pctText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(slice.percentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(slice.color)
            )
        }
    }
}
