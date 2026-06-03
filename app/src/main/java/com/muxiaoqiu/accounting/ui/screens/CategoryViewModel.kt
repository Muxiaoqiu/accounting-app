package com.muxiaoqiu.accounting.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muxiaoqiu.accounting.data.dao.CategoryDao
import com.muxiaoqiu.accounting.data.entity.CategoryEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val dao: CategoryDao) : ViewModel() {

    private val _expenseCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val expenseCategories: StateFlow<List<CategoryEntity>> = _expenseCategories

    private val _incomeCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val incomeCategories: StateFlow<List<CategoryEntity>> = _incomeCategories

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            dao.getByType(0).collect { _expenseCategories.value = it }
        }
        viewModelScope.launch {
            dao.getByType(1).collect { _incomeCategories.value = it }
        }
    }

    fun addCategory(name: String, type: Int) {
        viewModelScope.launch {
            dao.insert(CategoryEntity(name = name, type = type, sortOrder = Int.MAX_VALUE))
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            dao.delete(id)
        }
    }

    fun moveCategory(type: Int, from: Int, to: Int) {
        val flow = if (type == 0) _expenseCategories else _incomeCategories
        val list = flow.value.toMutableList()
        if (from < 0 || from >= list.size || to < 0 || to >= list.size) return

        val item = list.removeAt(from)
        list.add(to, item)
        flow.value = list

        // Cancel previous unfinished save, persist latest order
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            list.forEachIndexed { index, entity ->
                dao.updateSortOrder(entity.id, index)
            }
        }
    }
}
