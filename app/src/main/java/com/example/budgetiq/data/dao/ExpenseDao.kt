package com.example.budgetiq.data.dao

import androidx.room.*
import com.example.budgetiq.data.model.CategoryTotal
import com.example.budgetiq.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesForPeriod(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate 
        GROUP BY categoryId
    """)
    fun getCategoryTotalsForPeriod(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Long): Expense?

    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getExpensesForUser(userId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    fun getExpensesForCategory(categoryId: Long): Flow<List<Expense>>
} 