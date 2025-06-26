package com.example.budgetiq.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class FeatureSlide(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GetStartedScreen(
    onNavigateToLogin: () -> Unit
) {
    val features = remember {
        listOf(
            FeatureSlide(
                icon = Icons.Default.AccountBalance,
                title = "Smart Budgeting",
                description = "Track your expenses and income with intelligent categorization"
            ),
            FeatureSlide(
                icon = Icons.Default.Analytics,
                title = "Financial Insights",
                description = "Get detailed analytics and insights about your spending habits"
            ),
            FeatureSlide(
                icon = Icons.Default.Timeline,
                title = "Expense Tracking",
                description = "Easily record and categorize your daily expenses"
            ),
            FeatureSlide(
                icon = Icons.Default.Star,
                title = "Financial Goals",
                description = "Set and achieve your financial goals with our smart planning tools"
            )
        )
    }

    var currentSlide by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // 5 seconds delay
            currentSlide = (currentSlide + 1) % features.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo and App Name
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "BudgetIQ",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your Smart Financial Companion",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature Carousel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(1)) with
                        fadeOut(animationSpec = tween(1))
                    },
                    label = "Feature Carousel"
                ) { slideIndex ->
                    val feature = features[slideIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = feature.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feature.description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Page Indicators
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .height(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentSlide) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Get Started Button
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
} 