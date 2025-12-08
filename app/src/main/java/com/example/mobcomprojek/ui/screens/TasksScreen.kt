package com.example.mobcomprojek.ui.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobcomprojek.data.Category
import com.example.mobcomprojek.data.Subtask
import com.example.mobcomprojek.data.TaskParent
import com.example.mobcomprojek.data.TaskParentWithSubtasks
import com.example.mobcomprojek.viewmodel.TasksViewModel
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: TasksViewModel = viewModel()
) {
    // === STATE ===
    val categories by viewModel.categoriesWithTasks.collectAsStateWithLifecycle()

    // State Search
    var searchQuery by remember { mutableStateOf("") }

    // Logic Filtering Search
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            // Filter kategori yang memiliki task sesuai pencarian
            categories.mapNotNull { cat ->
                val matchingTasks = cat.tasks.filter {
                    it.taskParent.title.contains(searchQuery, ignoreCase = true)
                }
                if (matchingTasks.isNotEmpty()) {
                    cat.copy(tasks = matchingTasks)
                } else {
                    null
                }
            }
        }
    }

    var categoryToRename by remember { mutableStateOf<Category?>(null) }
    var taskToRename by remember { mutableStateOf<TaskParent?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                // UBAH DISINI: Ganti Title Text jadi Search Bar
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp), // Padding agar tidak mepet tombol +
                        placeholder = { Text("Search tasks...") },
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
                navigationIcon = {},
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // --- 1. WEEKLY PROGRESS (PER CATEGORY) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Category Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categories.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No categories yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            // Gunakan categories asli untuk progress bar agar tetap statis meski di search
                            items(categories) { catData ->
                                CategoryProgressCard(
                                    categoryName = catData.category.title,
                                    tasks = catData.tasks
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. CATEGORIES & TASKS LIST ---
            item {
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Render list yang sudah di-filter (filteredCategories)
            if (filteredCategories.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "No tasks found matching '$searchQuery'",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredCategories) { categoryWithTasks ->
                    CategorySection(
                        category = categoryWithTasks.category,
                        tasksWithSubtasks = categoryWithTasks.tasks,

                        onEditorClick = { taskParent ->
                            navController.navigate("task_detail/${taskParent.id}")
                        },
                        onRenameTaskParent = { taskParent -> taskToRename = taskParent },
                        onDeleteTaskParent = { taskParent -> viewModel.deleteTaskParent(taskParent) },
                        onRenameCategoryClick = { categoryToRename = categoryWithTasks.category },
                        onDeleteCategoryClick = { viewModel.deleteCategory(categoryWithTasks.category.id) },
                        onAddTaskClick = { viewModel.addTask(categoryWithTasks.category.id, "New Task") },
                        // Checkbox Logic
                        onTaskCheckedChange = { task -> viewModel.toggleTaskCompletion(task) },
                        onSubtaskCheckedChange = { sub -> viewModel.toggleSubtaskCompletion(sub) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // --- DIALOGS ---
    val currentCategoryToRename = categoryToRename
    if (currentCategoryToRename != null) {
        RenameDialog(
            title = "Rename Category",
            initialValue = currentCategoryToRename.title,
            onConfirm = { newName ->
                viewModel.renameCategory(currentCategoryToRename, newName)
                categoryToRename = null
            },
            onDismiss = { categoryToRename = null }
        )
    }

    val currentTaskToRename = taskToRename
    if (currentTaskToRename != null) {
        RenameDialog(
            title = "Rename Task",
            initialValue = currentTaskToRename.title,
            onConfirm = { newName ->
                viewModel.renameTaskParent(currentTaskToRename, newName)
                taskToRename = null
            },
            onDismiss = { taskToRename = null }
        )
    }

    if (showAddCategoryDialog) {
        RenameDialog(
            title = "New Category",
            initialValue = "",
            label = "Category Name",
            confirmText = "Add",
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}

// === KOMPONEN UI ===

@Composable
fun CategoryProgressCard(
    categoryName: String,
    tasks: List<TaskParentWithSubtasks>
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.taskParent.isCompleted }
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$completedTasks/$totalTasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    category: Category,
    tasksWithSubtasks: List<TaskParentWithSubtasks>,
    modifier: Modifier = Modifier,
    onEditorClick: (TaskParent) -> Unit,
    onRenameTaskParent: (TaskParent) -> Unit,
    onDeleteTaskParent: (TaskParent) -> Unit,
    onRenameCategoryClick: () -> Unit,
    onDeleteCategoryClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskCheckedChange: (TaskParent) -> Unit,
    onSubtaskCheckedChange: (Subtask) -> Unit
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { categoryMenuExpanded = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Add Task") }, onClick = { onAddTaskClick(); categoryMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Rename Category") }, onClick = { onRenameCategoryClick(); categoryMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Delete Category") }, onClick = { onDeleteCategoryClick(); categoryMenuExpanded = false })
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (tasksWithSubtasks.isEmpty()) {
                    Text(
                        "No tasks here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                } else {
                    tasksWithSubtasks.forEach { taskData ->
                        TaskParentItem(
                            taskWithSubtasks = taskData,
                            onRenameClick = { onRenameTaskParent(taskData.taskParent) },
                            onDeleteClick = { onDeleteTaskParent(taskData.taskParent) },
                            onEditorClick = { onEditorClick(taskData.taskParent) },
                            onTaskCheckedChange = { onTaskCheckedChange(taskData.taskParent) },
                            onSubtaskCheckedChange = onSubtaskCheckedChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskParentItem(
    taskWithSubtasks: TaskParentWithSubtasks,
    modifier: Modifier = Modifier,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditorClick: () -> Unit,
    onTaskCheckedChange: () -> Unit,
    onSubtaskCheckedChange: (Subtask) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val taskParent = taskWithSubtasks.taskParent
    val subtasks = taskWithSubtasks.subtasks
    val totalSub = subtasks.size

    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { onTaskCheckedChange() }
                ) {
                    Checkbox(
                        checked = taskParent.isCompleted,
                        onCheckedChange = { onTaskCheckedChange() },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (totalSub > 0) isExpanded = !isExpanded else onEditorClick()
                        }
                ) {
                    Text(
                        text = taskParent.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (taskParent.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (taskParent.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (taskParent.dueDate != null) {
                            MetadataChip(
                                text = dateFormatter.format(Date(taskParent.dueDate)),
                                icon = Icons.Default.CalendarToday,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        if (taskParent.priority != "None") {
                            val (prioColor, prioText) = when(taskParent.priority) {
                                "High" -> Color(0xFFE53935) to "High"
                                "Medium" -> Color(0xFFFB8C00) to "Med"
                                else -> Color(0xFF43A047) to "Low"
                            }
                            MetadataChip(
                                text = prioText,
                                icon = Icons.Default.Flag,
                                color = prioColor
                            )
                        }

                        if (taskParent.reminderTime != null) {
                            Icon(
                                Icons.Default.Notifications,
                                "Reminder Set",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (totalSub > 0) {
                        val completedSub = subtasks.count { it.isCompleted }
                        Text(
                            "$completedSub/$totalSub",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Edit Details") }, onClick = { onEditorClick(); menuExpanded = false })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { onRenameClick(); menuExpanded = false })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { onDeleteClick(); menuExpanded = false })
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded && totalSub > 0) {
                Column(modifier = Modifier.padding(start = 44.dp, end = 16.dp, bottom = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    Spacer(Modifier.height(4.dp))
                    subtasks.forEach { subtask ->
                        SubtaskDisplayItem(
                            subtask = subtask,
                            onCheckedChange = { onSubtaskCheckedChange(subtask) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataChip(text: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(10.dp), tint = color)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SubtaskDisplayItem(
    subtask: Subtask,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (subtask.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "Done",
            tint = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(
    title: String,
    initialValue: String,
    label: String = "Name",
    confirmText: String = "Save",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank()
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}