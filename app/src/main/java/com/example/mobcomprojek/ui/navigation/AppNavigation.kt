package com.example.mobcomprojek.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobcomprojek.ui.screens.Home
import com.example.mobcomprojek.ui.screens.NoteDetailScreen
import com.example.mobcomprojek.ui.screens.NotesScreen
import com.example.mobcomprojek.ui.screens.SettingsScreen
import com.example.mobcomprojek.ui.screens.TaskDetailScreen
import com.example.mobcomprojek.ui.screens.TasksScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean, // <-- BARU: Terima state
    onThemeToggle: () -> Unit // <-- BARU: Terima lambda
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 4 Layar Utama
        composable(Screen.Home.route) {
            Home(
                modifier = Modifier,
                navController = navController,
                isDarkTheme = isDarkTheme, // <-- Kirim state
                onThemeToggle = onThemeToggle // <-- Kirim lambda
            )
        }
        composable(Screen.Notes.route) {
            NotesScreen(modifier = Modifier, navController = navController)
        }
        composable(Screen.Tasks.route) {
            TasksScreen(modifier = Modifier, navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(modifier = Modifier)
        }


        composable(
            route = Screen.NoteDetail.route + "/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")

            NoteDetailScreen(
                navController = navController,
                noteId = noteId
            )
        }

        composable(Screen.TaskDetail.route + "/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(
                navController = navController,
                taskId = taskId
            )
        }
    }
}