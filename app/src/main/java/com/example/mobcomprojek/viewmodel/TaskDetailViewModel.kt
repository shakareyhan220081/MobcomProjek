package com.example.mobcomprojek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobcomprojek.data.Subtask
import com.example.mobcomprojek.data.TaskParentWithSubtasks
import com.example.mobcomprojek.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: Int
) : ViewModel() {

    // 1. Muat data dari database
    val taskState: StateFlow<TaskParentWithSubtasks?> =
        repository.getTask(taskId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null // Awalnya null
            )

    // 2. Fungsi save
    fun saveChanges(
        title: String,
        dueDate: Long?,
        reminderTime: Long?,
        priority: String,
        currentSubtasks: List<Subtask>,
        originalSubtasks: List<Subtask>
    ) {
        viewModelScope.launch {
            // Dapatkan TaskParent asli
            val currentParent = taskState.value?.taskParent ?: return@launch

            // Buat objek TaskParent yang sudah diupdate
            val updatedParent = currentParent.copy(
                title = title,
                dueDate = dueDate,
                reminderTime = reminderTime,
                priority = priority
            )

            // Cari subtask yang dihapus
            val deletedSubtasks = originalSubtasks.filterNot { o ->
                currentSubtasks.any { c -> c.id == o.id && c.id != 0 }
            }

            // Buat list subtask yang sudah diupdate (pastikan taskParentId benar)
            val updatedSubtasks = currentSubtasks.map {
                it.copy(taskParentId = currentParent.id)
            }

            // Panggil Repository untuk menyimpan
            repository.saveTaskDetail(
                taskParent = updatedParent,
                subtasks = updatedSubtasks,
                deletedSubtasks = deletedSubtasks
            )
        }
    }
}