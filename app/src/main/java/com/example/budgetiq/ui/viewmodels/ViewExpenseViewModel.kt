package com.example.budgetiq.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.model.Expense
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val expense: Expense,
            val categoryName: String,
            val categoryColor: Int = android.graphics.Color.GRAY
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private var currentExpenseId: Long? = null

    fun loadExpense(expenseId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            currentExpenseId = expenseId
            
            try {
                expenseRepository.getExpenseById(expenseId)
                    .onSuccess { expense ->
                        categoryRepository.getCategoryById(expense.categoryId)
                            .onSuccess { category ->
                                _uiState.value = UiState.Success(
                                    expense = expense,
                                    categoryName = category.name,
                                    categoryColor = category.color ?: android.graphics.Color.GRAY
                                )
                            }
                            .onFailure { e ->
                                _uiState.value = UiState.Error("Failed to load category: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        _uiState.value = UiState.Error("Failed to load expense: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load expense: ${e.message}")
            }
        }
    }

    fun deleteExpense() {
        viewModelScope.launch {
            currentExpenseId?.let { expenseId ->
                try {
                    expenseRepository.getExpenseById(expenseId)
                        .onSuccess { expense ->
                            expenseRepository.deleteExpense(expense)
                                .onFailure { e ->
                                    _uiState.value = UiState.Error("Failed to delete expense: ${e.message}")
                                }
                        }
                        .onFailure { e ->
                            _uiState.value = UiState.Error("Failed to load expense for deletion: ${e.message}")
                        }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Failed to delete expense: ${e.message}")
                }
            }
        }
    }
} 