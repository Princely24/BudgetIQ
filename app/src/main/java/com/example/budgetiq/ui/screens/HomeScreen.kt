package com.example.budgetiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseList: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgetGoals: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BudgetIQ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ElevatedCard(
                onClick = onNavigateToAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Add Expense") },
                    supportingContent = { Text("Record a new expense") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) }
                )
            }

            ElevatedCard(
                onClick = onNavigateToExpenseList,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("View Expenses") },
                    supportingContent = { Text("See all your expenses") },
                    leadingContent = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }

            ElevatedCard(
                onClick = onNavigateToCategories,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Categories") },
                    supportingContent = { Text("Manage expense categories") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }

            ElevatedCard(
                onClick = onNavigateToBudgetGoals,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Budget Goals") },
                    supportingContent = { Text("Set and track your budget goals") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }
        }
    }
} 