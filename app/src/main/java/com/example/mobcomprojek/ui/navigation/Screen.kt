package com.example.mobcomprojek.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Login // Tambahkan import ini
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    // --- TAMBAHAN BARU ---
    object Login : Screen("login", "Login", Icons.AutoMirrored.Filled.Login)

    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Notes : Screen("notes", "Notes", Icons.AutoMirrored.Filled.Notes)
    object Tasks : Screen("tasks", "Tasks", Icons.AutoMirrored.Filled.ListAlt)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    object NoteDetail : Screen("note_detail", "Note Detail")
    object TaskDetail : Screen("task_detail", "Task Detail")
}