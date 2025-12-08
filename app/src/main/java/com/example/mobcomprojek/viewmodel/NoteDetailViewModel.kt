package com.example.mobcomprojek.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobcomprojek.TaskApplication
import com.example.mobcomprojek.data.NoteItem
import com.example.mobcomprojek.utils.AlarmScheduler // Import Baru
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TaskApplication).repository
    private val alarmScheduler = AlarmScheduler(application) // Inisialisasi Scheduler

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _noteState = MutableStateFlow<NoteItem?>(null)
    val noteState: StateFlow<NoteItem?> = _noteState.asStateFlow()

    fun loadNote(noteId: String?) {
        // ... (Kode lama tetap sama) ...
        if (noteId == null || noteId == "new") {
            _noteState.value = null
        } else {
            viewModelScope.launch {
                val allNotes = repository.getNotesStream().firstOrNull()
                val foundNote = allNotes?.find { it.id == noteId }
                _noteState.value = foundNote
            }
        }
    }

    fun saveNote(
        currentId: String?,
        title: String,
        content: String,
        isPinned: Boolean,
        categoryId: String,
        reminderTime: Long?,
        dueDate: Long?,
        priority: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Objek note yang akan disimpan
                val noteToSave: NoteItem

                if (currentId == null || currentId == "new") {

                    repository.addNote(title, content, isPinned, categoryId, reminderTime, dueDate, priority)


                } else {
                    // Update Note Lama (ID Sudah Tahu)
                    noteToSave = NoteItem(
                        id = currentId,
                        title = title,
                        content = content,
                        categoryId = categoryId,
                        isPinned = isPinned,
                        reminderTime = reminderTime,
                        dueDate = dueDate,
                        priority = priority
                    )
                    repository.updateNote(noteToSave)

                    // PASANG ALARM DISINI (Hanya bisa untuk Note yang sudah punya ID/Edit)
                    alarmScheduler.scheduleNote(noteToSave)
                }
            } catch (e: Exception) {
                Log.e("NoteDetailViewModel", "Gagal: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(noteId: String?) {
        if (noteId != null && noteId != "new") {
            viewModelScope.launch {
                repository.deleteNote(noteId)
                alarmScheduler.cancel(noteId.hashCode()) // Batalkan Alarm jika dihapus
            }
        }
    }
}