package com.example.mobcomprojek.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobcomprojek.ui.screens.Home
import com.example.mobcomprojek.ui.screens.NoteDetailScreen
import com.example.mobcomprojek.ui.screens.NotesScreen
import com.example.mobcomprojek.ui.screens.SettingsScreen
import com.example.mobcomprojek.ui.screens.TaskDetailScreen
import com.example.mobcomprojek.ui.screens.TasksScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 4 Layar Utama
        composable(Screen.Home.route) {
            // Kita juga berikan NavController ke Home
            Home(modifier = Modifier, navController = navController)
        }
        composable(Screen.Notes.route) {
            // NotesScreen sekarang menerima NavController
            NotesScreen(modifier = Modifier, navController = navController)
        }
        composable(Screen.Tasks.route) {
            // Kita berikan juga ke TasksScreen
            TasksScreen(modifier = Modifier, navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(modifier = Modifier)
        }

        // 2 Layar Detail
        // (Kita tambahkan argumen {noteId} agar dinamis)
        composable(Screen.NoteDetail.route + "/{noteId}") {
            NoteDetailScreen(modifier = Modifier)
        }
        composable(Screen.TaskDetail.route + "/{taskId}") {
            TaskDetailScreen(modifier = Modifier)
        }
    }
}