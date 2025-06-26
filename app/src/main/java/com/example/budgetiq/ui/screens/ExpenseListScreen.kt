package com.example.budgetiq.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetiq.data.model.Category
import com.example.budgetiq.data.model.CategoryTotal
import com.example.budgetiq.ui.viewmodels.ExpenseListViewModel
import com.example.budgetiq.ui.viewmodels.TimePeriod
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CategoryTotalsList(
    categoryTotals: List<CategoryTotal>,
    categories: List<Category>,
    totalAmount: Double,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            categoryTotals.forEach { categoryTotal ->
                val category = categories.find { it.id == categoryTotal.categoryId }
                val percentage = (categoryTotal.total / totalAmount * 100).toInt()
                val color = category?.color?.let { Color(it) } ?: Color.Gray

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category?.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = currencyFormatter.format(categoryTotal.total),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToViewExpense: (Long) -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showPeriodMenu by remember { mutableStateOf(false) }

    // Refresh data when the screen becomes active
    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = uiState !is ExpenseListViewModel.UiState.Loading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    // Period selection menu
                    Box {
                        IconButton(onClick = { showPeriodMenu = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Period")
                        }
                        DropdownMenu(
                            expanded = showPeriodMenu,
                            onDismissRequest = { showPeriodMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("This Week") },
                                onClick = {
                                    viewModel.setTimePeriod(TimePeriod.WEEK)
                                    showPeriodMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("This Month") },
                                onClick = {
                                    viewModel.setTimePeriod(TimePeriod.MONTH)
                                    showPeriodMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("This Year") },
                                onClick = {
                                    viewModel.setTimePeriod(TimePeriod.YEAR)
                                    showPeriodMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Custom Range") },
                                onClick = {
                                    isSelectingStartDate = true
                                    selectedStartDate = null
                                    selectedEndDate = null
                                    showDatePicker = true
                                    showPeriodMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ExpenseListViewModel.UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Summary card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Total Spending",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = currencyFormatter.format(state.totalAmount),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${state.startDate.format(dateFormatter)} - ${state.endDate.format(dateFormatter)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Category breakdown
                    if (state.categoryTotals.isNotEmpty()) {
                        item {
                            CategoryTotalsList(
                                categoryTotals = state.categoryTotals,
                                categories = state.categories,
                                totalAmount = state.totalAmount,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }

                    // Expenses list
                    items(state.expenses) { expense ->
                        val category = state.categories.find { it.id == expense.categoryId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToViewExpense(expense.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.description,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = category?.name ?: "Unknown Category",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = expense.date.format(dateFormatter),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = currencyFormatter.format(expense.amount),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                    }
                }
            }
            is ExpenseListViewModel.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ExpenseListViewModel.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis()
            )
            
            DatePickerDialog(
                onDismissRequest = { 
                    showDatePicker = false
                    if (isSelectingStartDate) {
                        selectedStartDate = null
                        selectedEndDate = null
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = LocalDate.ofInstant(
                                    java.time.Instant.ofEpochMilli(millis),
                                    ZoneId.systemDefault()
                                )
                                
                                if (isSelectingStartDate) {
                                    selectedStartDate = selectedDate
                                    isSelectingStartDate = false
                                    showDatePicker = true
                                } else {
                                    selectedEndDate = selectedDate
                                    showDatePicker = false
                                    
                                    if (selectedStartDate != null && selectedEndDate != null) {
                                        viewModel.setCustomDateRange(selectedStartDate!!, selectedEndDate!!)
                                    }
                                }
                            }
                        }
                    ) {
                        Text(if (isSelectingStartDate) "Next" else "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDatePicker = false
                            if (isSelectingStartDate) {
                                selectedStartDate = null
                                selectedEndDate = null
                            }
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        Text(
                            text = if (isSelectingStartDate) "Select Start Date" else "Select End Date"
                        )
                    }
                )
            }
        }
    }
} 