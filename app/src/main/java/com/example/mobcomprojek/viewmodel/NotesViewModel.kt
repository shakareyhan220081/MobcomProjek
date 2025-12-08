package com.example.mobcomprojek.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobcomprojek.TaskApplication
import com.example.mobcomprojek.data.NoteItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    // Akses Repository dari Application
    private val repository = (application as TaskApplication).repository

    // 1. Ambil data NOTES (Realtime dari Room)
    val notesState: StateFlow<List<NoteItem>> = repository.getNotesStream()
        .catch { e ->
            Log.e("NotesViewModel", "Error loading notes: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Fungsi Hapus Note
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Gagal menghapus note: ${e.message}")
            }
        }
    }

    // Fungsi Kategori (Disimpan jaga-jaga, meski UI sudah dihapus)
    fun addCategory(title: String) = viewModelScope.launch {
        repository.addCategory(title, "note")
    }

    fun renameCategory(categoryId: String, newName: String) = viewModelScope.launch {
        repository.renameCategory(categoryId, newName)
    }

    fun deleteCategory(id: String) = viewModelScope.launch {
        repository.deleteCategory(id)
    }
}