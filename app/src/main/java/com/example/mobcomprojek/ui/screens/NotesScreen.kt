package com.example.mobcomprojek.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobcomprojek.data.NoteItem
import com.example.mobcomprojek.ui.components.SectionHeader
import com.example.mobcomprojek.ui.navigation.Screen
import com.example.mobcomprojek.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: NotesViewModel = viewModel()
) {
    // === STATE DATA ===
    val allNotes by viewModel.notesState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // === LOGIC (FILTERING & SPLITTING) ===

    // 1. Filter Pinned vs Unpinned (berdasarkan Search)
    val (pinnedNotes, unpinnedNotes) = remember(allNotes, searchQuery) {
        val filtered = if (searchQuery.isBlank()) allNotes else allNotes.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true)
        }
        filtered.partition { it.isPinned }
    }

    // 2. Split Unpinned menjadi Recent (4 teratas) & All Notes (Sisanya)
    val recentNotes = remember(unpinnedNotes) { unpinnedNotes.take(4) }
    val otherNotes = remember(unpinnedNotes) { unpinnedNotes.drop(4) }

    // --- UI Dimulai ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search notes...") },
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
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            // 1. PINNED NOTES SECTION
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "Pinned", showSeeAll = false)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                items(pinnedNotes.chunked(2)) { rowItems ->
                    NotesRow(rowItems, navController)
                }
            }

            // 2. RECENT NOTES SECTION (4 Teratas)
            if (recentNotes.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionHeader(title = "Recent Notes", showSeeAll = false)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                items(recentNotes.chunked(2)) { rowItems ->
                    NotesRow(rowItems, navController)
                }
            }

            // 3. ALL NOTES SECTION (Sisanya)
            if (otherNotes.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionHeader(title = "All Notes", showSeeAll = false)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                items(otherNotes.chunked(2)) { rowItems ->
                    NotesRow(rowItems, navController)
                }
            }

            // Empty State
            if (pinnedNotes.isEmpty() && unpinnedNotes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text("No notes found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// === COMPONENT HELPERS ===

@Composable
fun NotesRow(notes: List<NoteItem>, navController: NavController) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (note in notes) {
            NoteGridItem(
                note = note,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.NoteDetail.route + "/${note.id}") }
            )
        }
        if (notes.size == 1) Spacer(Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun NoteGridItem(note: NoteItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedCard(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Column {
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

            // Indikator Pin Visual
            if (note.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .rotate(45f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}