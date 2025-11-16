package com.example.mobcomprojek.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Composable header untuk section.
 * @param title Judul section.
 * @param showSeeAll Jika 'true', tombol "Lihat Semua" akan tampil.
 * @param onSeeAllClick Aksi saat "Lihat Semua" diklik.
 */
@Composable
fun SectionHeader(
    title: String,
    showSeeAll: Boolean = true, // Kita buat ini agar bisa diatur
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold
            )
        )
        // Tombol "Lihat Semua" hanya tampil jika showSeeAll = true
        if (showSeeAll) {
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "Lihat Semua",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}