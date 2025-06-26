package com.example.budgetiq.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )

    object Expenses : BottomNavItem(
        route = "expenses",
        title = "Expense",
        icon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet
    )
    
    object BudgetGoals : BottomNavItem(
        route = "budget_goals",
        title = "Budget",
        icon = Icons.Outlined.Savings,
        selectedIcon = Icons.Filled.Savings
    )
    
    object ExpenseList : BottomNavItem(
        route = "expense_list",
        title = "History",
        icon = Icons.Outlined.History,
        selectedIcon = Icons.Filled.History
    )
    
    object Categories : BottomNavItem(
        route = "categories",
        title = "Groups",
        icon = Icons.Outlined.Category,
        selectedIcon = Icons.Filled.Category
    )

    object Achievements : BottomNavItem(
        route = "achievements",
        title = "Achievements",
        icon = Icons.Outlined.Star,
        selectedIcon = Icons.Filled.Star
    )

    companion object {
        val items = listOf(Home, Expenses, BudgetGoals, ExpenseList, Categories, Achievements)
    }
} 