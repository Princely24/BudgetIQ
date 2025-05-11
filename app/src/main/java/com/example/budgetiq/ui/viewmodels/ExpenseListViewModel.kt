package com.example.budgetiq.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.model.Category
import com.example.budgetiq.data.model.CategoryTotal
import com.example.budgetiq.data.model.Expense
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.ExpenseRepository
import com.example.budgetiq.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class TimePeriod {
    WEEK, MONTH, YEAR, CUSTOM
}

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
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

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.currentUser.first()
                if (user != null) {
                    currentUserId = user.id
                    loadExpenses()
                } else {
                    _uiState.value = UiState.Error("No user found. Please log in first.")
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

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentUserId?.let { userId ->
                    val (startDate, endDate) = getDateRangeForPeriod()

                    // Load expenses and categories in parallel
                    val expenses = expenseRepository.getExpensesForPeriod(userId, startDate, endDate).first()
                    val categories = categoryRepository.getCategoriesForUser(userId).first()

                    // Calculate totals
                    val totalAmount = expenses.sumOf { it.amount }
                    val categoryTotals = calculateCategoryTotals(expenses, categories)

                    _uiState.value = UiState.Success(
                        expenses = expenses,
                        categories = categories,
                        totalAmount = totalAmount,
                        startDate = startDate,
                        endDate = endDate,
                        selectedPeriod = currentPeriod,
                        categoryTotals = categoryTotals
                    )
                } ?: run {
                    _uiState.value = UiState.Error("No user found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load expenses: ${e.message}")
            }
        }
    }
} 