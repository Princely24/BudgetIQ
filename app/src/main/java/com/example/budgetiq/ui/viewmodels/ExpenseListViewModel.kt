package com.example.budgetiq.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.model.Category
import com.example.budgetiq.data.model.CategoryTotal
import com.example.budgetiq.data.model.Expense
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.ExpenseRepository
import com.example.budgetiq.data.repository.UserRepository
import com.example.budgetiq.data.repository.BadgeRepository
import com.example.budgetiq.data.model.Badge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

enum class TimePeriod {
    WEEK, MONTH, YEAR, CUSTOM
}

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val expenses: List<Expense>,
            val categories: List<Category>,
            val totalAmount: Double,
            val startDate: LocalDate,
            val endDate: LocalDate,
            val selectedPeriod: TimePeriod,
            val categoryTotals: List<CategoryTotal>
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentUserId: Long? = null
    private var currentPeriod = TimePeriod.MONTH
    private var customStartDate: LocalDate? = null
    private var customEndDate: LocalDate? = null

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                userRepository.getAllUsers().collect { users ->
                    val user = users.firstOrNull()
                    if (user != null) {
                        currentUserId = user.id
                        loadExpenses()
                        loadBadges()
                    } else {
                        _uiState.value = UiState.Error("No user found. Please log in first.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load user: ${e.message}")
            }
        }
    }

    fun setTimePeriod(period: TimePeriod) {
        currentPeriod = period
        loadExpenses()
    }

    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        customStartDate = startDate
        customEndDate = endDate
        currentPeriod = TimePeriod.CUSTOM
        loadExpenses()
    }

    private fun getDateRangeForPeriod(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (currentPeriod) {
            TimePeriod.WEEK -> {
                val startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                startOfWeek to endOfWeek
            }
            TimePeriod.MONTH -> {
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                startOfMonth to endOfMonth
            }
            TimePeriod.YEAR -> {
                val startOfYear = today.withDayOfYear(1)
                val endOfYear = today.withDayOfYear(today.lengthOfYear())
                startOfYear to endOfYear
            }
            TimePeriod.CUSTOM -> {
                customStartDate?.let { start ->
                    customEndDate?.let { end ->
                        start to end
                    }
                } ?: (today to today)
            }
        }
    }

    private fun calculateCategoryTotals(
        expenses: List<Expense>,
        categories: List<Category>
    ): List<CategoryTotal> {
        val totalAmount = expenses.sumOf { it.amount }
        return expenses
            .groupBy { it.categoryId }
            .map { (categoryId, categoryExpenses) ->
                CategoryTotal(
                    categoryId = categoryId,
                    total = categoryExpenses.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.total }
    }

    private suspend fun checkConsistencyBadge(userId: Long, expenses: List<Expense>) {
        if (expenses.isEmpty()) return
        val today = LocalDate.now()
        val last7Days = (0..6).map { today.minusDays(it.toLong()) }
        val expenseDates = expenses.map { it.date }.toSet()
        val hasStreak = last7Days.all { it in expenseDates }
        if (hasStreak) {
            // Only award once per streak (per week)
            val weekOfYear = today.format(DateTimeFormatter.ofPattern("YYYY-ww"))
            val badgeName = "Consistency Badge"
            val alreadyAwarded = badgeRepository.getBadgesForUser(userId)
                .firstOrNull()?.any { it.name == badgeName &&
                    LocalDate.ofEpochDay(it.dateAwarded / (1000 * 60 * 60 * 24)).format(DateTimeFormatter.ofPattern("YYYY-ww")) == weekOfYear } == true
            if (!alreadyAwarded) {
                val badge = Badge(
                    userId = userId,
                    name = badgeName,
                    description = "Logged expenses for 7 consecutive days!"
                )
                badgeRepository.awardBadge(badge)
            }
        }
    }

    private suspend fun checkFrugalBadge(userId: Long, expenses: List<Expense>, totalBudget: Double) {
        if (totalBudget <= 0) return
        val today = LocalDate.now()
        val month = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val badgeName = "Frugal Badge"
        val monthExpenses = expenses.filter { it.date.month == today.month && it.date.year == today.year }
        val totalSpent = monthExpenses.sumOf { it.amount }
        if (totalSpent < 0.8 * totalBudget) {
            val alreadyAwarded = badgeRepository.hasBadgeForUserByMonth(userId, badgeName, month)
            if (!alreadyAwarded) {
                val badge = Badge(
                    userId = userId,
                    name = badgeName,
                    description = "Spent less than 80% of your monthly budget!",
                    amount = totalSpent
                )
                badgeRepository.awardBadge(badge)
            }
        }
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentUserId?.let { userId ->
                    val (startDate, endDate) = getDateRangeForPeriod()

                    combine(
                        expenseRepository.getExpensesForPeriod(userId, startDate, endDate),
                        categoryRepository.getCategoriesForUser(userId)
                    ) { expenses, categories ->
                        val totalAmount = expenses.sumOf { it.amount }
                        val categoryTotals = calculateCategoryTotals(expenses, categories)

                        // Badge logic
                        launch {
                            checkConsistencyBadge(userId, expenses)
                            // For Frugal badge, get total budget from BudgetGoalsViewModel or repository
                            // Here, you may need to inject BudgetGoalsViewModel or pass totalBudget as a parameter
                        }

                        UiState.Success(
                            expenses = expenses,
                            categories = categories,
                            totalAmount = totalAmount,
                            startDate = startDate,
                            endDate = endDate,
                            selectedPeriod = currentPeriod,
                            categoryTotals = categoryTotals
                        )
                    }.collect { successState ->
                        _uiState.value = successState
                    }
                } ?: run {
                    _uiState.value = UiState.Error("No user found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load expenses: ${e.message}")
            }
        }
    }

    fun loadBadges() {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            badgeRepository.getBadgesForUser(userId).collect { badgeList ->
                _badges.value = badgeList
            }
        }
    }

    // Add a refresh function that can be called from UI
    fun refresh() {
        loadExpenses()
    }
} 