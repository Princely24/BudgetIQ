package com.example.budgetiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.budgetiq.navigation.BottomNavItem
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetiq.ui.viewmodels.ExpenseListViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import com.example.budgetiq.ui.viewmodels.BudgetGoalsViewModel
import com.example.budgetiq.data.model.Badge
import androidx.compose.material.icons.filled.Star
import com.example.budgetiq.ui.screens.AchievementsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseList: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgetGoals: () -> Unit
) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = BottomNavItem.items

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BudgetIQ") },
                actions = {
                    IconButton(onClick = onNavigateToAddExpense) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == index) item.selectedIcon else item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { 
                            Text(
                                text = item.title,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (item) {
                                is BottomNavItem.Home -> { /* Already on home screen */ }
                                is BottomNavItem.Expenses -> { /* Already on expenses screen */ }
                                is BottomNavItem.BudgetGoals -> {}
                                is BottomNavItem.ExpenseList -> {} /*onNavigateToExpenseList()*/
                                is BottomNavItem.Categories -> onNavigateToCategories()
                                is BottomNavItem.Achievements -> { /* Handled below */ }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> HomeContent(
                    modifier = Modifier.padding(16.dp),
                    onNavigateToAddExpense = onNavigateToAddExpense,
                    onNavigateToExpenseList = onNavigateToExpenseList
                )
                1 -> ExpensesContent(modifier = Modifier.padding(16.dp), onNavigateToAddExpense = onNavigateToAddExpense)
                2 -> BudgetGoalsContent(modifier = Modifier.padding(16.dp), onNavigateToBudgetGoals = onNavigateToBudgetGoals)
                3 -> ExpenseListContent(
                    modifier = Modifier.padding(16.dp),
                    onNavigateToExpenseList = onNavigateToExpenseList
                )
                4 -> CategoriesContent(modifier = Modifier.padding(16.dp))
                5 -> AchievementsScreen()
            }
        }
    }
}

private val categoryColorMap = mapOf(
    "Transportation" to Color(0xFF1976D2), // Blue
    "Food & Dining" to Color(0xFF388E3C), // Green
    "Bills & Utilities" to Color(0xFFFBC02D), // Yellow
    "Entertainment" to Color(0xFFD32F2F), // Red
    "Shopping" to Color(0xFF7B1FA2), // Purple
    "Other" to Color(0xFF0288D1) // Cyan
)
private val fallbackColors = listOf(
    Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFFBC02D),
    Color(0xFFD32F2F), Color(0xFF7B1FA2), Color(0xFF0288D1)
)

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    viewModel: ExpenseListViewModel = hiltViewModel(),
    budgetGoalsViewModel: BudgetGoalsViewModel = hiltViewModel(),
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToExpenseList: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val totalBudget by budgetGoalsViewModel.totalBudget.collectAsState()
    val recentCategories by budgetGoalsViewModel.recentCategories.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply { currency = java.util.Currency.getInstance("ZAR") } }

    // Refresh data when the screen becomes active
    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Badges Section
        if (badges.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Achievements",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        badges.forEach { badge ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = badge.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome to BudgetIQ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            IconButton(
                onClick = { viewModel.refresh() },
                enabled = uiState !is ExpenseListViewModel.UiState.Loading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (val state = uiState) {
            is ExpenseListViewModel.UiState.Success -> {
                // Spending Summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                // Update title to reflect selected period
                                when (state.selectedPeriod) {
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.WEEK -> "This Week's Spending"
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.MONTH -> "This Month's Spending"
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.YEAR -> "This Year's Spending"
                                    else -> "Spending"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currencyFormatter.format(state.totalAmount),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val budget = if (totalBudget > 0.0) totalBudget else 10000.0
                        val progress = (state.totalAmount / budget).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Spent: R${"%.2f".format(state.totalAmount)} / R${"%.2f".format(budget)}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Top Categories
                val topCategories: List<Pair<String, Double>> = state.categoryTotals.take(5).map { ct ->
                    val category = state.categories.find { it.id == ct.categoryId }
                    (category?.name ?: "Other") to ct.total
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                // Update title to reflect selected period
                                when (state.selectedPeriod) {
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.WEEK -> "Top Categories (Week)"
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.MONTH -> "Top Categories (Month)"
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.YEAR -> "Top Categories (Year)"
                                    else -> "Top Categories"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryBarChart(
                            categoryTotals = topCategories,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Spending by Category Pie Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                // Update title to reflect selected period
                                when (state.selectedPeriod) {
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.WEEK -> "Spending by Category (Week)"
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.MONTH -> "Spending by Category (Month)"
                                    com.example.budgetiq.ui.viewmodels.TimePeriod.YEAR -> "Spending by Category (Year)"
                                    else -> "Spending by Category"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        // --- Time Period Selector (now here, just above the pie chart) ---
                        val selectedPeriod = state.selectedPeriod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            com.example.budgetiq.ui.viewmodels.TimePeriod.values().filter { it != com.example.budgetiq.ui.viewmodels.TimePeriod.CUSTOM }.forEach { period ->
                                FilterChip(
                                    selected = selectedPeriod == period,
                                    onClick = { viewModel.setTimePeriod(period) },
                                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryPieChart(
                            categoryTotals = topCategories,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Recent Expenses
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Recent Expenses",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            state.expenses.take(3).forEach { expense ->
                                val category = state.categories.find { it.id == expense.categoryId }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.medium
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                expense.description,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                            )
                                            Text(
                                                category?.name ?: "Unknown",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = "-R${"%.2f".format(expense.amount)}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                                if (expense != state.expenses.take(3).last()) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
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
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Categories Overview Section (moved from BudgetGoalsContent)
        if (recentCategories.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Categories Overview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        recentCategories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onNavigateToAddExpense,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Expense")
            }
            Button(
                onClick = onNavigateToExpenseList,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("View All")
            }
        }
    }
}

@Composable
private fun ExpensesContent(
    modifier: Modifier = Modifier,
    viewModel: ExpenseListViewModel = hiltViewModel(),
    onNavigateToAddExpense: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply { currency = java.util.Currency.getInstance("ZAR") } }
    val headerColor = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(0.dp)) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerColor)
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Expenses Overview",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    }
                    IconButton(
                        onClick = onNavigateToAddExpense,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Divider(color = headerColor.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                when (val state = uiState) {
                    is ExpenseListViewModel.UiState.Success -> {
                        val total = state.totalAmount
                        val count = state.expenses.size
                        val avg = if (count > 0) total / count else 0.0
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent
                            )
                            Text(
                                text = "R${"%.2f".format(total)}",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = headerColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "$count expenses this month",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Expense Statistics in Vertical Layout
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Average Expense
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.medium
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Calculate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Average Expense",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = accent
                                            )
                                            Text(
                                                "R${"%.2f".format(avg)}",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                            )
                                        }
                                    }
                                }

                                // Highest Expense
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.medium
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Highest Expense",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = accent
                                            )
                                            val max = state.expenses.maxOfOrNull { it.amount } ?: 0.0
                                            Text(
                                                "R${"%.2f".format(max)}",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                            )
                                        }
                                    }
                                }

                                // Lowest Expense
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.medium
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Lowest Expense",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = accent
                                            )
                                            val min = state.expenses.minOfOrNull { it.amount } ?: 0.0
                                            Text(
                                                "R${"%.2f".format(min)}",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Track your spending habits and stay on top of your budget goals.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is ExpenseListViewModel.UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ExpenseListViewModel.UiState.Error -> {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetGoalsContent(
    modifier: Modifier = Modifier,
    viewModel: ExpenseListViewModel = hiltViewModel(),
    budgetGoalsViewModel: BudgetGoalsViewModel = hiltViewModel(),
    onNavigateToBudgetGoals: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val totalBudget by budgetGoalsViewModel.totalBudget.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply { currency = java.util.Currency.getInstance("ZAR") } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Monthly Budget Goals Overview",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (val state = uiState) {
                    is ExpenseListViewModel.UiState.Success -> {
                        val total = state.totalAmount
                        val budget = if (totalBudget > 0.0) totalBudget else 10000.0
                        val remaining = budget - total
                        val progress = (total / budget).toFloat().coerceIn(0f, 1f)

                        // Budget Progress
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Budget Progress",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Budget Summary
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Total Budget
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Total Budget",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "R${"%.2f".format(budget)}",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }

                            // Amount Spent
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Money,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Amount Spent",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "R${"%.2f".format(total)}",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }

                            // Remaining Budget
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Remaining Budget",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "R${"%.2f".format(remaining)}",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Stay within your budget to achieve your financial goals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToBudgetGoals,
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("View/Edit Budget Goals")
                        }
                    }
                    is ExpenseListViewModel.UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ExpenseListViewModel.UiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseListContent(
    modifier: Modifier = Modifier,
    onNavigateToExpenseList: () -> Unit = {},
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply { currency = java.util.Currency.getInstance("ZAR") } }

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense History",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            when (val state = uiState) {
                is ExpenseListViewModel.UiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Recent Activity",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Track your spending patterns",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Recent Expenses List
                        state.expenses.take(3).forEach { expense ->
                            val category = state.categories.find { it.id == expense.categoryId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            expense.description,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            category?.name ?: "Unknown",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = "-R${"%.2f".format(expense.amount)}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                            if (expense != state.expenses.take(3).last()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // View Expense List Button
                        Button(
                            onClick = onNavigateToExpenseList,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Expense List",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                is ExpenseListViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ExpenseListViewModel.UiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineMedium
        )
        // Add your categories content here
    }
}

@Composable
fun CategoryBarChart(categoryTotals: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val maxAmount = categoryTotals.maxOfOrNull { it.second } ?: 1.0
    Column(modifier = modifier) {
        categoryTotals.forEach { (category, amount) ->
            val color = categoryColorMap[category] ?: fallbackColors[categoryTotals.indexOfFirst { it.first == category } % fallbackColors.size]
            val animatedProgress by animateFloatAsState(
                targetValue = (amount / maxAmount).toFloat(),
                animationSpec = tween(durationMillis = 100)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category, modifier = Modifier.width(100.dp))
                Box(modifier = Modifier
                    .height(24.dp)
                    .weight(1f)
                    .padding(horizontal = 8.dp)) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val barWidth = size.width * animatedProgress
                        drawRect(
                            color = color,
                            topLeft = Offset.Zero,
                            size = Size(barWidth, size.height)
                        )
                    }
                    // Amount inside the bar
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(min = 0.dp, max = 1000.dp)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "R${"%.2f".format(amount)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryPieChart(categoryTotals: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val total = categoryTotals.sumOf { it.second }
    val colors = categoryTotals.map { (category, _) -> categoryColorMap[category] ?: fallbackColors[categoryTotals.indexOfFirst { it.first == category } % fallbackColors.size] }
    val sweepAngles = remember(categoryTotals) { categoryTotals.map { Animatable(0f) } }
    val targetSweeps = categoryTotals.map { if (total == 0.0) 0f else (it.second / total * 360f).toFloat() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(categoryTotals) {
        sweepAngles.forEachIndexed { i, anim ->
            scope.launch {
                anim.animateTo(targetSweeps[i], animationSpec = tween(durationMillis = 900))
            }
        }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp).padding(8.dp)) {
            var startAngle = -90f
            categoryTotals.forEachIndexed { i, (_, amount) ->
                val sweep = sweepAngles[i].value
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )
                // Draw percentage label
                if (sweep > 0f) {
                    val angle = Math.toRadians((startAngle + sweep / 2).toDouble())
                    val radius = size.minDimension / 2.5
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val x = (centerX + radius * cos(angle)).toFloat()
                    val y = (centerY + radius * sin(angle)).toFloat()
                    val percent = (amount / total * 100).toInt()
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$percent%",
                            x,
                            y,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 28f
                                isFakeBoldText = true
                            }
                        )
                    }
                }
                startAngle += sweep
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    // Legend
    Column {
        categoryTotals.forEachIndexed { i, (category, amount) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors[i])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$category: R${"%.2f".format(amount)}")
            }
        }
    }
} 