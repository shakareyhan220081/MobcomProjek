package com.example.mobcomprojek.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobcomprojek.TaskApplication
import com.example.mobcomprojek.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Ganti ViewModel() menjadi AndroidViewModel(application)
class TasksViewModel(application: Application) : AndroidViewModel(application) {

    // Ambil repository dari Application
    private val repository = (application as TaskApplication).repository

    // GANTI SEMUA 'FirestoreRepository' DENGAN 'repository' DI BAWAH INI

    val categoriesWithTasks: StateFlow<List<CategoryWithTasks>> = combine(
        repository.getCategoriesStream("task"),
        repository.getTasksStream(),
        repository.getAllSubtasksStream()
    ) { categories, tasks, subtasks ->
        categories.map { category ->
            val tasksInCat = tasks.filter { it.categoryId == category.id }
            val tasksWithSubs = tasksInCat.map { task ->
                val subsForTask = subtasks.filter { it.taskId == task.id }
                TaskParentWithSubtasks(task, subsForTask)
            }
            CategoryWithTasks(category, tasksWithSubs)
        }
    }
        .catch { e -> Log.e("TasksVM", "Error", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(title: String) = viewModelScope.launch {
        repository.addCategory(title, "task")
    }

    fun deleteCategory(id: String) = viewModelScope.launch {
        repository.deleteCategory(id)
    }

    fun renameCategory(cat: Category, name: String) = viewModelScope.launch {
        repository.renameCategory(cat.id, name)
    }

    fun addTask(catId: String, title: String) = viewModelScope.launch {
        repository.addTask(catId, title)
    }

    fun deleteTaskParent(t: TaskParent) = viewModelScope.launch {
        repository.deleteTask(t.id)
    }

    fun renameTaskParent(t: TaskParent, title: String) = viewModelScope.launch {
        repository.updateTaskTitle(t.id, title)
    }

    fun toggleTaskCompletion(task: TaskParent) = viewModelScope.launch {
        val newStatus = !task.isCompleted
        // Kita butuh ID subtask, logic ini sama seperti sebelumnya tapi panggil repository
        // Karena di viewmodel ini agak ribet ambil ID subtask dari flow,
        // simplifikasi: panggil saja fungsi repo yg sudah kita buat.
        // TAPI: Repository butuh list ID.
        // Workaround cepat: Di DataRepository logic batch update bisa dioptimize.
        // Untuk sekarang, kita ambil dari current value:
        val currentData = categoriesWithTasks.value
        val subtasks = currentData.flatMap { it.tasks }
            .find { it.taskParent.id == task.id }?.subtasks ?: emptyList()
        val subtaskIds = subtasks.map { it.id }

        repository.toggleTaskAndSubtasksBatch(task.id, newStatus, subtaskIds)
    }

    fun toggleSubtaskCompletion(subtask: Subtask) = viewModelScope.launch {
        val newStatus = !subtask.isCompleted
        repository.updateSubtaskStatus(subtask.id, newStatus)

        // Cek parent logic (sama seperti sebelumnya)
        val currentData = categoriesWithTasks.value
        val parentTaskData = currentData.flatMap { it.tasks }.find { it.taskParent.id == subtask.taskId }
        if (parentTaskData != null) {
            val allSubtasks = parentTaskData.subtasks
            val areAllCompleted = allSubtasks.all {
                if (it.id == subtask.id) newStatus else it.isCompleted
            }
            if (parentTaskData.taskParent.isCompleted != areAllCompleted) {
                repository.updateTaskStatus(parentTaskData.taskParent.id, areAllCompleted)
            }
        }
    }
}