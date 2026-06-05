package com.muxiaoqiu.accounting.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Blue & Pink Palette ──
private val Blue500 = Color(0xFF6BA3E0)
private val Blue600 = Color(0xFF5C9CE6)
private val Blue50 = Color(0xFFF0F4FA)
private val Blue100 = Color(0xFFE4EFFB)
private val Pink400 = Color(0xFFE8788A)
private val Pink300 = Color(0xFFF08C99)
private val Pink50 = Color(0xFFFDE8ED)
private val Pink100 = Color(0xFFFDE4E8)
private val Grey50 = Color(0xFFF5F7FA)
private val Grey100 = Color(0xFFE8EDF3)
private val Grey200 = Color(0xFFD0D8E0)
private val Grey800 = Color(0xFF2C3E50)
private val Grey900 = Color(0xFF1A2530)
private val Grey600 = Color(0xFF6B7B8D)

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Color(0xFF1A3A5C),
    secondary = Pink300,
    onSecondary = Color.White,
    secondaryContainer = Pink100,
    onSecondaryContainer = Color(0xFF5C2028),
    background = Blue50,
    onBackground = Grey800,
    surface = Color.White,
    onSurface = Grey800,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey600,
    outline = Grey200,
    error = Pink400,
    errorContainer = Pink100,
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
