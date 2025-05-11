package com.example.budgetiq.di

import android.content.Context
import com.example.budgetiq.data.AppDatabase
import com.example.budgetiq.data.dao.BudgetGoalDao
import com.example.budgetiq.data.dao.CategoryDao
import com.example.budgetiq.data.dao.ExpenseDao
import com.example.budgetiq.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideBudgetGoalDao(database: AppDatabase): BudgetGoalDao {
        return database.budgetGoalDao()
    }
} 