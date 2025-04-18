package com.example.taskit.data.repository

import com.example.taskit.data.local.dao.CategoryDao
import com.example.taskit.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun getCategoryById(categoryId: String): Category?
    fun getCategoryByIdFlow(categoryId: String): Flow<Category?>
    fun getAllCategoriesFlow(): Flow<List<Category>>
    fun searchCategoriesFlow(query: String): Flow<List<Category>>
    suspend fun getTaskCountForCategory(categoryId: String): Int
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    
    override suspend fun addCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }
    
    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    override suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    override fun getCategoryByIdFlow(categoryId: String): Flow<Category?> {
        return categoryDao.getCategoryByIdFlow(categoryId)
    }
    
    override fun getAllCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategoriesFlow()
    }
    
    override fun searchCategoriesFlow(query: String): Flow<List<Category>> {
        return categoryDao.searchCategoriesFlow(query)
    }
    
    override suspend fun getTaskCountForCategory(categoryId: String): Int {
        return categoryDao.getTaskCountForCategory(categoryId)
    }
} 