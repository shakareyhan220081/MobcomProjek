package com.example.mobcomprojek.ui.screens

// --- IMPORT BARU ---
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// --- IMPORT DIPERBARUI ---
import androidx.compose.material.icons.automirrored.filled.ArrowRight // <-- DIPERBARUI
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
// --- IMPORT BARU ---
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobcomprojek.data.NoteItem
import com.example.mobcomprojek.data.NoteRepository
import com.example.mobcomprojek.ui.components.SectionHeader
import com.example.mobcomprojek.ui.navigation.Screen
import com.example.mobcomprojek.ui.theme.MobcomProjekTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    // === STATE ===
    var searchQuery by remember { mutableStateOf("") }

    val allNotes = NoteRepository.notes
    val allCategories = NoteRepository.categories

    var showRenameDialog by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<com.example.mobcomprojek.data.Category?>(null) }
    var newCategoryName by remember { mutableStateOf("") }

    // --- PERBAIKAN: State untuk expand/collapse diangkat ke sini ---
    // Kita akan menyimpan ID dari kategori yang TERTUTUP (collapsed)
    var collapsedCategoryIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    // ---

    // === LOGIC (FILTERING) ===

    // (Logika filter tidak berubah)
    val filteredPinnedNotes = run {
        val notes = allNotes.filter { it.isPinned }
        if (searchQuery.isBlank()) {
            notes
        } else {
            notes.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredRecentNotes = run {
        val notes = allNotes.filter { !it.isPinned }.take(6)
        if (searchQuery.isBlank()) {
            notes
        } else {
            notes.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredCategories = run {
        val catsWithNotes = allCategories.map { category ->
            val notesForCategory = allNotes.filter { !it.isPinned && it.categoryId == category.id }
            category to notesForCategory
        }

        if (searchQuery.isBlank()) {
            catsWithNotes
        } else {
            catsWithNotes
                .mapNotNull { (category, notes) ->
                    val filteredNotes = notes.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    if (filteredNotes.isNotEmpty()) category to filteredNotes else null
                }
        }
    }

    // === FUNGSI AKSI ===

    // --- BARU: Fungsi untuk toggle expand/collapse ---
    val onToggleCategoryExpansion = { categoryId: Int ->
        collapsedCategoryIds = if (categoryId in collapsedCategoryIds) {
            collapsedCategoryIds - categoryId // Buka
        } else {
            collapsedCategoryIds + categoryId // Tutup
        }
    }
    // ---

    // (Fungsi aksi lainnya tidak berubah)
    val onDeleteCategory = { categoryId: Int ->
        NoteRepository.deleteCategory(categoryId)
    }

    val onRenameTrigger = { category: com.example.mobcomprojek.data.Category ->
        categoryToRename = category
        newCategoryName = category.name
        showRenameDialog = true
    }

    val onDismissDialog = {
        showRenameDialog = false
        categoryToRename = null
        newCategoryName = ""
    }

    val onRenameConfirm = {
        categoryToRename?.let {
            if (newCategoryName.isNotBlank()) {
                NoteRepository.renameCategory(it.id, newCategoryName)
            }
        }
        onDismissDialog()
    }

    val onDeleteNote = { note: NoteItem ->
        NoteRepository.deleteNote(note)
    }

    // --- UI Dimulai ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
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
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {

            // (Bagian Pinned Notes tidak berubah)
            if (filteredPinnedNotes.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Pinned", showSeeAll = false)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                items(filteredPinnedNotes.chunked(3)) { rowItems ->
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (note in rowItems) {
                            NoteGridItem(note, Modifier.weight(1f)) {
                                navController.navigate(Screen.NoteDetail.route + "/${note.id}")
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // (Bagian Recent Notes tidak berubah)
            if (filteredRecentNotes.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Recent Notes", showSeeAll = false)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
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
                        repeat(3 - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- Bagian Kategori (List) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(title = "Categories", showSeeAll = false)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(filteredCategories) { (category, notes) ->
                if (notes.isNotEmpty()) {
                    // --- PERBAIKAN: Kirim state & event ke CategorySection ---
                    val isExpanded = category.id !in collapsedCategoryIds
                    CategorySection(
                        title = category.name,
                        notes = notes,
                        isExpanded = isExpanded, // <-- Kirim state
                        onToggleExpansion = { onToggleCategoryExpansion(category.id) }, // <-- Kirim event
                        onNoteClick = { note ->
                            navController.navigate(Screen.NoteDetail.route + "/${note.id}")
                        },
                        onRenameClick = { onRenameTrigger(category) },
                        onDeleteClick = { onDeleteCategory(category.id) },
                        onDeleteNoteClick = onDeleteNote,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        } // End LazyColumn

        // (Dialog tidak berubah)
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { onDismissDialog() },
                title = { Text("Rename Category") },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New category name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onRenameConfirm() }) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onDismissDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        // ---

    } // End Scaffold
}


/**
 * Composable untuk 1 item di dalam Grid "Recent Notes"
 */
@Composable
fun NoteGridItem(note: NoteItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
    isExpanded: Boolean, // <-- BARU: Terima state
    onToggleExpansion: () -> Unit, // <-- BARU: Terima event
    onNoteClick: (NoteItem) -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteNoteClick: (NoteItem) -> Unit
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // --- DIHAPUS: State lokal dipindahkan ---
    // var isExpanded by remember { mutableStateOf(true) }
    // ---

    // --- Animasi untuk rotasi ikon ---
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f, // <-- Gunakan parameter
        label = "rotation"
    )
    // ---

    Column(modifier = modifier.fillMaxWidth()) {
        // === Header Kategori ===
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // --- Klik baris ini untuk expand/collapse ---
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpansion() } // <-- Gunakan parameter
                .padding(vertical = 4.dp)
        ) {
            // --- Ikon Expand/Collapse ---
            IconButton(
                onClick = { onToggleExpansion() }, // <-- Gunakan parameter
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // ---

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.weight(1f)) // Pendorong

            // (Box Aksi Kategori tidak berubah)
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
        // --- Gunakan AnimatedVisibility ---
        AnimatedVisibility(visible = isExpanded) { // <-- Gunakan parameter
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
                        trailingContent = {
                            Row {
                                IconButton(onClick = { onNoteClick(note) }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteNoteClick(note) }) {
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
        // ---
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