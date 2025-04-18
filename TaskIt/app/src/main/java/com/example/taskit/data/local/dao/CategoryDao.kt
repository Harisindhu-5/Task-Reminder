package com.example.taskit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskit.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): Category?
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryByIdFlow(categoryId: String): Flow<Category?>
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<Category>>
    
    @Query("""
        SELECT * FROM categories 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchCategoriesFlow(query: String): Flow<List<Category>>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    suspend fun getTaskCountForCategory(categoryId: String): Int
} 