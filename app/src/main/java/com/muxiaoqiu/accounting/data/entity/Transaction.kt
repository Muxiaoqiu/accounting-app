package com.muxiaoqiu.accounting.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String = "",
    val type: Int,          // 0=支出 1=收入
    val timestamp: Long = System.currentTimeMillis()
)
