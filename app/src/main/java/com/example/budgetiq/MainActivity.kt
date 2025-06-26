package com.example.budgetiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.budgetiq.navigation.Screen
import com.example.budgetiq.ui.screens.*
import com.example.budgetiq.ui.theme.BudgetIQTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetIQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.GetStarted.route
                    ) {
                        composable(Screen.GetStarted.route) {
                            GetStartedScreen(
                                onNavigateToLogin = {
                                    navController.navigate(Screen.Login.route)
                                }
                            )
                        }

                        composable(Screen.Login.route) {
                            LoginScreen(
                                onNavigateToRegister = {
                                    navController.navigate(Screen.Register.route)
                                },
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.GetStarted.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.navigateUp()
                                },
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.GetStarted.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToAddExpense = {
                                    navController.navigate(Screen.AddExpense.route)
                                },
                                onNavigateToExpenseList = {
                                    navController.navigate(Screen.ExpenseList.route)
                                },
                                onNavigateToCategories = {
                                    navController.navigate(Screen.Categories.route)
                                },
                                onNavigateToBudgetGoals = {
                                    navController.navigate(Screen.BudgetGoals.route)
                                }
                            )
                        }

                        composable(Screen.AddExpense.route) {
                            AddExpenseScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                },
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.ExpenseList.route) {
                            ExpenseListScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                },
                                onNavigateToViewExpense = { expenseId ->
                                    navController.navigate(Screen.ViewExpense.createRoute(expenseId))
                                }
                            )
                        }

                        composable(
                            route = Screen.ViewExpense.route,
                            arguments = listOf(
                                navArgument("expenseId") {
                                    type = NavType.LongType
                                }
                            )
                        ) { backStackEntry ->
                            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: return@composable
                            ViewExpenseScreen(
                                expenseId = expenseId,
                                onNavigateBack = {
                                    navController.navigateUp()
                                },
                                onNavigateToEdit = { id ->
                                    // TODO: Implement edit navigation when EditExpenseScreen is created
                                    navController.navigateUp()
                                }
                            )
                        }

                        composable(Screen.Categories.route) {
                            CategoriesScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }

                        composable(Screen.BudgetGoals.route) {
                            BudgetGoalsScreen(
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}