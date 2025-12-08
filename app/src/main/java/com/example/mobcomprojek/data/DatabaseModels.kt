package com.example.mobcomprojek.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

// =======================
// 1. DATA MODEL CATATAN
// =======================
@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val categoryId: String = "",
    val reminderTime: Long? = null,
    val dueDate: Long? = null,
    val priority: String = "None",

    @get:PropertyName("isPinned")
    @set:PropertyName("isPinned")
    var isPinned: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    // BARU: Penanda apakah sudah sinkron ke Firebase
    // Exclude agar tidak ikut terupload ke Firebase sebagai field
    @get:PropertyName("ignore_sync")
    @set:PropertyName("ignore_sync")
    var isSynced: Boolean = true
)

// =======================
// 2. DATA MODEL TUGAS
// =======================

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = "task",

    @get:PropertyName("ignore_sync")
    var isSynced: Boolean = true
)

@Entity(tableName = "tasks")
data class TaskParent(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val categoryId: String = "",
    val title: String = "",
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val priority: String = "None",

    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("ignore_sync")
    var isSynced: Boolean = true
)

@Entity(tableName = "subtasks")
data class Subtask(
    @PrimaryKey val id: String = "",
    val taskId: String = "",
    val title: String = "",

    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,

    @get:PropertyName("ignore_sync")
    var isSynced: Boolean = true
)

// Helper class tetap sama...
data class CategoryWithTasks(val category: Category, val tasks: List<TaskParentWithSubtasks>)
data class TaskParentWithSubtasks(val taskParent: TaskParent, val subtasks: List<Subtask> = emptyList())