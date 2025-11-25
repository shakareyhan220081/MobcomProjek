package com.example.mobcomprojek.data

import kotlinx.coroutines.flow.Flow

// Repository hanya meneruskan panggilan ke DAO
// Dalam app yang lebih besar, di sinilah Anda akan
// menggabungkan data lokal (DAO) dan data remote (API)

class TaskRepository(private val taskDao: TaskDao) {

    // --- Untuk TasksScreen ---
    fun getCategoriesWithTasks(): Flow<List<CategoryWithTasks>> {
        return taskDao.getCategoriesWithTasks()
    }

    suspend fun addCategory(title: String) {
        taskDao.upsertCategory(Category(title = title))
    }

    suspend fun addTask(categoryId: Int, title: String) {
        taskDao.upsertTaskParent(TaskParent(categoryId = categoryId, title = title))
    }

    suspend fun renameCategory(category: Category) {
        taskDao.upsertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        taskDao.deleteCategory(category)
    }

    suspend fun renameTaskParent(taskParent: TaskParent) {
        taskDao.upsertTaskParent(taskParent)
    }

    suspend fun deleteTaskParent(taskParent: TaskParent) {
        taskDao.deleteTaskParent(taskParent)
    }

    // --- Untuk TaskDetailScreen ---
    fun getTask(id: Int): Flow<TaskParentWithSubtasks?> {
        return taskDao.getTaskWithSubtasks(id)
    }

    suspend fun saveTaskDetail(
        taskParent: TaskParent,
        subtasks: List<Subtask>,
        deletedSubtasks: List<Subtask>
    ) {
        // Simpan parent task
        taskDao.upsertTaskParent(taskParent)

        // Simpan semua subtask (baru atau yang diubah)
        subtasks.forEach {
            taskDao.upsertSubtask(it)
        }

        // Hapus subtask yang ada di list 'delete'
        deletedSubtasks.forEach {
            taskDao.deleteSubtask(it)
        }
    }
}