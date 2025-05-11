package com.example.budgetiq.data.dao

import androidx.room.*
import com.example.budgetiq.data.model.BudgetGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {
    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Query("SELECT * FROM budget_goals WHERE userId = :userId")
    fun getBudgetGoalsForUser(userId: Long): Flow<List<BudgetGoal>>

    @Query("SELECT * FROM budget_goals WHERE categoryId = :categoryId")
    fun getBudgetGoalForCategory(categoryId: Long): Flow<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE id = :goalId")
    suspend fun getBudgetGoalById(goalId: Long): BudgetGoal?

    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)
} 