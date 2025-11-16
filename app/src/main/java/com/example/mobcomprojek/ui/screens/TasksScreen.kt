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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun TasksScreen(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Layar Tasks\n(Belum Dibuat)",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun TasksScreenPreviewLight() {
    MobcomProjekTheme(darkTheme = false) {
        TasksScreen(navController = rememberNavController())
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun TasksScreenPreviewDark() {
    MobcomProjekTheme(darkTheme = true) {
        TasksScreen(navController = rememberNavController())
    }
}

