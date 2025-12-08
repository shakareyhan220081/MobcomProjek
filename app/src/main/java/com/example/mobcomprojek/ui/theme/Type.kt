package com.example.mobcomprojek.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Ubah menjadi FUNGSI agar bisa menerima scale
fun getCustomTypography(fontScale: Float): Typography {
    return Typography(
        // Kita kalikan semua ukuran font dengan fontScale
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp * fontScale,
            lineHeight = 64.sp * fontScale
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp * fontScale,
            lineHeight = 52.sp * fontScale
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp * fontScale,
            lineHeight = 36.sp * fontScale
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp * fontScale,
            lineHeight = 28.sp * fontScale
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp * fontScale,
            lineHeight = 24.sp * fontScale,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp * fontScale,
            lineHeight = 20.sp * fontScale,
            letterSpacing = 0.25.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp * fontScale,
            lineHeight = 16.sp * fontScale,
            letterSpacing = 0.5.sp
        )
    )
}