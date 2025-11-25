package com.example.mobcomprojek.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobcomprojek.data.Category
import com.example.mobcomprojek.data.Subtask
import com.example.mobcomprojek.data.TaskParent
import com.example.mobcomprojek.data.TaskParentWithSubtasks
import com.example.mobcomprojek.viewmodel.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: TasksViewModel
) {
    // === STATE ===
    val categories by viewModel.categoriesWithTasks.collectAsState()

    // --- ADDED ---
    // State to hold the category that is currently being renamed.
    // If null, no dialog is shown.
    var categoryToRename by remember { mutableStateOf<Category?>(null) }

    // TODO: Implement dialogs for adding
    // var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TASKS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open nav drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { viewModel.addCategory("New Category") }) {
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
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {

            // --- Bagian "Scheduled Tasks" (Placeholder) ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scheduled Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Bagian Kategori (List) ---
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Render list dari ViewModel
            items(categories) { categoryWithTasks ->
                CategorySection(
                    category = categoryWithTasks.category,
                    tasksWithSubtasks = categoryWithTasks.tasks,

                    onEditorClick = { taskParent ->
                        navController.navigate("task_detail/${taskParent.id}")
                    },

                    onRenameTaskParent = { taskParent ->
                        viewModel.renameTaskParent(taskParent, "Renamed Task") // TODO: Pakai dialog
                    },
                    onDeleteTaskParent = { taskParent ->
                        viewModel.deleteTaskParent(taskParent)
                    },
                    // --- CHANGED ---
                    // When rename is clicked, set the state to show the dialog
                    onRenameCategoryClick = {
                        categoryToRename = categoryWithTasks.category
                    },
                    onDeleteCategoryClick = {
                        viewModel.deleteCategory(categoryWithTasks.category)
                    },
                    onAddTaskClick = {
                        viewModel.addTask(categoryWithTasks.category.id, "New Task") // TODO: Pakai dialog
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // --- ADDED ---
    // Dialog logic:
    // Use a local val to ensure stability if the state changes during recomposition
    val currentCategoryToRename = categoryToRename
    if (currentCategoryToRename != null) {
        RenameCategoryDialog(
            category = currentCategoryToRename,
            onConfirm = { newName ->
                // Call the viewmodel with the new name
                viewModel.renameCategory(currentCategoryToRename, newName)
                // Hide the dialog
                categoryToRename = null
            },
            onDismiss = {
                // Hide the dialog
                categoryToRename = null
            }
        )
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
    onAddTaskClick: () -> Unit
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // === Header Kategori (No change) ===
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand/Collapse"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.weight(1f))
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
                        text = { Text("Add Task") },
                        onClick = {
                            onAddTaskClick()
                            categoryMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Category") },
                        onClick = {
                            onRenameCategoryClick() // This now triggers the dialog
                            categoryMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Category") },
                        onClick = {
                            onDeleteCategoryClick()
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // === Daftar tasks di Kategori ===
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            tasksWithSubtasks.forEach { taskWithSubtasks ->
                TaskParentItem(
                    taskWithSubtasks = taskWithSubtasks,
                    onRenameClick = { onRenameTaskParent(taskWithSubtasks.taskParent) },
                    onDeleteClick = { onDeleteTaskParent(taskWithSubtasks.taskParent) },
                    onEditorClick = { onEditorClick(taskWithSubtasks.taskParent) }
                )
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
    onEditorClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val taskParent = taskWithSubtasks.taskParent
    val subtasks = taskWithSubtasks.subtasks

    val totalSubtasks = subtasks.size
    val completedSubtasks = subtasks.count { it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Expand/Collapse"
            )

            Text(
                text = taskParent.title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )

            if (totalSubtasks > 0) {
                Text(
                    text = "$completedSubtasks/$totalSubtasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi Task")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Editor") },
                        onClick = {
                            onEditorClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onRenameClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick()
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        // --- Konten Subtask (Collapsible) ---
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 16.dp, bottom = 8.dp)
            ) {
                if (subtasks.isEmpty()) {
                    Text(
                        text = "No subtasks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    subtasks.forEach { subtask ->
                        SubtaskDisplayItem(subtask = subtask)
                    }
                }
            }
        }
    }
}

/**
 * [NEW COMPOSABLE]
 * A simple, read-only item to display a subtask in the list.
 */
@Composable
fun SubtaskDisplayItem(
    subtask: Subtask,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (subtask.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "Completed",
            tint = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- ADDED ---
/**
 * A dialog composable for renaming a category.
 *
 * @param category The category to be renamed.
 * @param onConfirm Callback with the new name when the user confirms.
 * @param onDismiss Callback when the user dismisses the dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameCategoryDialog(
    category: Category,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Remember the text field state, pre-filled with the current name.
    // Use 'key' to reset the state if a different category is passed.
    var newName by remember(category) { mutableStateOf(category.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Category") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Only confirm if the name is not blank
                    if (newName.isNotBlank()) {
                        onConfirm(newName)
                    }
                },
                // Disable the button if the name is blank
                enabled = newName.isNotBlank()
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    // Preview this will not work properly without a fake ViewModel
    // TasksScreen(navController = rememberNavController(), viewModel = ...)
}