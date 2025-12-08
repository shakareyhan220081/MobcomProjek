package com.example.mobcomprojek.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Membuat file penyimpanan setting bernama "user_settings"
val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val FONT_SCALE_KEY = floatPreferencesKey("font_scale")
    }

    // Ambil Nama (Default: "User")
    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY] ?: "User"
        }

    // Ambil Skala Font (Default: 1.0f / Normal)
    val fontScale: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_SCALE_KEY] ?: 1.0f
        }

    // Simpan Nama
    suspend fun saveName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    // Simpan Skala Font
    suspend fun saveFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SCALE_KEY] = scale
        }
    }
}