package com.example.budgetiq.data.repository

import com.example.budgetiq.data.dao.UserDao
import com.example.budgetiq.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val categoryRepository: CategoryRepository
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun createUser(username: String, password: String): Result<Long> {
        return try {
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                Result.failure(Exception("Username already exists"))
            } else {
                val userId = userDao.insertUser(User(username = username, password = password))
                // Create default categories for the new user
                categoryRepository.createDefaultCategories(userId)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val user = userDao.login(username, password)
            if (user != null) {
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getUserById(userId: Long): Flow<User?> = userDao.getUserById(userId)

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
} 