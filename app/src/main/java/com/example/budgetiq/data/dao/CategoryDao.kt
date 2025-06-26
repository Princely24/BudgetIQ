package com.example.budgetiq.data.dao

import androidx.room.*
import com.example.budgetiq.data.model.Category
import kotlinx.coroutines.flow.Flow


@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesForUser(userId: Long): Flow<List<Category>>

    @Delete
    suspend fun deleteCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?
} 