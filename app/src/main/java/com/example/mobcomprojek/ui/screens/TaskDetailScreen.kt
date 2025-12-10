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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobcomprojek.data.Subtask
import com.example.mobcomprojek.viewmodel.TaskDetailViewModel
//import com.example.mobcomprojek.data.api.WeatherRepository
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    taskId: String?,
    viewModel: TaskDetailViewModel = viewModel()
) {
    // 1. Load Data saat Layar Dibuka
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    // 2. Ambil state dari ViewModel
    val taskWithSubtasks by viewModel.taskState.collectAsStateWithLifecycle()

    // 3. State lokal untuk UI (Buffer editing)
    var title by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.taskParent?.title ?: "")
    }
    var subtasks by remember(taskWithSubtasks) {
        mutableStateOf(taskWithSubtasks?.subtasks ?: emptyList())
    }
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

    // State untuk dialog UI
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }

    // Picker States
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDueDate ?: System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedReminderTime?.get(Calendar.HOUR_OF_DAY) ?: 12,
        initialMinute = selectedReminderTime?.get(Calendar.MINUTE) ?: 0
    )

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // === DATA CUACA (QoL Feature) ===
//    val weatherIcon = WeatherRepository.getWeatherIcon(WeatherRepository.currentWeatherCode)
//    val currentTemp = WeatherRepository.currentTemp

    // === LOGIC ===

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
                id = "",
                taskId = taskWithSubtasks?.taskParent?.id ?: "",
                title = newSubtaskTitle,
                isCompleted = false
            )
            subtasks = subtasks + newSub
            newSubtaskTitle = ""
        }
    }

    val onSaveChanges: () -> Unit = {
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

    if (taskWithSubtasks == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (taskWithSubtasks?.taskParent?.id == "") "New Task" else "Edit Task") },
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
                ) { DatePicker(state = datePickerState) }
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
                        }) { Text("OK") }
                    }
                ) { TimePicker(state = timePickerState) }
            }

            // --- KONTEN FORM ---
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Judul Task
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Task Title") },
                        singleLine = true
                    )
                }

                // 2. Detail Grid (Due Date, Priority, Reminder + WEATHER)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Column 1: Date & Time
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Deadline", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = selectedDueDate?.let { dateFormatter.format(Date(it)) } ?: "-",
                                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = selectedReminderTime?.let { timeFormatter.format(it.time) } ?: "-",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Column 2: Priority
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Priority", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = selectedPriority,
                                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                                    color = if(selectedPriority=="High") Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Column 3: WEATHER INFO (Integrasi API)
//                            Column(horizontalAlignment = Alignment.End) {
//                                Text("Forecast", style = MaterialTheme.typography.labelSmall)
//                                if (currentTemp != null) {
//                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                        Text(weatherIcon, style = MaterialTheme.typography.titleMedium)
//                                        Spacer(Modifier.width(4.dp))
//                                        Text(
//                                            "${currentTemp.toInt()}°C",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//                                } else {
//                                    Text("N/A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//                                }
//                            }
                        }
                    }
                }

                // 3. Tombol Aksi
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Alarm, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Reminder")
                            }
                            Button(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.CalendarToday, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Due Date")
                            }
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
                                Icon(Icons.Default.Flag, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Priority: $selectedPriority")
                            }
                            DropdownMenu(
                                expanded = showPriorityMenu,
                                onDismissRequest = { showPriorityMenu = false }
                            ) {
                                listOf("High", "Medium", "Low", "None").forEach { p ->
                                    DropdownMenuItem(text = { Text(p) }, onClick = { selectedPriority = p; showPriorityMenu = false })
                                }
                            }
                        }
                    }
                }

                // 4. Subtasks
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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

                // Input Subtask Baru
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

// --- HELPER COMPOSABLES ---

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