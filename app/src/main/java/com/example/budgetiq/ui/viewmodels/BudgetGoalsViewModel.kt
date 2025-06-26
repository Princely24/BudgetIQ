package com.example.budgetiq.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.model.Category
import com.example.budgetiq.data.repository.BudgetGoalRepository
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.ExpenseRepository
import com.example.budgetiq.data.repository.UserRepository
import com.example.budgetiq.ui.screens.BudgetGoalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetGoalsViewModel @Inject constructor(
    private val budgetGoalRepository: BudgetGoalRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val budgetGoals: List<BudgetGoalUiState>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    private val _recentCategories = MutableStateFlow<List<Category>>(emptyList())
    val recentCategories: StateFlow<List<Category>> = _recentCategories

    private var currentUserId: Long? = null

    private val _totalBudget = MutableStateFlow(0.0)
    val totalBudget: StateFlow<Double> = _totalBudget

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                userRepository.getAllUsers().collect { users ->
                    val user = users.firstOrNull()
                    currentUserId = user?.id
                    if (currentUserId != null) {
                        loadBudgetGoals()
                        loadCategories()
                    } else {
                        _uiState.value = UiState.Error("No user logged in")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load user: ${e.message}")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    categoryRepository.getCategoriesForUser(userId).collect { categoryList ->
                        categories = categoryList
                        // Update recent categories (last 5, most recent first)
                        _recentCategories.value = categoryList.sortedByDescending { it.id }.take(5)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    fun loadBudgetGoals() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentUserId?.let { userId ->
                    combine(
                        budgetGoalRepository.getBudgetGoalsForUser(userId),
                        categoryRepository.getCategoriesForUser(userId),
                        expenseRepository.getExpensesForUser(userId)
                    ) { goals, categories, expenses ->
                        _totalBudget.value = goals.sumOf { it.maxAmount }
                        val goalsWithSpending = goals.map { goal ->
                            val category = categories.find { it.id == goal.categoryId }
                            val spent = expenses.filter { it.categoryId == goal.categoryId }.sumOf { it.amount }
                            BudgetGoalUiState(
                                id = goal.id,
                                categoryId = goal.categoryId,
                                categoryName = category?.name ?: "Unknown Category",
                                minAmount = goal.minAmount,
                                maxAmount = goal.maxAmount,
                                spent = spent
                            )
                        }
                        UiState.Success(goalsWithSpending)
                    }.collect { successState ->
                        _uiState.value = successState
                    }
                } ?: run {
                    _uiState.value = UiState.Error("No user found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load budget goals: ${e.message}")
            }
        }
    }

    fun addBudgetGoal(minAmount: Double, maxAmount: Double, categoryId: Long) {
        viewModelScope.launch {
            try {
                if (maxAmount < minAmount) {
                    _uiState.value = UiState.Error("Maximum amount must be greater than minimum amount")
                    return@launch
                }
                
                currentUserId?.let { userId ->
                    budgetGoalRepository.createBudgetGoal(minAmount, maxAmount, categoryId, userId)
                        .onSuccess {
                        }
                        .onFailure { e ->
                            _uiState.value = UiState.Error("Failed to create budget goal: ${e.message}")
                        }
                } ?: run {
                    _uiState.value = UiState.Error("No user found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to create budget goal: ${e.message}")
            }
        }
    }

    fun updateBudgetGoal(goalId: Long, minAmount: Double, maxAmount: Double) {
        viewModelScope.launch {
            try {
                if (maxAmount < minAmount) {
                    _uiState.value = UiState.Error("Maximum amount must be greater than minimum amount")
                    return@launch
                }

                budgetGoalRepository.getBudgetGoalById(goalId)
                    .onSuccess { goal ->
                        val updatedGoal = goal.copy(minAmount = minAmount, maxAmount = maxAmount)
                        budgetGoalRepository.updateBudgetGoal(updatedGoal)
                            .onSuccess {
                            }
                            .onFailure { e ->
                                _uiState.value = UiState.Error("Failed to update budget goal: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        _uiState.value = UiState.Error("Failed to find budget goal: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to update budget goal: ${e.message}")
            }
        }
    }

    fun deleteBudgetGoal(goalId: Long) {
        viewModelScope.launch {
            try {
                budgetGoalRepository.getBudgetGoalById(goalId)
                    .onSuccess { goal ->
                        budgetGoalRepository.deleteBudgetGoal(goal)
                            .onSuccess {
                            }
                            .onFailure { e ->
                                _uiState.value = UiState.Error("Failed to delete budget goal: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        _uiState.value = UiState.Error("Failed to find budget goal: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to delete budget goal: ${e.message}")
            }
        }
    }

    fun refresh() {
        loadBudgetGoals()
    }
} 