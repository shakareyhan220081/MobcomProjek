package com.example.mobcomprojek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobcomprojek.data.TaskRepository

// Factory ini akan membuat *semua* ViewModel yang butuh TaskRepository
class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(repository) as T
        }
        // Ditambahkan untuk TaskDetailViewModel
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            throw IllegalArgumentException("TaskDetailViewModel requires a taskId")
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory terpisah untuk TaskDetailViewModel karena butuh taskId
class TaskDetailViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(repository, taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}