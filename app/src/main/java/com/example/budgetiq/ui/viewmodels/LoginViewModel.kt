package com.example.budgetiq.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetiq.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    var loginState by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginState = LoginState.Loading
            userRepository.login(username, password)
                .onSuccess {
                    loginState = LoginState.Success
                }
                .onFailure {
                    loginState = LoginState.Error(it.message ?: "Login failed")
                }
        }
    }
} 