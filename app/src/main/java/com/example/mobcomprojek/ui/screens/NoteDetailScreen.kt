package com.example.mobcomprojek.ui.screens

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobcomprojek.viewmodel.NoteDetailViewModel
//import com.example.mobcomprojek.data.api.WeatherRepository
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    noteId: String?,
    viewModel: NoteDetailViewModel = viewModel()
) {
    val isNewNote = noteId == "new" || noteId == null

    // 1. Load Data
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    // 2. Ambil state note
    val noteData by viewModel.noteState.collectAsStateWithLifecycle()

    // 3. State Lokal
    var title by remember(noteData) { mutableStateOf(noteData?.title ?: "") }
    var content by remember(noteData) { mutableStateOf(noteData?.content ?: "") }
    var isPinned by remember(noteData) { mutableStateOf(noteData?.isPinned ?: false) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedDueDate by remember(noteData) { mutableStateOf(noteData?.dueDate) }
    var selectedReminderTime by remember(noteData) { mutableStateOf(noteData?.reminderTime) }
    var selectedPriority by remember(noteData) { mutableStateOf(noteData?.priority ?: "None") }

    val isDataLoaded = isNewNote || noteData != null

    // State Dialog
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState()
    val datePickerState = rememberDatePickerState()
    val timeFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val todayDateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val todayDate = remember { todayDateFormatter.format(Date()) }

    // === DATA CUACA (QoL Feature) ===
//    val weatherIcon = WeatherRepository.getWeatherIcon(WeatherRepository.currentWeatherCode)
//    val currentTemp = WeatherRepository.currentTemp

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // === INTEGRASI CUACA (SEBELAH PIN) ===
//                    if (currentTemp != null) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.padding(end = 12.dp)
//                        ) {
//                            Text(weatherIcon, style = MaterialTheme.typography.titleMedium)
//                            Spacer(Modifier.width(4.dp))
//                            Text(
//                                "${currentTemp.toInt()}°",
//                                style = MaterialTheme.typography.labelMedium,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }

                    // Ikon Pin
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tombol Hapus (Hanya jika edit mode)
                    if (!isNewNote) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        viewModel.saveNote(
                            currentId = noteId,
                            title = title,
                            content = content,
                            isPinned = isPinned,
                            categoryId = selectedCategoryId,
                            reminderTime = selectedReminderTime,
                            dueDate = selectedDueDate,
                            priority = selectedPriority
                        )
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Done, "Simpan")
                    }
                },
                actions = {}
            )
        }
    ) { innerPadding ->
        if (!isDataLoaded && !isNewNote) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // INPUT JUDUL
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    placeholder = { Text("Title", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // TANGGAL HARI INI
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(text = todayDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // INPUT KONTEN
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text("Start writing your note...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // SETTINGS UI
                Text("Settings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

                // 1. Reminder Card
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Reminder", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = selectedReminderTime?.let { timeFormatter.format(it) } ?: "Not Set",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Auto H-1 Reminder Logic
                LaunchedEffect(selectedDueDate) {
                    if (selectedDueDate != null && selectedReminderTime == null) {
                        val oneDayInMillis = 24 * 60 * 60 * 1000
                        selectedReminderTime = selectedDueDate!! - oneDayInMillis
                    }
                }

                // 2. Due Date Card
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Due Date", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = selectedDueDate?.let { dateFormatter.format(Date(it)) } ?: "Not Set",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // 3. Priority Card
                Box {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable { showPriorityMenu = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Priority", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = selectedPriority,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    DropdownMenu(expanded = showPriorityMenu, onDismissRequest = { showPriorityMenu = false }) {
                        listOf("High", "Medium", "Low", "None").forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { selectedPriority = p; showPriorityMenu = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- DIALOGS ---

    // Ganti nama composable agar tidak bentrok (opsional, tapi aman)
    if (showTimePicker) {
        MyTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    selectedReminderTime = cal.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            }
        ) { TimePicker(state = timePickerState) }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(noteId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

// Reuse MyTimePickerDialog dari TaskDetailScreen (atau pindahkan ke SharedComponents)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.wrapContentWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    confirmButton()
                }
            }
        }
    }
}