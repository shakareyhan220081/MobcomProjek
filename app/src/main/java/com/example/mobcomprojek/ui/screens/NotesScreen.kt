package com.example.mobcomprojek.ui.screens

import androidx.compose.foundation.BorderStroke // Import yang benar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobcomprojek.ui.components.SectionHeader
import com.example.mobcomprojek.ui.navigation.Screen
import com.example.mobcomprojek.ui.theme.MobcomProjekTheme
// import java.util.Locale // <-- Impor yang tidak terpakai dihapus

// Data class sementara untuk "isi"
data class NoteItem(val id: Int, val title: String, val content: String)

// Data dipindahkan ke dalam Composable sebagai state

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    // === STATE ===
    var searchQuery by remember { mutableStateOf("") }

    // State "diangkat" ke sini agar bisa diubah
    var recentNotes by remember {
        mutableStateOf(List(6) { NoteItem(it, "Note Title ${it + 1}", "Content preview...") })
    }
    var categories by remember {
        mutableStateOf(listOf(
            "Category 1" to listOf(NoteItem(10, "Rapat Proyek", ""), NoteItem(11, "Revisi UI", "")),
            "Category 2" to listOf(NoteItem(12, "Bahan Masak", ""), NoteItem(13, "Link Penting", ""))
        ))
    }

    // === LOGIC (FILTERING) ===
    // Filter sekarang menggunakan state 'recentNotes'
    val filteredRecentNotes = remember(searchQuery, recentNotes) {
        if (searchQuery.isBlank()) {
            recentNotes
        } else {
            recentNotes.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Filter sekarang menggunakan state 'categories'
    val filteredCategories = remember(searchQuery, categories) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories
                .mapNotNull { (category, notes) ->
                    val filteredNotes = notes.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    if (filteredNotes.isNotEmpty()) category to filteredNotes else null
                }
        }
    }

    // Fungsi untuk memodifikasi state
    val onDeleteCategory = { categoryTitle: String ->
        // Buat list baru tanpa kategori yang dihapus
        categories = categories.filterNot { (title, _) -> title == categoryTitle }
    }

    val onRenameCategory = { oldTitle: String ->
        // TODO: Tampilkan dialog untuk mendapatkan nama baru
        val newTitle = "676767" // Contoh
        categories = categories.map { (title, notes) ->
            if (title == oldTitle) newTitle to notes else title to notes
        }
    }

    Scaffold(
        topBar = {
            // === Top App Bar (dengan Search Bar Fungsional) ===
            TopAppBar(
                title = {
                    // 1. FITUR SEARCH BAR
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp), // .height(50.dp) DIHAPUS
                        placeholder = { Text("Search notes by title...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true,
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Ganti View */ }) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = "Ganti Tampilan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        // === Konten Utama (Menggunakan LazyColumn) ===
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {

            // --- Bagian "Recent Notes" (Grid) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Recent Notes", showSeeAll = false)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Render 3 item per baris
            items(filteredRecentNotes.chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (note in rowItems) {
                        NoteGridItem(note, Modifier.weight(1f)) {
                            navController.navigate(Screen.NoteDetail.route + "/${note.id}")
                        }
                    }
                    // Jika baris tidak penuh (misal sisa 1 atau 2), tambahkan Spacer
                    repeat(3 - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Bagian Kategori (List) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(title = "Categories", showSeeAll = false)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(filteredCategories) { (categoryTitle, notes) ->
                CategorySection(
                    title = categoryTitle,
                    notes = notes,
                    onNoteClick = { note ->
                        navController.navigate(Screen.NoteDetail.route + "/${note.id}")
                    },
                    // Kirim fungsi aksi ke Composable
                    onRenameClick = { onRenameCategory(categoryTitle) },
                    onDeleteClick = { onDeleteCategory(categoryTitle) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Composable untuk 1 item di dalam Grid "Recent Notes"
 * 4. TAMPILAN "RING" (Menggunakan OutlinedCard)
 */
@Composable
fun NoteGridItem(note: NoteItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Menggunakan OutlinedCard untuk efek "ring"
    OutlinedCard(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline) // Ini "ring" nya
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Composable untuk 1 bagian Kategori
 */
@Composable
fun CategorySection(
    title: String,
    notes: List<NoteItem>,
    modifier: Modifier = Modifier,
    onNoteClick: (NoteItem) -> Unit,
    onRenameClick: () -> Unit, // Terima lambda
    onDeleteClick: () -> Unit  // Terima lambda
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // === Header Kategori ===
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.weight(1f)) // Pendorong

            // 3. FITUR AKSI KATEGORI (Delete, Rename, dll)
            Box {
                IconButton(onClick = { categoryMenuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opsi Kategori",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    // Panggil lambda saat diklik
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onRenameClick()
                            categoryMenuExpanded = false // Tutup menu
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick()
                            categoryMenuExpanded = false // Tutup menu
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // === Daftar notes di Kategori ===
        Column {
            notes.forEach { note ->
                ListItem(
                    headlineContent = {
                        Text(note.title, fontWeight = FontWeight.SemiBold)
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    // 2. FITUR AKSI NOTE (Edit, Delete)
                    trailingContent = {
                        Row {
                            IconButton(onClick = { /* TODO: Edit Note */ }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { /* TODO: Delete Note */ }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNoteClick(note) }
                )
            }
        }
    }
}


// --- PREVIEW ---

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun NotesScreenPreviewLight() {
    MobcomProjekTheme(darkTheme = false) {
        NotesScreen(navController = rememberNavController())
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun NotesScreenPreviewDark() {
    MobcomProjekTheme(darkTheme = true) {
        NotesScreen(navController = rememberNavController())
    }
}