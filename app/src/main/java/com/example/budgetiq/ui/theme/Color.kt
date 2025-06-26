package com.example.budgetiq.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val Primary = Color(0xFF1976D2) // Professional blue
val PrimaryLight = Color(0xFF63A4FF)
val PrimaryDark = Color(0xFF004BA0)

// Secondary colors
val Secondary = Color(0xFF388E3C) // Professional green
val SecondaryLight = Color(0xFF6ABF69)
val SecondaryDark = Color(0xFF00600F)

// Tertiary colors
val Tertiary = Color(0xFF7B1FA2) // Professional purple
val TertiaryLight = Color(0xFFAE52D4)
val TertiaryDark = Color(0xFF4A0072)

// Neutral colors
val Background = Color(0xFFF5F5F5) // Light gray background
val Surface = Color(0xFFFFFFFF) // White surface
val SurfaceVariant = Color(0xFFF0F0F0) // Slightly darker surface variant

// Text colors
val OnPrimary = Color(0xFFFFFFFF) // White text on primary
val OnSecondary = Color(0xFFFFFFFF) // White text on secondary
val OnTertiary = Color(0xFFFFFFFF) // White text on tertiary
val OnBackground = Color(0xFF1C1B1F) // Dark text on background
val OnSurface = Color(0xFF1C1B1F) // Dark text on surface
val OnSurfaceVariant = Color(0xFF49454F) // Slightly lighter text for variants

// Status colors
val Success = Color(0xFF4CAF50) // Green for success states
val Error = Color(0xFFD32F2F) // Red for error states
val Warning = Color(0xFFFFA000) // Amber for warning states
val Info = Color(0xFF2196F3) // Blue for info states

// Category colors
val CategoryColors = mapOf(
    "Transportation" to Color(0xFF1976D2), // Blue
    "Food & Dining" to Color(0xFF388E3C), // Green
    "Bills & Utilities" to Color(0xFFFBC02D), // Yellow
    "Entertainment" to Color(0xFFD32F2F), // Red
    "Shopping" to Color(0xFF7B1FA2), // Purple
    "Healthcare" to Color(0xFF00BCD4), // Cyan
    "Education" to Color(0xFF009688), // Teal
    "Other" to Color(0xFF757575) // Gray
)

// Chart colors
val ChartColors = listOf(
    Color(0xFF1976D2), // Blue
    Color(0xFF388E3C), // Green
    Color(0xFFFBC02D), // Yellow
    Color(0xFFD32F2F), // Red
    Color(0xFF7B1FA2), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFF009688), // Teal
    Color(0xFF757575) // Gray
)