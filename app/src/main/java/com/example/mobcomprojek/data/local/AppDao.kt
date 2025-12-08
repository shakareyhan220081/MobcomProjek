package com.example.mobcomprojek.data.local

import androidx.room.*
import com.example.mobcomprojek.data.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // === NOTES ===
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<NoteItem>

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskParent>

    @Query("SELECT * FROM categories WHERE isSynced = 0")
    suspend fun getUnsyncedCategories(): List<Category>

    // Subtask agak unik, kita update saat parentnya diupdate,
    // atau bisa tambahkan query serupa jika perlu sync terpisah.
    @Query("SELECT * FROM subtasks WHERE isSynced = 0")
    suspend fun getUnsyncedSubtasks(): List<Subtask>


    // === TASKS ===
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskParent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskParent)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean)

    @Query("UPDATE tasks SET title = :title WHERE id = :taskId")
    suspend fun updateTaskTitle(taskId: String, title: String)

    // === SUBTASKS ===
    @Query("SELECT * FROM subtasks")
    fun getAllSubtasks(): Flow<List<Subtask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask)

    @Query("DELETE FROM subtasks WHERE id = :subtaskId")
    suspend fun deleteSubtaskById(subtaskId: String)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subtaskId")
    suspend fun updateSubtaskStatus(subtaskId: String, isCompleted: Boolean)

    // === CATEGORIES ===
    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategories(type: String): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :catId")
    suspend fun deleteCategory(catId: String)

    @Query("UPDATE categories SET title = :title WHERE id = :catId")
    suspend fun updateCategoryTitle(catId: String, title: String)

    // === TRANSACTION (RELIABILITY) ===
    // Menyimpan task dan subtask sekaligus secara atomik
    @Transaction
    suspend fun saveTaskWithSubtasksTransaction(
        task: TaskParent,
        subtasksToInsert: List<Subtask>,
        subtaskIdsToDelete: List<String>
    ) {
        insertTask(task)
        subtaskIdsToDelete.forEach { deleteSubtaskById(it) }
        subtasksToInsert.forEach { insertSubtask(it) }
    }
}