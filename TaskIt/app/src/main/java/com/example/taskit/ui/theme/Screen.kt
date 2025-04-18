package com.example.taskit.ui.theme

/**
 * Sealed class representing the different screens in the TaskIt app
 */
sealed class Screen(val route: String) {
    object Tasks : Screen("tasks")
    object Focus : Screen("focus")
    object Habits : Screen("habits")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
} 