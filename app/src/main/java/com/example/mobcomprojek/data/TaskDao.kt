package com.example.mobcomprojek.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // --- FUNGSI GET (Menggunakan Flow agar UI update otomatis) ---

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithTasks(): Flow<List<CategoryWithTasks>>

    @Transaction
    @Query("SELECT * FROM task_parents WHERE id = :id")
    fun getTaskWithSubtasks(id: Int): Flow<TaskParentWithSubtasks?> // Buat nullable

    // --- FUNGSI INSERT/UPDATE ---
    // @Upsert = (UPDATE or INSERT)

    @Upsert
    suspend fun upsertCategory(category: Category)

    @Upsert
    suspend fun upsertTaskParent(task: TaskParent): Long // Mengembalikan ID

    @Upsert
    suspend fun upsertSubtask(subtask: Subtask)

    // --- FUNGSI DELETE ---

    @Delete
    suspend fun deleteCategory(category: Category)

    @Delete
    suspend fun deleteTaskParent(taskParent: TaskParent)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)
}