package com.example.taskit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskit.data.repository.UserPreferencesRepository
import com.example.taskit.ui.focus.FocusScreen
import com.example.taskit.ui.habits.HabitsScreen
import com.example.taskit.ui.reports.ReportsScreen
import com.example.taskit.ui.settings.SettingsScreen
import com.example.taskit.ui.settings.SettingsViewModel
import com.example.taskit.ui.tasks.TasksScreen
import com.example.taskit.ui.theme.Screen
import com.example.taskit.ui.theme.TaskItBottomNavigation
import com.example.taskit.ui.theme.TaskItTheme
import com.example.taskit.ui.theme.BottomNavigationItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> 
        // Handle permission result if needed
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        setContent {
            val darkModeEnabled by userPreferencesRepository.userPreferencesFlow
                .collectAsState(initial = null)
            
            TaskItTheme(
                userDarkTheme = darkModeEnabled?.darkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskManagerApp()
                }
            }
        }
    }
}

@Composable
fun TaskManagerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavItems = listOf(
        BottomNavigationItem(
            route = Screen.Tasks.route,
            icon = Icons.Default.CheckCircle,
            label = "Tasks"
        ),
        BottomNavigationItem(
            route = Screen.Focus.route,
            icon = Icons.Default.Timer,
            label = "Focus"
        ),
        BottomNavigationItem(
            route = Screen.Habits.route,
            icon = Icons.Default.Repeat,
            label = "Habits"
        ),
        BottomNavigationItem(
            route = Screen.Reports.route,
            icon = Icons.Default.BarChart,
            label = "Reports"
        ),
        BottomNavigationItem(
            route = Screen.Settings.route,
            icon = Icons.Default.Settings,
            label = "Settings"
        )
    )

    Scaffold(
        bottomBar = {
            TaskItBottomNavigation(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onNavigate = { navController.navigate(it) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            composable(Screen.Focus.route) {
                FocusScreen()
            }
            composable(Screen.Habits.route) {
                HabitsScreen()
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskManagerAppPreview() {
    TaskItTheme {
        TaskManagerApp()
    }
}