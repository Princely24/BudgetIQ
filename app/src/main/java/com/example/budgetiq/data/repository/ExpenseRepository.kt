package com.example.budgetiq.data.repository

import com.example.budgetiq.data.dao.ExpenseDao
import com.example.budgetiq.data.model.CategoryTotal
import com.example.budgetiq.data.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun insertExpense(expense: Expense): Result<Long> {
        return try {
            val expenseId = expenseDao.insertExpense(expense)
            Result.success(expenseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExpense(
        userId: Long,
        categoryId: Long,
        amount: Double,
        description: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        photoPath: String? = null
    ): Result<Long> {
        return try {
            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                description = description,
                date = date,
                startTime = startTime,
                endTime = endTime,
                photoPath = photoPath
            )
            val expenseId = expenseDao.insertExpense(expense)
            Result.success(expenseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getExpensesForPeriod(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>> = expenseDao.getExpensesForPeriod(userId, startDate, endDate)

    fun getCategoryTotalsForPeriod(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CategoryTotal>> = expenseDao.getCategoryTotalsForPeriod(userId, startDate, endDate)

    suspend fun deleteExpense(expense: Expense): Result<Unit> {
        return try {
            expenseDao.deleteExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            expenseDao.updateExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenseById(expenseId: Long): Result<Expense> {
        return try {
            val expense = expenseDao.getExpenseById(expenseId)
            if (expense != null) {
                Result.success(expense)
            } else {
                Result.failure(Exception("Expense not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllExpenses(): Flow<List<Expense>> =
        expenseDao.getAllExpenses()

    fun getExpensesForUser(userId: Long): Flow<List<Expense>> =
        expenseDao.getExpensesForUser(userId)

    fun getTotalExpenseForCategory(categoryId: Long): Flow<Double> =
        expenseDao.getExpensesForCategory(categoryId).map { expenses ->
            expenses.sumOf { it.amount }
        }
} 