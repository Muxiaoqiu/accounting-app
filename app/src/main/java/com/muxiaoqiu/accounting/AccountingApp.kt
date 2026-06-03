package com.muxiaoqiu.accounting

import android.app.Application
import com.muxiaoqiu.accounting.data.database.AppDatabase
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountingApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        initDefaultCategories()
    }

    private fun initDefaultCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.categoryDao()
            if (dao.count() > 0) return@launch

            val defaults = listOf(
                CategoryEntity(name = "餐饮", type = 0, sortOrder = 0, icon = "🍴"),
                CategoryEntity(name = "购物", type = 0, sortOrder = 1, icon = "🛒"),
                CategoryEntity(name = "日用", type = 0, sortOrder = 2, icon = "🧴"),
                CategoryEntity(name = "美妆", type = 0, sortOrder = 3, icon = "💄"),
                CategoryEntity(name = "交通", type = 0, sortOrder = 4, icon = "🚗"),
                CategoryEntity(name = "蔬菜", type = 0, sortOrder = 5, icon = "🥦"),
                CategoryEntity(name = "水果", type = 0, sortOrder = 6, icon = "🍎"),
                CategoryEntity(name = "零食", type = 0, sortOrder = 7, icon = "🍪"),
                CategoryEntity(name = "通讯", type = 0, sortOrder = 8, icon = "📱"),
                CategoryEntity(name = "运动", type = 0, sortOrder = 9, icon = "🏃"),
                CategoryEntity(name = "娱乐", type = 0, sortOrder = 10, icon = "🎬"),
                CategoryEntity(name = "服饰", type = 0, sortOrder = 11, icon = "👕"),
                CategoryEntity(name = "住房", type = 0, sortOrder = 12, icon = "🏠"),
                CategoryEntity(name = "宠物", type = 0, sortOrder = 13, icon = "🐶"),
                CategoryEntity(name = "医疗", type = 0, sortOrder = 14, icon = "💊"),
                CategoryEntity(name = "数码", type = 0, sortOrder = 15, icon = "💻"),
                CategoryEntity(name = "学习", type = 0, sortOrder = 16, icon = "📖"),
                CategoryEntity(name = "摩托", type = 0, sortOrder = 17, icon = "🏍"),
                CategoryEntity(name = "快递", type = 0, sortOrder = 18, icon = "📦"),
                CategoryEntity(name = "礼金", type = 0, sortOrder = 19, icon = "🎁"),
                CategoryEntity(name = "书籍", type = 0, sortOrder = 20, icon = "📚"),
                CategoryEntity(name = "工资", type = 1, sortOrder = 0, icon = "💰"),
                CategoryEntity(name = "生活费", type = 1, sortOrder = 1, icon = "💵"),
                CategoryEntity(name = "理财", type = 1, sortOrder = 2, icon = "📈"),
                CategoryEntity(name = "礼金", type = 1, sortOrder = 3, icon = "🧧"),
                CategoryEntity(name = "红包", type = 1, sortOrder = 4, icon = "🧧"),
                CategoryEntity(name = "其他", type = 1, sortOrder = 5, icon = "📋")
            )
            dao.insertAll(defaults)
        }
    }
}
