package com.muxiaoqiu.accounting.ui.theme

data class CategoryIconGroup(
    val name: String,
    val icons: List<String>
)

val CATEGORY_ICON_GROUPS = listOf(
    CategoryIconGroup("娱乐", listOf("🎮", "🎵", "🎬", "🎤", "🎯", "🎲", "🎸", "🎭", "🎪", "🎳")),
    CategoryIconGroup("饮食", listOf("🍔", "🍕", "🍜", "🍰", "☕", "🍺", "🥗", "🍳", "🍱", "🧋")),
    CategoryIconGroup("医疗", listOf("🏥", "💊", "💉", "🩺", "🩹", "🌡️", "🧬", "🩻", "💗", "🚑")),
    CategoryIconGroup("学习", listOf("📚", "📝", "🎓", "📖", "✏️", "📐", "🔬", "🖊️", "🏫", "📕")),
    CategoryIconGroup("交通", listOf("🚗", "🚌", "✈️", "🚲", "🚇", "🚕", "🛵", "🚢", "🚄", "🛴")),
    CategoryIconGroup("购物", listOf("🛒", "🛍️", "👗", "👠", "💄", "⌚", "👜", "🎁", "👒", "🪞")),
    CategoryIconGroup("生活", listOf("🏠", "🛏️", "🚿", "🧹", "🧺", "🌱", "🐱", "🔧", "🌈", "🧘")),
    CategoryIconGroup("个人", listOf("👤", "💪", "🏃", "🎯", "💇", "💅", "🧴", "👓", "🎒", "💍")),
    CategoryIconGroup("家居", listOf("🛋️", "🖼️", "🪴", "💡", "🗝️", "🪟", "🛁", "🕰️", "📺", "🧸")),
    CategoryIconGroup("收入", listOf("💰", "💵", "💸", "💳", "🏦", "📊", "💼", "🪙", "🧧", "💎")),
    CategoryIconGroup("办公", listOf("💼", "📊", "📋", "🖨️", "📁", "📎", "🖊️", "💻", "📞", "📠")),
    CategoryIconGroup("其他", listOf("📌", "🔖", "🏷️", "⭐", "❤️", "🔔", "📦", "🗂️", "🧩", "🎀")),
)
