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
                CategoryEntity(name = "餐饮", type = 0, sortOrder = 0),
                CategoryEntity(name = "购物", type = 0, sortOrder = 1),
                CategoryEntity(name = "日用", type = 0, sortOrder = 2),
                CategoryEntity(name = "美妆", type = 0, sortOrder = 3),
                CategoryEntity(name = "交通", type = 0, sortOrder = 4),
                CategoryEntity(name = "蔬菜", type = 0, sortOrder = 5),
                CategoryEntity(name = "水果", type = 0, sortOrder = 6),
                CategoryEntity(name = "零食", type = 0, sortOrder = 7),
                CategoryEntity(name = "通讯", type = 0, sortOrder = 8),
                CategoryEntity(name = "运动", type = 0, sortOrder = 9),
                CategoryEntity(name = "娱乐", type = 0, sortOrder = 10),
                CategoryEntity(name = "服饰", type = 0, sortOrder = 11),
                CategoryEntity(name = "住房", type = 0, sortOrder = 12),
                CategoryEntity(name = "宠物", type = 0, sortOrder = 13),
                CategoryEntity(name = "医疗", type = 0, sortOrder = 14),
                CategoryEntity(name = "数码", type = 0, sortOrder = 15),
                CategoryEntity(name = "学习", type = 0, sortOrder = 16),
                CategoryEntity(name = "摩托", type = 0, sortOrder = 17),
                CategoryEntity(name = "快递", type = 0, sortOrder = 18),
                CategoryEntity(name = "礼金", type = 0, sortOrder = 19),
                CategoryEntity(name = "书籍", type = 0, sortOrder = 20),
                CategoryEntity(name = "工资", type = 1, sortOrder = 0),
                CategoryEntity(name = "生活费", type = 1, sortOrder = 1),
                CategoryEntity(name = "理财", type = 1, sortOrder = 2),
                CategoryEntity(name = "礼金", type = 1, sortOrder = 3),
                CategoryEntity(name = "红包", type = 1, sortOrder = 4),
                CategoryEntity(name = "其他", type = 1, sortOrder = 5)
            )
            dao.insertAll(defaults)
        }
    }
}
