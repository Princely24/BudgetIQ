package com.example.budgetiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetiq.ui.viewmodels.BudgetGoalsViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetGoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetGoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<BudgetGoalUiState?>(null) }
    var newMinAmount by remember { mutableStateOf("") }
    var newMaxAmount by remember { mutableStateOf("") }
    var newGoalCategory by remember { mutableStateOf<Long?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.loadBudgetGoals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget Goal")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    newMinAmount = ""
                    newMaxAmount = ""
                    newGoalCategory = null
                    isDropdownExpanded = false
                },
                title = { Text("Add Budget Goal") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newMinAmount,
                            onValueChange = { newMinAmount = it },
                            label = { Text("Minimum Monthly Budget") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newMaxAmount,
                            onValueChange = { newMaxAmount = it },
                            label = { Text("Maximum Monthly Budget") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (viewModel.categories.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = isDropdownExpanded,
                                onExpandedChange = { isDropdownExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = viewModel.categories.find { it.id == newGoalCategory }?.name ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                ) {
                                    viewModel.categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = { 
                                                newGoalCategory = category.id
                                                isDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val minAmount = newMinAmount.toDoubleOrNull()
                            val maxAmount = newMaxAmount.toDoubleOrNull()
                            if (minAmount != null && maxAmount != null && newGoalCategory != null) {
                                viewModel.addBudgetGoal(minAmount, maxAmount, newGoalCategory!!)
                                showAddDialog = false
                                newMinAmount = ""
                                newMaxAmount = ""
                                newGoalCategory = null
                                isDropdownExpanded = false
                            }
                        },
                        enabled = newMinAmount.toDoubleOrNull() != null && 
                                 newMaxAmount.toDoubleOrNull() != null && 
                                 newGoalCategory != null &&
                                 (newMaxAmount.toDoubleOrNull() ?: 0.0) >= (newMinAmount.toDoubleOrNull() ?: 0.0)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            newMinAmount = ""
                            newMaxAmount = ""
                            newGoalCategory = null
                            isDropdownExpanded = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditDialog && selectedGoal != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    selectedGoal = null
                    newMinAmount = ""
                    newMaxAmount = ""
                },
                title = { Text("Edit Budget Goal") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newMinAmount,
                            onValueChange = { newMinAmount = it },
                            label = { Text("Minimum Monthly Budget") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newMaxAmount,
                            onValueChange = { newMaxAmount = it },
                            label = { Text("Maximum Monthly Budget") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val minAmount = newMinAmount.toDoubleOrNull()
                            val maxAmount = newMaxAmount.toDoubleOrNull()
                            if (minAmount != null && maxAmount != null) {
                                selectedGoal?.let { goal ->
                                    viewModel.updateBudgetGoal(goal.id, minAmount, maxAmount)
                                }
                                showEditDialog = false
                                selectedGoal = null
                                newMinAmount = ""
                                newMaxAmount = ""
                            }
                        },
                        enabled = newMinAmount.toDoubleOrNull() != null && 
                                 newMaxAmount.toDoubleOrNull() != null &&
                                 (newMaxAmount.toDoubleOrNull() ?: 0.0) >= (newMinAmount.toDoubleOrNull() ?: 0.0)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            selectedGoal = null
                            newMinAmount = ""
                            newMaxAmount = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteDialog && selectedGoal != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    selectedGoal = null
                },
                title = { Text("Delete Budget Goal") },
                text = { Text("Are you sure you want to delete this budget goal?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedGoal?.let { goal ->
                                viewModel.deleteBudgetGoal(goal.id)
                            }
                            showDeleteDialog = false
                            selectedGoal = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            selectedGoal = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        when (val state = uiState) {
            is BudgetGoalsViewModel.UiState.Success -> {
                if (state.budgetGoals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No budget goals set.\nTap + to add one.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.budgetGoals) { goal ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = goal.categoryName,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Min: ${currencyFormatter.format(goal.minAmount)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Max: ${currencyFormatter.format(goal.maxAmount)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Spent: ${currencyFormatter.format(goal.spent)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = when {
                                                    goal.spent < goal.minAmount -> MaterialTheme.colorScheme.error
                                                    goal.spent > goal.maxAmount -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    selectedGoal = goal
                                                    newMinAmount = goal.minAmount.toString()
                                                    newMaxAmount = goal.maxAmount.toString()
                                                    showEditDialog = true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Budget Goal"
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    selectedGoal = goal
                                                    showDeleteDialog = true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Budget Goal"
                                                )
                                            }
                                        }
                                    }
                                    LinearProgressIndicator(
                                        progress = (goal.spent / goal.maxAmount).toFloat().coerceIn(0f, 1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        color = when {
                                            goal.spent < goal.minAmount -> MaterialTheme.colorScheme.error
                                            goal.spent > goal.maxAmount -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is BudgetGoalsViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            BudgetGoalsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

data class BudgetGoalUiState(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val minAmount: Double,
    val maxAmount: Double,
    val spent: Double
) 