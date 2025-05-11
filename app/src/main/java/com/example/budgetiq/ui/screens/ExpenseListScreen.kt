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
                                    // Keep the dialog open for end date selection
                                } else {
                                    selectedEndDate = selectedDate
                                    // Validate date range
                                    selectedStartDate?.let { startDate ->
                                        if (selectedDate.isBefore(startDate)) {
                                            // Swap dates if end date is before start date
                                            val temp = selectedStartDate
                                            selectedStartDate = selectedDate
                                            selectedEndDate = temp
                                        }
                                        viewModel.setCustomDateRange(
                                            selectedStartDate!!,
                                            selectedEndDate!!
                                        )
                                    }
                                    showDatePicker = false
                                }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (isSelectingStartDate) {
                                showDatePicker = false
                                selectedStartDate = null
                                selectedEndDate = null
                            } else {
                                isSelectingStartDate = true
                                selectedEndDate = null
                            }
                        }
                    ) {
                        Text(if (isSelectingStartDate) "Cancel" else "Back")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = { Text(if (isSelectingStartDate) "Select Start Date" else "Select End Date") },
                    headline = { 
                        if (!isSelectingStartDate && selectedStartDate != null) {
                            Text(
                                "Start Date: ${selectedStartDate?.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        when (val state = uiState) {
            is ExpenseListViewModel.UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Period summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = when (state.selectedPeriod) {
                                    TimePeriod.WEEK -> "This Week"
                                    TimePeriod.MONTH -> "This Month"
                                    TimePeriod.YEAR -> "This Year"
                                    TimePeriod.CUSTOM -> "Custom Range"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${state.startDate.format(dateFormatter)} - ${state.endDate.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Total: ${currencyFormatter.format(state.totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    // Category totals
                    CategoryTotalsList(
                        categoryTotals = state.categoryTotals,
                        categories = state.categories,
                        totalAmount = state.totalAmount,
                        currencyFormatter = currencyFormatter
                    )

                    if (state.expenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No expenses for this period")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.expenses) { expense ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToViewExpense(expense.id) }
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
                                            Text(
                                                text = currencyFormatter.format(expense.amount),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = expense.date.format(dateFormatter),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.categories.find { it.id == expense.categoryId }?.name ?: "Unknown Category",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (expense.description.isNotBlank()) {
                                            Text(
                                                text = expense.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is ExpenseListViewModel.UiState.Error -> {
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
            ExpenseListViewModel.UiState.Loading -> {
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