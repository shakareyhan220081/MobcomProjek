package com.example.mobcomprojek.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobcomprojek.R
import com.example.mobcomprojek.data.local.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences(context) }

    // State
    val savedName by userPreferences.userName.collectAsState(initial = "User")
    val savedFontScale by userPreferences.fontScale.collectAsState(initial = 1.0f)

    var nameInput by remember(savedName) { mutableStateOf(savedName) }
    var tempFontScale by remember(savedFontScale) { mutableStateOf(savedFontScale) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. SECTION PROFILE (Ganti Nama)
        SettingsCard(title = "Profile", icon = Icons.Default.Person) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        scope.launch { userPreferences.saveName(nameInput) }
                    }) {
                        Icon(Icons.Default.Save, "Save Name")
                    }
                }
            )
            Text(
                "Tap save icon to apply changes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 2. SECTION APPEARANCE (Ukuran Font)
        SettingsCard(title = "Appearance", icon = Icons.Default.TextFormat) {
            Text("Font Size: ${(tempFontScale * 100).toInt()}%")
            Slider(
                value = tempFontScale,
                onValueChange = { tempFontScale = it },
                onValueChangeFinished = {
                    scope.launch { userPreferences.saveFontScale(tempFontScale) }
                },
                valueRange = 0.8f..1.4f, // Min 80%, Max 140%
                steps = 5
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Small", style = MaterialTheme.typography.labelSmall)
                Text("Large", style = MaterialTheme.typography.labelSmall)
            }
        }

        // 3. SECTION SOUND (Test Ringtone)
        SettingsCard(title = "Notification Sound", icon = Icons.Default.VolumeUp) {
            Button(
                onClick = {
                    // FIX: Menggunakan MediaPlayer agar lebih stabil saat testing
                    // Suara tidak akan mati saat tombol volume ditekan
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.custom_alert)
                        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                        mediaPlayer.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play Custom Sound (Test)")
            }
            Text(
                "This sound will play for reminders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SettingsCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}