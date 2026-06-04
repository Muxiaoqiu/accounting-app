package com.muxiaoqiu.accounting.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val year: Int,
    val month: Int? = null,          // null = 年度预算
    val categoryName: String? = null, // null = 总预算
    val amount: Double,
    val alertEnabled: Boolean = false,
    val alertThreshold: Float = 0.8f   // 0.0~1.0
)
