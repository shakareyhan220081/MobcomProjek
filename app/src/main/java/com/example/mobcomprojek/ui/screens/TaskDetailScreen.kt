package com.example.mobcomprojek.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobcomprojek.ui.theme.MobcomProjekTheme
// --- IMPORT BARU ---
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// Ini adalah layar "Task" (Untitled T...)
@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController, // <-- TAMBAHKAN INI
    taskId: String? // <-- TAMBAHKAN INI
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            // --- KITA BISA GUNAKAN taskId DI SINI ---
            text = "Layar Detail Task (Editor)\nTask ID: $taskId\n(Belum Dibuat)",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun TaskDetailScreenPreviewLight() {
    MobcomProjekTheme(darkTheme = false) {
        // --- PERBARUI PREVIEW ---
        TaskDetailScreen(
            navController = rememberNavController(),
            taskId = "preview_123"
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun TaskDetailScreenPreviewDark() {
    MobcomProjekTheme(darkTheme = true) {
        // --- PERBARUI PREVIEW ---
        TaskDetailScreen(
            navController = rememberNavController(),
            taskId = "preview_123"
        )
    }
}