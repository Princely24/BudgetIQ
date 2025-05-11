package com.example.budgetiq.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.repository.CategoryRepository
import com.example.budgetiq.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    var registerState by mutableStateOf<RegisterState>(RegisterState.Idle)
        private set

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            registerState = RegisterState.Error("Username and password cannot be empty")
            return
        }

        viewModelScope.launch {
            registerState = RegisterState.Loading
            userRepository.createUser(username, password)
                .onSuccess { userId ->
                    // Create default categories for the new user
                    try {
                        categoryRepository.createDefaultCategories(userId)
                        registerState = RegisterState.Success
                    } catch (e: Exception) {
                        registerState = RegisterState.Error("Registration successful but failed to create default categories")
                    }
                }
                .onFailure {
                    registerState = RegisterState.Error(it.message ?: "Registration failed")
                }
        }
    }
} 