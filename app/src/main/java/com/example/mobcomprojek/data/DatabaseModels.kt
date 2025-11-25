package com.example.mobcomprojek.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

// 1. Kategori (Paling atas)
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String
)

// 2. TaskParent (Anak dari Kategori)
@Entity(
    tableName = "task_parents",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Jika Kategori dihapus, Task ikut terhapus
        )
    ]
)
data class TaskParent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int, // Kunci asing ke Kategori
    val title: String,
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val priority: String = "None"
)

// 3. Subtask (Anak dari TaskParent)
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskParent::class,
            parentColumns = ["id"],
            childColumns = ["taskParentId"],
            onDelete = ForeignKey.CASCADE // Jika TaskParent dihapus, Subtask ikut terhapus
        )
    ]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskParentId: Int, // Kunci asing ke TaskParent
    val title: String,
    val isCompleted: Boolean
)

// --- RELATIONAL CLASSES (Untuk Mengambil Data) ---

// Menggabungkan TaskParent dengan list Subtask-nya
data class TaskParentWithSubtasks(
    @Embedded
    val taskParent: TaskParent,

    @Relation(
        parentColumn = "id", // Dari TaskParent.id
        entityColumn = "taskParentId" // Dari Subtask.taskParentId
    )
    val subtasks: List<Subtask>
)

// Menggabungkan Kategori dengan list Task-nya
data class CategoryWithTasks(
    @Embedded
    val category: Category,

    @Relation(
        entity = TaskParent::class, // Kita butuh perantara
        parentColumn = "id", // Dari Category.id
        entityColumn = "categoryId" // Dari TaskParent.categoryId
    )
    val tasks: List<TaskParentWithSubtasks> // List of TaskParent...with their subtasks
)