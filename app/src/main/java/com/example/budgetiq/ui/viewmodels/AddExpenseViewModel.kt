package com.example.budgetiq.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.model.Category
import com.example.budgetiq.data.model.Expense
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.ExpenseRepository
import com.example.budgetiq.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    var saveState by mutableStateOf<SaveState>(SaveState.Idle)
        private set

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    private var currentUserId: Long? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                // Use continuous Flow collection instead of one-time
                userRepository.getAllUsers().collect { users ->
                    val user = users.firstOrNull()
                    if (user != null) {
                        currentUserId = user.id
                        loadCategories()
                    } else {
                        saveState = SaveState.Error("No user found. Please log in first.")
                    }
                }
            } catch (e: Exception) {
                saveState = SaveState.Error("Failed to load user: ${e.message}")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    // Use continuous Flow collection to automatically update when data changes
                    categoryRepository.getCategoriesForUser(userId).collect { categoryList ->
                        categories = categoryList
                    }
                }
            } catch (e: Exception) {
                saveState = SaveState.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    fun saveExpense(
        amount: Double,
        description: String,
        categoryId: Long,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        photoUri: String? = null
    ) {
        if (currentUserId == null) {
            saveState = SaveState.Error("No user logged in")
            return
        }

        if (amount <= 0) {
            saveState = SaveState.Error("Amount must be greater than 0")
            return
        }

        if (description.isBlank()) {
            saveState = SaveState.Error("Description cannot be empty")
            return
        }

        viewModelScope.launch {
            saveState = SaveState.Loading
            try {
                val expense = Expense(
                    userId = currentUserId!!,
                    categoryId = categoryId,
                    amount = amount,
                    description = description,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    photoPath = photoUri
                )
                expenseRepository.insertExpense(expense)
                    .onSuccess {
                        saveState = SaveState.Success
                    }
                    .onFailure { e ->
                        saveState = SaveState.Error(e.message ?: "Failed to save expense")
                    }
            } catch (e: Exception) {
                saveState = SaveState.Error(e.message ?: "Failed to save expense")
            }
        }
    }

    // Add a refresh function that can be called from UI
    fun refresh() {
        loadCategories()
    }
} 