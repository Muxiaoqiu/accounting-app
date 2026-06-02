package com.muxiaoqiu.accounting.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: Int,          // 0=支出 1=收入
    val sortOrder: Int = 0
)
