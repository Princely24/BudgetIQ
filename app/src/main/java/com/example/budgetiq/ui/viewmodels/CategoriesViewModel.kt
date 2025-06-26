package com.example.budgetiq.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.UserRepository
import com.example.budgetiq.ui.screens.CategoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val categories: List<CategoryUiState>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private var currentUserId: Long? = null

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
                        loadCategories()
                    } else {
                        _uiState.value = UiState.Error("No user found. Please log in first.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load user: ${e.message}")
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentUserId?.let { userId ->
                    categoryRepository.getCategoriesForUser(userId).collect { categories ->
                        _uiState.value = UiState.Success(
                            categories = categories.map { category ->
                                CategoryUiState(
                                    id = category.id,
                                    name = category.name
                                )
                            }
                        )
                    }
                } ?: run {
                    _uiState.value = UiState.Error("No user found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    categoryRepository.createCategory(name, userId)
                        .onSuccess {
                            // No need to manually reload - Flow will automatically update
                        }
                        .onFailure { e ->
                            _uiState.value = UiState.Error("Failed to create category: ${e.message}")
                        }
                } ?: run {
                    _uiState.value = UiState.Error("No user found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to create category: ${e.message}")
            }
        }
    }

    fun updateCategory(categoryId: Long, newName: String) {
        viewModelScope.launch {
            try {
                categoryRepository.getCategoryById(categoryId)
                    .onSuccess { category ->
                        val updatedCategory = category.copy(name = newName)
                        categoryRepository.updateCategory(updatedCategory)
                            .onSuccess {
                                // No need to manually reload - Flow will automatically update
                            }
                            .onFailure { e ->
                                _uiState.value = UiState.Error("Failed to update category: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        _uiState.value = UiState.Error("Failed to find category: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to update category: ${e.message}")
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.getCategoryById(categoryId)
                    .onSuccess { category ->
                        categoryRepository.deleteCategory(category)
                            .onSuccess {
                                // No need to manually reload - Flow will automatically update
                            }
                            .onFailure { e ->
                                _uiState.value = UiState.Error("Failed to delete category: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        _uiState.value = UiState.Error("Failed to find category: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to delete category: ${e.message}")
            }
        }
    }

    fun refresh() {
        loadCategories()
    }
} 