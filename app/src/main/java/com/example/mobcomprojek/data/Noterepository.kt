package com.example.mobcomprojek.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// 1. Definisikan Data Class kita
data class Category(
    val id: Int,
    var name: String // 'var' agar bisa di-rename
)

data class NoteItem(
    val id: Int,
    var title: String,
    var content: String,
    var categoryId: Int,
    var isPinned: Boolean = false // <-- BARU: Tambahkan properti pin
)

// 2. Buat "database" palsu (in-memory) sebagai Singleton
object NoteRepository {

    // Data Kategori Awal
    private val initialCategories = listOf(
        Category(1, "Category 1"),
        Category(2, "Category 2")
    )

    // Data Catatan Awal
    private val initialNotes = listOf(
        // Catatan "Recent" (kita buat saja Kategori 0 = "Recent")
        NoteItem(1, "Note Title 1", "Content preview...", 0, isPinned = true), // <-- BARU: Satu note di-pin
        NoteItem(2, "Note Title 2", "Content preview...", 0),
        NoteItem(3, "Note Title 3", "Content preview...", 0),
        NoteItem(4, "Note Title 4", "Content preview...", 0),
        NoteItem(5, "Note Title 5", "Content preview...", 0),
        NoteItem(6, "Note Title 6", "Content preview...", 0),
        // Catatan Kategori 1
        NoteItem(10, "Rapat Proyek", "", 1),
        NoteItem(11, "Revisi UI", "", 1),
        // Catatan Kategori 2
        NoteItem(12, "Bahan Masak", "", 2),
        NoteItem(13, "Link Penting", "", 2)
    )

    // 3. Gunakan 'mutableStateListOf' agar Compose bisa "melihat" perubahan
    val categories: SnapshotStateList<Category> = mutableStateListOf(*initialCategories.toTypedArray())
    val notes: SnapshotStateList<NoteItem> = mutableStateListOf(*initialNotes.toTypedArray())

    // --- VARIABEL ID BARU ---
    private var nextNoteId = notes.maxOfOrNull { it.id }?.plus(1) ?: 14
    private var nextCategoryId = categories.maxOfOrNull { it.id }?.plus(1) ?: 3 // <-- BARU

    // 4. Fungsi untuk berinteraksi dengan data

    fun getNoteById(id: Int): NoteItem? {
        return notes.find { it.id == id }
    }

    fun getCategoryById(id: Int): Category? {
        return categories.find { it.id == id }
    }

    // --- BARU: Fungsi untuk mengambil note yang di-pin ---
    fun getPinnedNotes(): List<NoteItem> {
        // Ambil semua note, urutkan yang di-pin ke atas
        return notes.filter { it.isPinned }
    }

    // Mendapatkan catatan "Recent"
    fun getRecentNotes(): List<NoteItem> {
        // "Recent" adalah 6 catatan teratas yang TIDAK di-pin
        return notes.filter { !it.isPinned }.take(6)
    }

    // Mendapatkan semua kategori
    fun getCategorizedNotes(): List<Pair<Category, List<NoteItem>>> {
        return categories.map { category ->
            // Catatan di kategori TIDAK termasuk yang di-pin
            val notesForCategory = notes.filter { !it.isPinned && it.categoryId == category.id }
            category to notesForCategory
        }
    }

    fun addNote(title: String, content: String, categoryId: Int) {
        val newNote = NoteItem(
            id = nextNoteId++,
            title = if (title.isBlank()) "Untitled Note" else title, // Judul default
            content = content,
            categoryId = categoryId,
            isPinned = false // Note baru tidak otomatis di-pin
        )
        // Tambah di awal list agar langsung terlihat
        notes.add(0, newNote)
    }

    fun updateNote(noteId: Int, newTitle: String, newContent: String, newCategoryId: Int) {
        val note = getNoteById(noteId)
        note?.let {
            it.title = if (newTitle.isBlank()) "Untitled Note" else newTitle
            it.content = newContent
            it.categoryId = newCategoryId
            // Ganti posisi note di list agar memicu update
            val index = notes.indexOf(it)
            if (index != -1) {
                notes.removeAt(index)
                notes.add(index, it)
            }
        }
    }

    // --- PERBAIKAN BUG RENAME ---
    fun renameCategory(categoryId: Int, newName: String) {
        val category = getCategoryById(categoryId)
        category?.let {
            it.name = newName
            // Ganti posisi kategori di list agar memicu update
            val index = categories.indexOf(it)
            if (index != -1) {
                categories.removeAt(index)
                categories.add(index, it)
            }
        }
    }
    // --- AKHIR PERBAIKAN ---

    fun deleteCategory(categoryId: Int) {
        // Hapus kategori
        categories.removeAll { it.id == categoryId }
        // Hapus catatan di dalam kategori tsb (atau pindahkan ke "Recent" / 0)
        notes.forEach {
            if (it.categoryId == categoryId) {
                it.categoryId = 0 // Pindahkan ke recent
            }
        }
    }

    fun deleteNote(note: NoteItem) {
        notes.remove(note)
    }

    // --- BARU: Fungsi untuk menambah Kategori baru ---
    fun addCategory(name: String): Category {
        val newCategory = Category(id = nextCategoryId++, name = name)
        categories.add(newCategory)
        return newCategory // Return new category so it can be selected
    }

    // --- BARU: Fungsi untuk mengubah status Pin ---
    fun togglePinStatus(noteId: Int) {
        val note = getNoteById(noteId)
        note?.let {
            it.isPinned = !it.isPinned
            // Paksa update UI
            val index = notes.indexOf(it)
            if (index != -1) {
                notes.removeAt(index)
                notes.add(index, it)
            }
        }
    }
}