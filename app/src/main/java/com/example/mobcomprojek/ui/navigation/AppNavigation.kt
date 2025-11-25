package com.example.mobcomprojek.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobcomprojek.data.TaskRepository // Import repository
import com.example.mobcomprojek.ui.screens.TaskDetailScreen
import com.example.mobcomprojek.ui.screens.TasksScreen
import com.example.mobcomprojek.viewmodel.TaskDetailViewModel
import com.example.mobcomprojek.viewmodel.TaskDetailViewModelFactory
import com.example.mobcomprojek.viewmodel.TaskViewModelFactory
import com.example.mobcomprojek.viewmodel.TasksViewModel

/**
 * AppNavHost defines the navigation graph and provides ViewModels to screens.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    repository: TaskRepository, // AppNavHost now needs the repository
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route, // Use your existing start destination
        modifier = modifier
    ) {

        // --- Home Screen ---
        composable(Screen.Home.route) {
            // TODO: Create HomeScreen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Home Screen")
            }
        }

        // --- Notes Screen ---
        composable(Screen.Notes.route) {
            // TODO: Create NotesScreen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Notes Screen")
            }
        }

        // --- Tasks Screen ---
        composable(Screen.Tasks.route) {
            // This fixes: "No value passed for parameter 'viewModel'."
            val tasksViewModel: TasksViewModel = viewModel(
                factory = TaskViewModelFactory(repository)
            )
            TasksScreen(
                navController = navController,
                viewModel = tasksViewModel // Pass the viewModel
            )
        }

        // --- Settings Screen ---
        composable(Screen.Settings.route) {
            // TODO: Create SettingsScreen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Settings Screen")
            }
        }

        // --- Task Detail Screen ---
        // This route definition fixes: "No parameter with name 'taskId' found."
        val taskDetailRoute = "task_detail/{taskId}"
        composable(taskDetailRoute) { backStackEntry ->
            // Extract taskId
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0

            // This fixes: "No value passed for parameter 'viewModel'."
            val detailViewModel: TaskDetailViewModel = viewModel(
                factory = TaskDetailViewModelFactory(repository, taskId)
            )
            TaskDetailScreen(
                navController = navController,
                viewModel = detailViewModel // Pass the viewModel
            )
        }
    }
}