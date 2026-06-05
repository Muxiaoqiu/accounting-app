package com.muxiaoqiu.accounting.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand Colors ──
private val Green700 = Color(0xFF388E3C)
private val Green600 = Color(0xFF43A047)
private val Green50 = Color(0xFFE8F5E9)
private val Green100 = Color(0xFFC8E6C9)
private val Amber500 = Color(0xFFFF9800)
private val Red600 = Color(0xFFE53935)
private val Red50 = Color(0xFFFFEBEE)
private val Grey50 = Color(0xFFFAFAFA)
private val Grey100 = Color(0xFFF5F5F5)
private val Grey200 = Color(0xFFEEEEEE)
private val Grey800 = Color(0xFF424242)
private val Grey900 = Color(0xFF212121)
private val Grey600 = Color(0xFF757575)

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Amber500,
    onSecondary = Color.White,
    background = Grey50,
    onBackground = Grey900,
    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey600,
    outline = Grey200,
    error = Red600,
    errorContainer = Red50,
    onError = Color.White
)

val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

// ── Category Emoji Map ──
val CATEGORY_EMOJI = mapOf(
    "餐饮" to "🍴",
    "购物" to "🛒",
    "日用" to "🧴",
    "美妆" to "💄",
    "交通" to "🚗",
    "蔬菜" to "🥦",
    "水果" to "🍎",
    "零食" to "🍪",
    "通讯" to "📱",
    "运动" to "🏃",
    "娱乐" to "🎬",
    "服饰" to "👕",
    "住房" to "🏠",
    "宠物" to "🐶",
    "医疗" to "💊",
    "数码" to "💻",
    "学习" to "📖",
    "摩托" to "🏍",
    "快递" to "📦",
    "礼金" to "🎁",
    "书籍" to "📚",
    "工资" to "💰",
    "生活费" to "💵",
    "理财" to "📈",
    "红包" to "🧧",
    "其他" to "📋"
)

@Composable
fun AccountingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
