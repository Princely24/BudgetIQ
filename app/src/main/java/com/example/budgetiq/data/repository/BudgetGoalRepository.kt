package com.example.budgetiq.data.repository

import com.example.budgetiq.data.dao.BudgetGoalDao
import com.example.budgetiq.data.model.BudgetGoal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetGoalRepository @Inject constructor(
    private val budgetGoalDao: BudgetGoalDao
) {
    fun getBudgetGoalsForUser(userId: Long): Flow<List<BudgetGoal>> =
        budgetGoalDao.getBudgetGoalsForUser(userId)

    fun getBudgetGoalForCategory(categoryId: Long): Flow<BudgetGoal?> =
        budgetGoalDao.getBudgetGoalForCategory(categoryId)

    suspend fun createBudgetGoal(minAmount: Double, maxAmount: Double, categoryId: Long, userId: Long): Result<Long> {
        return try {
            require(maxAmount >= minAmount) { "Maximum amount must be greater than or equal to minimum amount" }
            val goalId = budgetGoalDao.insertBudgetGoal(
                BudgetGoal(
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    categoryId = categoryId,
                    userId = userId
                )
            )
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBudgetGoalById(goalId: Long): Result<BudgetGoal> {
        return try {
            val goal = budgetGoalDao.getBudgetGoalById(goalId)
            if (goal != null) {
                Result.success(goal)
            } else {
                Result.failure(Exception("Budget goal not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal): Result<Unit> {
        return try {
            require(budgetGoal.maxAmount >= budgetGoal.minAmount) { "Maximum amount must be greater than or equal to minimum amount" }
            budgetGoalDao.updateBudgetGoal(budgetGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal): Result<Unit> {
        return try {
            budgetGoalDao.deleteBudgetGoal(budgetGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 