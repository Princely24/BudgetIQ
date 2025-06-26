package com.example.budgetiq.navigation

sealed class Screen(val route: String) {
    object GetStarted : Screen("get_started")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddExpense : Screen("add_expense")
    object ViewExpense : Screen("view_expense/{expenseId}") {
        fun createRoute(expenseId: Long) = "view_expense/$expenseId"
    }
    object ExpenseList : Screen("expense_list")
    object Categories : Screen("categories")
    object BudgetGoals : Screen("budget_goals")
    object Achievements : Screen("achievements")
} 