package com.example.mobcomprojek.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
// TAMBAHKAN IMPORT INI
import androidx.navigation.NavType
import androidx.navigation.navArgument

import com.example.mobcomprojek.ui.screens.LoginScreen
import com.example.mobcomprojek.ui.screens.NoteDetailScreen
import com.example.mobcomprojek.ui.screens.NotesScreen
import com.example.mobcomprojek.ui.screens.SettingsScreen
import com.example.mobcomprojek.ui.screens.TaskDetailScreen
import com.example.mobcomprojek.ui.screens.TasksScreen
import com.example.mobcomprojek.ui.screens.Home
import com.example.mobcomprojek.viewmodel.NoteDetailViewModel
import com.example.mobcomprojek.viewmodel.NotesViewModel
import com.example.mobcomprojek.viewmodel.TaskDetailViewModel
import com.example.mobcomprojek.viewmodel.TasksViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onThemeToggle: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        // --- Home Screen ---
        composable(Screen.Home.route) {
            val notesVM: NotesViewModel = viewModel()
            val tasksVM: TasksViewModel = viewModel()
            Home(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                notesViewModel = notesVM,
                tasksViewModel = tasksVM
            )
        }

        // --- Login Screen ---
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // --- Notes Screen ---
        composable(Screen.Notes.route) {
            val notesViewModel: NotesViewModel = viewModel()
            NotesScreen(
                navController = navController,
                viewModel = notesViewModel
            )
        }

        // --- Note Detail Screen (UPDATE DISINI) ---
        composable(
            route = Screen.NoteDetail.route + "/{noteId}", // Hapus query ?categoryId
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")

            // Panggil tanpa initialCategoryId
            NoteDetailScreen(
                navController = navController,
                noteId = noteId
            )
        }

        // --- Tasks Screen ---
        composable(Screen.Tasks.route) {
            val tasksViewModel: TasksViewModel = viewModel()
            TasksScreen(
                navController = navController,
                viewModel = tasksViewModel
            )
        }

        // --- Task Detail Screen ---
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val detailViewModel: TaskDetailViewModel = viewModel()

            LaunchedEffect(taskId) {
                detailViewModel.loadTask(taskId)
            }

            TaskDetailScreen(
                navController = navController,
                taskId = taskId,
                viewModel = detailViewModel
            )
        }

        // --- Settings Screen ---
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}