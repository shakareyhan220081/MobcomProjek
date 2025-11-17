package com.example.mobcomprojek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape


// --- Impor yang Diperlukan ---
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin // <-- BARU: Untuk ikon pin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // <-- Untuk TextField transparan
import androidx.compose.ui.text.font.FontWeight // <-- Untuk style Placeholder
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
// --- IMPORT BARU ---
import com.example.mobcomprojek.data.NoteRepository
import com.example.mobcomprojek.ui.theme.MobcomProjekTheme
// ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    noteId: String? // Menerima ID sebagai String ("new", "10", dll)
) {
    // === STATE ===
    val isNewNote = noteId == "new"

    val note = remember(noteId) {
        if (isNewNote || noteId == null) {
            null // Catatan baru
        } else {
            NoteRepository.getNoteById(noteId.toInt())
        }
    }

    // State untuk field input
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    // State untuk Kategori
    val allCategories = NoteRepository.categories
    var selectedCategoryId by remember { mutableStateOf(note?.categoryId ?: 0) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // --- BARU: State untuk Pin ---
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }

    // --- BARU: State untuk dialog "+ Tag" ---
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    // ---

    // --- BARU: Fungsi untuk konfirmasi dialog "+ Tag" ---
    val onAddCategoryConfirm = {
        if (newCategoryName.isNotBlank()) {
            val newCategory = NoteRepository.addCategory(newCategoryName)
            selectedCategoryId = newCategory.id // Otomatis pilih kategori baru
            showNewCategoryDialog = false
            newCategoryName = ""
        }
    }


    Scaffold(
        modifier = modifier,
        // === 1. TOP BAR ===
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (isNewNote) "New Note" else "Edit Note")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // --- PERBAIKAN: Tombol Pin ---
                    if (!isNewNote) { // Hanya tampilkan pin jika note sudah ada
                        IconButton(onClick = {
                            note?.id?.let { NoteRepository.togglePinStatus(it) }
                            isPinned = !isPinned // Update state lokal
                        }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin Note",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { /* TODO: Tampilkan menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu Lainnya"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },

        // === 2. BOTTOM BAR (DIPERBARUI) ===
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            // --- LOGIKA SIMPAN ---
                            if (isNewNote) {
                                NoteRepository.addNote(title, content, selectedCategoryId)
                            } else {
                                note?.id?.let {
                                    NoteRepository.updateNote(it, title, content, selectedCategoryId)
                                }
                            }
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Simpan Catatan")
                    }
                },
                // <-- PERBAIKAN: 'actions' dikosongkan sesuai permintaan
                actions = {
                    // Tidak ada aksi di sini
                }
            )
        },



        ) { innerPadding ->
        // === 4. KONTEN UTAMA ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // --- Input Judul (DIPERBARUI) ---
            TextField(
                value = title,
                onValueChange = { title = it },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                placeholder = {
                    Text(
                        "Title",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // --- Toolbar 1 (Chips/Tag) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- KATEGORI CHIP (DENGAN LOGIKA) ---
                Box {
                    val categoryName = NoteRepository.getCategoryById(selectedCategoryId)?.name ?: "Recent"
                    AssistChip(
                        onClick = { categoryMenuExpanded = true },
                        label = { Text(categoryName) }
                    )

                    // Dropdown untuk memilih kategori
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        // Opsi "Recent" (Tanpa kategori)
                        DropdownMenuItem(
                            text = { Text("Recent") },
                            onClick = {
                                selectedCategoryId = 0
                                categoryMenuExpanded = false
                            }
                        )
                        // Opsi dari daftar kategori
                        allCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // --- PERBAIKAN BUG 2 ---
                AssistChip(
                    onClick = {
                        newCategoryName = "" // Kosongkan field
                        showNewCategoryDialog = true // Tampilkan dialog
                    },
                    label = { Text("+ Tag") }
                )
            }

            // Pemisah (DIPERBARUI)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // --- Input Konten Catatan (DIPERBARUI) ---
            TextField(
                value = content,
                onValueChange = { content = it },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        "Start writing your note...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 300.dp) // Beri area minimum
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Area Reminder ---
            Text(
                text = "Reminder",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { /* TODO: Set Reminder */ },
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No reminder set",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Spacer di akhir agar tidak terpotong FAB
            Spacer(modifier = Modifier.height(80.dp))
        } // --- End Column ---

        // --- BARU: Dialog untuk "+ Tag" ---
        if (showNewCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showNewCategoryDialog = false },
                title = { Text("New Category") },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onAddCategoryConfirm() }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewCategoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    } // --- End Scaffold ---
}


// --- PREVIEW ---
@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun NoteDetailScreenPreviewLight() {
    MobcomProjekTheme(darkTheme = false) {
        // Preview untuk mode "Edit"
        NoteDetailScreen(navController = rememberNavController(), noteId = "1")
    }
}

@Preview(name = "Dark Mode (New Note)", showBackground = true)
@Composable
private fun NoteDetailScreenPreviewDark() {
    MobcomProjekTheme(darkTheme = true) {
        // Preview untuk mode "New Note"
        NoteDetailScreen(navController = rememberNavController(), noteId = "new")
    }
}