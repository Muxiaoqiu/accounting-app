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
            _expenseCategories.value = dao.getByTypeList(0)
            _incomeCategories.value = dao.getByTypeList(1)
        }
    }

    fun addCategory(name: String, type: Int, icon: String) {
        viewModelScope.launch {
            val flow = if (type == 0) _expenseCategories else _incomeCategories
            dao.insert(CategoryEntity(name = name, type = type, sortOrder = flow.value.size, icon = icon))
            flow.value = dao.getByTypeList(type)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            // Need to figure out which type this category belongs to
            val expList = _expenseCategories.value
            val incList = _incomeCategories.value
            val deletedType = if (expList.any { it.id == id }) 0 else 1

            dao.delete(id)
            if (deletedType == 0) {
                _expenseCategories.value = dao.getByTypeList(0)
            } else {
                _incomeCategories.value = dao.getByTypeList(1)
            }
        }
    }

    fun moveCategory(type: Int, from: Int, to: Int) {
        val flow = if (type == 0) _expenseCategories else _incomeCategories
        val list = flow.value.toMutableList()
        if (from < 0 || from >= list.size || to < 0 || to >= list.size) return

        val item = list.removeAt(from)
        list.add(to, item)

        // Update StateFlow immediately — no DB middle-state interference
        flow.value = list

        // Cancel any in-progress save, write latest order
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            list.forEachIndexed { index, entity ->
                dao.updateSortOrder(entity.id, index)
            }
        }
    }
}
