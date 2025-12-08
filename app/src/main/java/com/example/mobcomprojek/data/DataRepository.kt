package com.example.mobcomprojek.data

import android.content.Context
import android.util.Log
import com.example.mobcomprojek.data.local.AppDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DataRepository(context: Context) {

    // 1. Inisialisasi Database Lokal & Firebase
    private val dbLocal = AppDatabase.getDatabase(context)
    private val dao = dbLocal.appDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // ==========================================
    // BAGIAN 1: NOTES
    // ==========================================

    fun getNotesStream(): Flow<List<NoteItem>> = dao.getAllNotes()

    suspend fun addNote(
        title: String, content: String, isPinned: Boolean,
        categoryId: String, reminderTime: Long?,
        dueDate: Long?, priority: String
    ) {
        val newId = UUID.randomUUID().toString()
        val note = NoteItem(
            id = newId, userId = currentUserId,
            title = title, content = content, categoryId = categoryId,
            isPinned = isPinned, reminderTime = reminderTime,
            dueDate = dueDate, priority = priority,
            isSynced = false // Belum sync
        )
        // 1. Simpan Lokal (Cepat)
        dao.insertNote(note)
        // 2. Coba Upload (Background)
        tryPushNoteToFirebase(note)
    }

    suspend fun updateNote(note: NoteItem) {
        val noteToUpdate = note.copy(isSynced = false)
        dao.insertNote(noteToUpdate)
        tryPushNoteToFirebase(noteToUpdate)
    }

    suspend fun deleteNote(noteId: String) {
        dao.deleteNoteById(noteId)
        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("notes").document(noteId).delete()
            } catch (e: Exception) { /* Offline: Ignore */ }
        }
    }

    // ==========================================
    // BAGIAN 2: CATEGORIES
    // ==========================================

    fun getCategoriesStream(type: String): Flow<List<Category>> = dao.getCategories(type)

    suspend fun addCategory(title: String, type: String) {
        val newId = UUID.randomUUID().toString()
        val cat = Category(id = newId, userId = currentUserId, title = title, type = type, isSynced = false)
        dao.insertCategory(cat)

        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("categories").document(newId).set(cat).await()
                dao.insertCategory(cat.copy(isSynced = true))
            } catch (e: Exception) { }
        }
    }

    suspend fun deleteCategory(id: String) {
        dao.deleteCategory(id)
        if (currentUserId.isNotEmpty()) {
            try { firestore.collection("categories").document(id).delete() } catch (e: Exception) { }
        }
    }

    suspend fun renameCategory(id: String, name: String) {
        dao.updateCategoryTitle(id, name) // Update lokal partial
        // Untuk sync rename, idealnya kita perlu object utuh, tapi kita bisa update field saja di firebase
        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("categories").document(id).update("title", name)
            } catch (e: Exception) { }
        }
    }

    // ==========================================
    // BAGIAN 3: TASKS & SUBTASKS
    // ==========================================

    fun getTasksStream(): Flow<List<TaskParent>> = dao.getAllTasks()

    fun getAllSubtasksStream(): Flow<List<Subtask>> = dao.getAllSubtasks()

    suspend fun addTask(categoryId: String, title: String) {
        val newId = UUID.randomUUID().toString()
        val task = TaskParent(
            id = newId, userId = currentUserId,
            categoryId = categoryId, title = title,
            isSynced = false
        )
        dao.insertTask(task)

        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("tasks").document(newId).set(task).await()
                dao.insertTask(task.copy(isSynced = true))
            } catch (e: Exception) { }
        }
    }

    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        dao.updateTaskStatus(taskId, isCompleted)
        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("tasks").document(taskId).update("isCompleted", isCompleted)
            } catch (e: Exception) { }
        }
    }

    suspend fun updateSubtaskStatus(subtaskId: String, isCompleted: Boolean) {
        dao.updateSubtaskStatus(subtaskId, isCompleted)
        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("subtasks").document(subtaskId).update("isCompleted", isCompleted)
            } catch (e: Exception) { }
        }
    }

    suspend fun saveTaskWithSubtasks(task: TaskParent, subtasks: List<Subtask>, deletedSubtaskIds: List<String>) {
        val finalTask = if(task.id.isEmpty()) task.copy(id = UUID.randomUUID().toString(), userId = currentUserId) else task.copy(isSynced = false)

        val finalSubtasks = subtasks.map { sub ->
            if(sub.id.isEmpty()) sub.copy(id = UUID.randomUUID().toString(), taskId = finalTask.id, isSynced = false)
            else sub.copy(taskId = finalTask.id, isSynced = false)
        }

        // 1. Simpan Lokal Transaction
        dao.saveTaskWithSubtasksTransaction(finalTask, finalSubtasks, deletedSubtaskIds)

        // 2. Coba Sync Firebase (Batch Write)
        if (currentUserId.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val batch = firestore.batch()
                    // Task
                    batch.set(firestore.collection("tasks").document(finalTask.id), finalTask)
                    // Subtasks
                    finalSubtasks.forEach {
                        batch.set(firestore.collection("subtasks").document(it.id), it)
                    }
                    // Deleted Subtasks
                    deletedSubtaskIds.forEach {
                        batch.delete(firestore.collection("subtasks").document(it))
                    }
                    batch.commit().await()

                    // Jika sukses, tandai synced true di lokal
                    dao.insertTask(finalTask.copy(isSynced = true))
                    finalSubtasks.forEach { dao.insertSubtask(it.copy(isSynced = true)) }

                } catch (e: Exception) {
                    Log.e("Sync", "Offline mode: Task saved locally.")
                }
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        dao.deleteTaskById(taskId)
        if (currentUserId.isNotEmpty()) {
            try {
                // Di Firebase idealnya pakai Cloud Functions untuk cascading delete,
                // tapi kita coba hapus parent dulu dari client.
                firestore.collection("tasks").document(taskId).delete()
            } catch (e: Exception) { }
        }
    }

    suspend fun toggleTaskAndSubtasksBatch(taskId: String, newStatus: Boolean, subtaskIds: List<String>) {
        // Update Local
        dao.updateTaskStatus(taskId, newStatus)
        subtaskIds.forEach { dao.updateSubtaskStatus(it, newStatus) }

        // Update Firebase
        if (currentUserId.isNotEmpty()) {
            try {
                val batch = firestore.batch()
                batch.update(firestore.collection("tasks").document(taskId), "isCompleted", newStatus)
                subtaskIds.forEach {
                    batch.update(firestore.collection("subtasks").document(it), "isCompleted", newStatus)
                }
                batch.commit()
            } catch (e: Exception) { }
        }
    }

    suspend fun updateTaskTitle(taskId: String, newTitle: String) {
        dao.updateTaskTitle(taskId, newTitle)
        if (currentUserId.isNotEmpty()) {
            try {
                firestore.collection("tasks").document(taskId).update("title", newTitle)
            } catch (e: Exception) { }
        }
    }

    // ==========================================
    // BAGIAN 4: SYNC WORKER HELPER
    // ==========================================

    private suspend fun tryPushNoteToFirebase(note: NoteItem) {
        if (currentUserId.isEmpty()) return
        try {
            firestore.collection("notes").document(note.id).set(note).await()
            dao.insertNote(note.copy(isSynced = true))
        } catch (e: Exception) {
            Log.d("Sync", "Offline mode: Note saved locally only.")
        }
    }

    // Dipanggil oleh SyncWorker saat internet kembali
    suspend fun syncAllData() {
        if (currentUserId.isEmpty()) return

        try {
            // === 1. PUSH: Upload data lokal yang pending (Dibuat saat offline) ===

            // A. Sync Categories
            val pendingCats = dao.getUnsyncedCategories()
            pendingCats.forEach { cat ->
                try {
                    firestore.collection("categories").document(cat.id).set(cat).await()
                    dao.insertCategory(cat.copy(isSynced = true))
                } catch (e: Exception) { Log.e("Sync", "Fail push cat ${cat.id}") }
            }

            // B. Sync Notes
            val pendingNotes = dao.getUnsyncedNotes()
            pendingNotes.forEach { note ->
                try {
                    firestore.collection("notes").document(note.id).set(note).await()
                    dao.insertNote(note.copy(isSynced = true))
                } catch (e: Exception) { Log.e("Sync", "Fail push note ${note.id}") }
            }

            // C. Sync Tasks
            val pendingTasks = dao.getUnsyncedTasks()
            pendingTasks.forEach { task ->
                try {
                    firestore.collection("tasks").document(task.id).set(task).await()
                    dao.insertTask(task.copy(isSynced = true))
                } catch (e: Exception) { Log.e("Sync", "Fail push task ${task.id}") }
            }

            // D. Sync Subtasks
            val pendingSubtasks = dao.getUnsyncedSubtasks()
            pendingSubtasks.forEach { sub ->
                try {
                    firestore.collection("subtasks").document(sub.id).set(sub).await()
                    dao.insertSubtask(sub.copy(isSynced = true))
                } catch (e: Exception) { Log.e("Sync", "Fail push subtask ${sub.id}") }
            }


            // === 2. PULL: Ambil data terbaru dari Firebase ===
            // (Strategi: Server Truth / Last Write Wins)

            // Notes
            val notesSnap = firestore.collection("notes").whereEqualTo("userId", currentUserId).get().await()
            val remoteNotes = notesSnap.toObjects(NoteItem::class.java)
            remoteNotes.forEach { dao.insertNote(it.copy(isSynced = true)) }

            // Tasks
            val tasksSnap = firestore.collection("tasks").whereEqualTo("userId", currentUserId).get().await()
            val remoteTasks = tasksSnap.toObjects(TaskParent::class.java)
            remoteTasks.forEach { dao.insertTask(it.copy(isSynced = true)) }

            // Categories
            val catSnap = firestore.collection("categories").whereEqualTo("userId", currentUserId).get().await()
            val remoteCats = catSnap.toObjects(Category::class.java)
            remoteCats.forEach { dao.insertCategory(it.copy(isSynced = true)) }

            // Subtasks
            val subSnap = firestore.collection("subtasks").get().await()
            // Filter manual karena di rule security mungkin kita batasi bacaan
            // (Untuk production sebaiknya query by taskId loop, tapi ini ok untuk MVP)
            val remoteSubs = subSnap.toObjects(Subtask::class.java)
            remoteSubs.forEach { dao.insertSubtask(it.copy(isSynced = true)) }

        } catch (e: Exception) {
            Log.e("Sync", "Sync failed: ${e.message}")
            throw e // Lempar error agar Worker tahu dan retry nanti
        }
    }
}