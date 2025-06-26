package com.example.budgetiq.data.repository

import android.graphics.Color
import com.example.budgetiq.data.dao.CategoryDao
import com.example.budgetiq.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    private val defaultCategoryColors = listOf(
        Color.parseColor("#FF6B6B"), // Red
        Color.parseColor("#4ECDC4"), // Teal
        Color.parseColor("#45B7D1"), // Blue
        Color.parseColor("#96CEB4"), // Green
        Color.parseColor("#FFEEAD"), // Yellow
        Color.parseColor("#D4A5A5"), // Pink
        Color.parseColor("#9B59B6"), // Purple
        Color.parseColor("#95A5A6")  // Gray
    )

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories()

    suspend fun createCategory(name: String, userId: Long, color: Int? = null): Result<Long> {
        return try {
            val categoryId = categoryDao.insertCategory(
                Category(
                    name = name,
                    userId = userId,
                    color = color
                )
            )
            Result.success(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCategoriesForUser(userId: Long): Flow<List<Category>> =
        categoryDao.getCategoriesForUser(userId)

    suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.deleteCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.updateCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategoryById(categoryId: Long): Result<Category> {
        return try {
            val category = categoryDao.getCategoryById(categoryId)
            if (category != null) {
                Result.success(category)
            } else {
                Result.failure(Exception("Category not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDefaultCategories(userId: Long) {
        val existing = getCategoriesForUser(userId).first()
        if (existing.isNotEmpty()) return
        val defaultCategories = listOf(
            "Food & Dining",
            "Transportation",
            "Shopping",
            "Bills & Utilities",
            "Entertainment",
            "Health & Fitness",
            "Travel",
            "Other"
        )

        defaultCategories.forEachIndexed { index, categoryName ->
            try {
                createCategory(
                    name = categoryName,
                    userId = userId,
                    color = defaultCategoryColors[index]
                )
            } catch (e: Exception) {
                // Log error but continue with other categories
                e.printStackTrace()
            }
        }
    }
} 