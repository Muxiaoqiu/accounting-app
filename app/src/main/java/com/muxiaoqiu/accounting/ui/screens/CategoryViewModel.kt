package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.CategoryDao
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val dao: CategoryDao) : ViewModel() {

    private val _currentType = MutableStateFlow(0)

    val expenseCategories = dao.getByType(0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeCategories = dao.getByType(1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setType(type: Int) {
        _currentType.value = type
    }

    fun addCategory(name: String, type: Int, afterMaxOrder: Int) {
        viewModelScope.launch {
            dao.insert(CategoryEntity(name = name, type = type, sortOrder = afterMaxOrder))
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            dao.delete(id)
        }
    }

    fun saveOrder(categories: List<CategoryEntity>) {
        viewModelScope.launch {
            categories.forEachIndexed { index, entity ->
                dao.updateSortOrder(entity.id, index)
            }
        }
    }
}
