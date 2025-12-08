package com.example.mobcomprojek.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobcomprojek.TaskApplication
import com.example.mobcomprojek.data.Subtask
import com.example.mobcomprojek.data.TaskParent
import com.example.mobcomprojek.data.TaskParentWithSubtasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {

    // Akses Repository dari Application
    private val repository = (application as TaskApplication).repository

    private val _taskState = MutableStateFlow<TaskParentWithSubtasks?>(null)
    val taskState: StateFlow<TaskParentWithSubtasks?> = _taskState.asStateFlow()

    fun loadTask(taskId: String?, categoryId: String? = "") {
        if (taskId == null || taskId == "new") {
            // Task Baru
            val newTask = TaskParent(id = "", categoryId = categoryId ?: "")
            _taskState.value = TaskParentWithSubtasks(newTask, emptyList())
        } else {
            // Load Task Lama dari Database Lokal
            viewModelScope.launch {
                combine(
                    repository.getTasksStream(),
                    repository.getAllSubtasksStream()
                ) { tasks, subtasks ->
                    val foundTask = tasks.find { it.id == taskId }
                    if (foundTask != null) {
                        val foundSubs = subtasks.filter { it.taskId == taskId }
                        TaskParentWithSubtasks(foundTask, foundSubs)
                    } else {
                        null
                    }
                }
                    .catch { e ->
                        Log.e("TaskDetailViewModel", "Error loading task detail: ${e.message}", e)
                    }
                    .collect { result ->
                        if (result != null) _taskState.value = result
                    }
            }
        }
    }

    fun saveChanges(
        title: String,
        dueDate: Long?,
        reminderTime: Long?,
        priority: String,
        currentSubtasks: List<Subtask>,
        originalSubtasks: List<Subtask>
    ) {
        val currentState = _taskState.value ?: return

        viewModelScope.launch {
            try {
                // 1. Update data parent
                val updatedTask = currentState.taskParent.copy(
                    title = title,
                    dueDate = dueDate,
                    reminderTime = reminderTime,
                    priority = priority
                )

                // 2. Cari subtask yang dihapus user
                val currentIds = currentSubtasks.map { it.id }
                val deletedIds = originalSubtasks
                    .filter { it.id !in currentIds && it.id.isNotEmpty() }
                    .map { it.id }

                // 3. Simpan ke Repository Lokal (Room)
                repository.saveTaskWithSubtasks(updatedTask, currentSubtasks, deletedIds)

            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "Gagal menyimpan task: ${e.message}")
            }
        }
    }
}