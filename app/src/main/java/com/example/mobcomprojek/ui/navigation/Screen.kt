package com.example.mobcomprojek.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
// IMPORT BARU UNTUK IKON YANG DIPERBAIKI
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {

    object Home : Screen("home", "Home", Icons.Filled.Home)
    // DIUBAH ke AutoMirrored
    object Notes : Screen("notes", "Notes", Icons.AutoMirrored.Filled.Notes)
    // DIUBAH ke AutoMirrored
    object Tasks : Screen("tasks", "Tasks", Icons.AutoMirrored.Filled.ListAlt)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)


    object NoteDetail : Screen("note_detail", "Note Detail")
    object TaskDetail : Screen("task_detail", "Task Detail")
}