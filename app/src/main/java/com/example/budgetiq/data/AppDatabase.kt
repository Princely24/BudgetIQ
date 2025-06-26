package com.example.budgetiq.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetiq.data.dao.*
import com.example.budgetiq.data.model.*
import com.example.budgetiq.util.Converters

@Database(
    entities = [
        User::class,
        Category::class,
        Expense::class,
        BudgetGoal::class,
        Badge::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop the existing categories table
                database.execSQL("DROP TABLE IF EXISTS categories")
                
                // Recreate the categories table with the correct schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER,
                        icon TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                // Recreate the index
                database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_userId ON categories(userId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop the existing budget_goals table
                database.execSQL("DROP TABLE IF EXISTS budget_goals")
                
                // Create the new budget_goals table with the updated schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budget_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        categoryId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indices for foreign keys
                database.execSQL("CREATE INDEX IF NOT EXISTS index_budget_goals_userId ON budget_goals(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_budget_goals_categoryId ON budget_goals(categoryId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_iq_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 