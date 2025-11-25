package com.example.mobcomprojek.ui.screens

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobcomprojek.data.Subtask
import com.example.mobcomprojek.viewmodel.TaskDetailViewModel
import com.example.mobcomprojek.viewmodel.TaskDetailViewModelFactory
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: TaskDetailViewModel // Terima ViewModel
) {
    // === STATE ===
    // 1. Ambil state dari ViewModel
    val taskWithSubtasks by viewModel.taskState.collectAsStateWithLifecycle()

    // 2. State lokal untuk UI, di-reset saat data DB berubah
    var title by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.taskParent?.title ?: "")
    }
    var subtasks by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.subtasks ?: emptyList())
    }
    // Simpan list asli untuk perbandingan saat save
    val originalSubtasks = remember(taskWithSubtasks) {
        taskWithSubtasks?.subtasks ?: emptyList()
    }

    var newSubtaskTitle by remember { mutableStateOf("") }

    var selectedDueDate by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.taskParent?.dueDate)
    }
    var selectedReminderTime by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.taskParent?.reminderTime?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        })
    }
    var selectedPriority by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.taskParent?.priority ?: "None")
    }

    // State untuk dialog
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDueDate ?: System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedReminderTime?.get(Calendar.HOUR_OF_DAY) ?: 12,
        initialMinute = selectedReminderTime?.get(Calendar.MINUTE) ?: 0
    )

    // Formatters
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // === LOGIC (Hanya modifikasi state LOKAL) ===

    val onSubtaskCheckedChange = { subtask: Subtask, isChecked: Boolean ->
        subtasks = subtasks.map {
            if (it.id == subtask.id) it.copy(isCompleted = isChecked) else it
        }
    }

    val onSubtaskTitleChange = { subtask: Subtask, newTitle: String ->
        subtasks = subtasks.map {
            if (it.id == subtask.id) it.copy(title = newTitle) else it
        }
    }

    val onDeleteSubtask = { subtask: Subtask ->
        subtasks = subtasks.filterNot { it.id == subtask.id }
    }

    val onAddNewSubtask = {
        if (newSubtaskTitle.isNotBlank()) {
            val newSub = Subtask(
                id = 0, // ID 0 (auto-generate by Room)
                taskParentId = taskWithSubtasks?.taskParent?.id ?: 0,
                title = newSubtaskTitle,
                isCompleted = false
            )
            subtasks = subtasks + newSub
            newSubtaskTitle = ""
        }
    }

    val onSaveChanges: () -> Unit = {
        // Panggil ViewModel untuk menyimpan
        viewModel.saveChanges(
            title = title,
            dueDate = selectedDueDate,
            reminderTime = selectedReminderTime?.timeInMillis,
            priority = selectedPriority,
            currentSubtasks = subtasks,
            originalSubtasks = originalSubtasks
        )
        navController.popBackStack()
    }

    // === UI ===

    // Tampilkan loading jika data belum siap
    if (taskWithSubtasks == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Data sudah siap, tampilkan UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (taskWithSubtasks?.taskParent?.id == 0) "New Task" else "Edit Task") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onSaveChanges) {
                            Icon(Icons.Default.Done, contentDescription = "Save Changes")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->

            // --- DIALOGS ---
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
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker) {
                TimePickerDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            selectedReminderTime = cal
                            showTimePicker = false
                            // TODO: Implementasi REAL reminder menggunakan AlarmManager
                        }) { Text("OK") }
                    }
                ) {
                    TimePicker(state = timePickerState)
                }
            }

            // --- KONTEN UTAMA ---
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // JUDUL TASK
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Task Title") },
                        singleLine = true
                    )
                }

                // TAMPILAN NILAI TERPILIH
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Due Date", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = selectedDueDate?.let { dateFormatter.format(Date(it)) } ?: "Not Set",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reminder", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = selectedReminderTime?.let { timeFormatter.format(it.time) } ?: "Not Set",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Priority", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = selectedPriority,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // TOMBOL AKSI
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Set Reminder")
                        }
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Set Due Date")
                        }
                        Box {
                            Button(
                                onClick = { showPriorityMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Set Priority")
                            }
                            DropdownMenu(
                                expanded = showPriorityMenu,
                                onDismissRequest = { showPriorityMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("High") },
                                    onClick = { selectedPriority = "High"; showPriorityMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Medium") },
                                    onClick = { selectedPriority = "Medium"; showPriorityMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Low") },
                                    onClick = { selectedPriority = "Low"; showPriorityMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = { selectedPriority = "None"; showPriorityMenu = false }
                                )
                            }
                        }
                    }
                }

                // --- SUBTASKS ---
                item {
                    Divider(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Subtasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(subtasks) { subtask ->
                    EditSubtaskItem(
                        subtask = subtask,
                        onCheckedChange = { isChecked -> onSubtaskCheckedChange(subtask, isChecked) },
                        onTitleChange = { newTitle -> onSubtaskTitleChange(subtask, newTitle) },
                        onDelete = { onDeleteSubtask(subtask) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Add new subtask...") },
                        trailingIcon = {
                            IconButton(onClick = onAddNewSubtask) {
                                Icon(Icons.Default.Add, contentDescription = "Add Subtask")
                            }
                        },
                        singleLine = true
                    )
                }
            }
        }
    }
}

// Composable EditSubtaskItem (Tidak diubah)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubtaskItem(
    subtask: Subtask,
    onCheckedChange: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = subtask.isCompleted,
            onCheckedChange = onCheckedChange
        )
        TextField(
            value = subtask.title,
            onValueChange = onTitleChange,
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete Subtask",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Composable TimePickerDialog (Helper)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
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

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenPreview() {
    TaskDetailScreen(navController = rememberNavController(), viewModel = viewModel())
}