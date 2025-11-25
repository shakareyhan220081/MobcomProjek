package com.example.mobcomprojek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobcomprojek.data.Category
import com.example.mobcomprojek.data.TaskParent
import com.example.mobcomprojek.data.TaskParentWithSubtasks // Penting
import com.example.mobcomprojek.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant // Perlu di-import
import java.time.LocalDate // Perlu di-import
import java.time.ZoneId // Perlu di-import

class TasksViewModel(private val repository: TaskRepository) : ViewModel() {

    // --- State untuk Kalender ---

    // 1. Menyimpan tanggal yang dipilih (default: hari ini)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // --- State yang Sudah Ada ---
    val categoriesWithTasks = repository.getCategoriesWithTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // --- State Turunan (Derived) untuk Kalender ---

    // 2. Map<LocalDate, List<Task>> untuk penanda di kalender
    val tasksByDate = categoriesWithTasks.map { categories ->
        categories
            .flatMap { it.tasks } // Ambil semua task dari semua kategori
            .filter { it.taskParent.dueDate != null } // Hanya task yang punya due date
            .groupBy {
                // Konversi Long timestamp ke LocalDate
                Instant.ofEpochMilli(it.taskParent.dueDate!!)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    // 3. List<Task> untuk list "Scheduled Tasks"
    val tasksForSelectedDate = combine(tasksByDate, selectedDate) { tasks, date ->
        tasks[date] ?: emptyList() // Ambil task untuk tanggal yang dipilih
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    // --- Fungsi ---

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addCategory(title: String) = viewModelScope.launch {
        repository.addCategory(title)
    }

    fun addTask(categoryId: Int, title: String) = viewModelScope.launch {
        repository.addTask(categoryId, title)
    }

    fun renameCategory(category: Category, newTitle: String) = viewModelScope.launch {
        repository.renameCategory(category.copy(title = newTitle))
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun renameTaskParent(taskParent: TaskParent, newTitle: String) = viewModelScope.launch {
        repository.renameTaskParent(taskParent.copy(title = newTitle))
    }

    fun deleteTaskParent(taskParent: TaskParent) = viewModelScope.launch {
        repository.deleteTaskParent(taskParent)
    }
}