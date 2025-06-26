package com.example.budgetiq.di

import android.content.Context
import androidx.room.Room
import com.example.budgetiq.data.AppDatabase
import com.example.budgetiq.data.dao.BudgetGoalDao
import com.example.budgetiq.data.dao.CategoryDao
import com.example.budgetiq.data.dao.ExpenseDao
import com.example.budgetiq.data.dao.UserDao
import com.example.budgetiq.data.dao.BadgeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budgetiq_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideBudgetGoalDao(database: AppDatabase): BudgetGoalDao {
        return database.budgetGoalDao()
    }

    @Provides
    @Singleton
    fun provideBadgeDao(database: AppDatabase): BadgeDao {
        return database.badgeDao()
    }
} 