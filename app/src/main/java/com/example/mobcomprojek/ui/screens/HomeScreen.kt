package com.example.mobcomprojek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
// UBAH: Ganti DarkMode dengan NightlightRound atau Bedtime
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobcomprojek.data.NoteItem
import com.example.mobcomprojek.data.TaskParent
import com.example.mobcomprojek.ui.components.SectionHeader
import com.example.mobcomprojek.ui.navigation.Screen
import com.example.mobcomprojek.ui.theme.cardHighlight
import com.example.mobcomprojek.viewmodel.NotesViewModel
import com.example.mobcomprojek.viewmodel.TasksViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.Date
import com.example.mobcomprojek.data.local.UserPreferences

// Helper Model untuk menyatukan Task dan Note di Home
sealed class ScheduledItem(val id: String, val time: Long, val title: String, val subtitle: String, val type: String) {
    class Task(val data: TaskParent) : ScheduledItem(data.id, data.dueDate!!, data.title, "Priority: ${data.priority}", "Task")
    class Note(val data: NoteItem) : ScheduledItem(data.id, data.reminderTime!!, data.title, "Reminder Note", "Note")
}

// Helper function untuk Kalender Mingguan
fun getWeeksForMonth(date: LocalDate): List<Pair<LocalDate, LocalDate>> {
    val weeks = mutableListOf<Pair<LocalDate, LocalDate>>()
    val firstDayOfMonth = date.withDayOfMonth(1)
    val lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth())
    val weekFields = WeekFields.of(Locale.getDefault())
    var current = firstDayOfMonth.with(weekFields.dayOfWeek(), 1)

    while (current.isBefore(lastDayOfMonth) || current.isEqual(lastDayOfMonth)) {
        val startOfWeek = if (current.month == date.month) current else firstDayOfMonth
        var endOfWeek = startOfWeek.plusDays(6)
        if (endOfWeek.month != date.month) endOfWeek = lastDayOfMonth
        weeks.add(Pair(startOfWeek, endOfWeek))
        current = current.plusWeeks(1)
    }
    return weeks
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    notesViewModel: NotesViewModel = viewModel(),
    tasksViewModel: TasksViewModel = viewModel()
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    // --- AMBIL NAMA DARI SETTING ---
    val userPreferences = remember { UserPreferences(context) }
    val savedName by userPreferences.userName.collectAsState(initial = "Loading...")

    // Fallback: Jika belum ada di setting, ambil dari Firebase
    val displayName = if (savedName == "User" || savedName == "Loading...") {
        Firebase.auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
    } else {
        savedName
    }


    // --- DATA REALTIME DARI FIREBASE ---

    // 1. Data Notes
    val recentNotes by notesViewModel.notesState.collectAsStateWithLifecycle()
    val displayNotes = recentNotes.take(4)
    val columnLeftNotes = displayNotes.filterIndexed { index, _ -> index % 2 == 0 }
    val columnRightNotes = displayNotes.filterIndexed { index, _ -> index % 2 != 0 }

    // 2. Data Tasks
    val taskCategories by tasksViewModel.categoriesWithTasks.collectAsStateWithLifecycle()

    // Formatter Tanggal
    val noteDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val itemDateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    // Logic Kalender
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val weeksInMonth = remember(currentDate) { getWeeksForMonth(currentDate) }
    var selectedWeek by remember {
        mutableStateOf(
            weeksInMonth.firstOrNull {
                val now = LocalDate.now()
                now.isEqual(it.first) || (now.isAfter(it.first) && now.isBefore(it.second)) || now.isEqual(it.second)
            } ?: weeksInMonth.first()
        )
    }
    if (selectedWeek.first.month != currentDate.month) selectedWeek = weeksInMonth.first()

    // --- LOGIC INTEGRASI SCHEDULED TASKS ---
    val scheduledItems = remember(taskCategories, recentNotes) {
        val tasks = taskCategories.flatMap { it.tasks }.map { it.taskParent }
            .filter { it.dueDate != null && !it.isCompleted }
            .map { task -> ScheduledItem.Task(task) }

        val notes = recentNotes.filter { it.reminderTime != null }
            .map { note -> ScheduledItem.Note(note) }

        (tasks + notes).sortedBy { it.time }
    }

    // Filter item berdasarkan Minggu yang dipilih
    val filteredScheduledItems = remember(scheduledItems, selectedWeek) {
        scheduledItems.filter { item ->
            val itemDate = Instant.ofEpochMilli(item.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            !itemDate.isBefore(selectedWeek.first) && !itemDate.isAfter(selectedWeek.second)
        }
    }

    val userName = Firebase.auth.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
    val userInitial = displayName.firstOrNull()?.toString() ?: "U"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 20.dp)
    ) {
        // --- HEADER ---
        HomeHeader(
            userName = displayName,
            userInitial = userInitial,
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle
        )
        Spacer(modifier = Modifier.height(26.dp))

        // --- SCHEDULED TASKS & NOTES ---
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            SectionHeader(
                title = "Scheduled Tasks",
                showSeeAll = true,
                onSeeAllClick = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            MonthSelector(currentDate = currentDate) { currentDate = currentDate.plusMonths(it.toLong()) }
            Spacer(modifier = Modifier.height(12.dp))

            WeekSelector(weeks = weeksInMonth, selectedWeek = selectedWeek) { selectedWeek = it }
            Spacer(modifier = Modifier.height(20.dp))

            val weekRangeText = "Week ${weeksInMonth.indexOf(selectedWeek) + 1} (${selectedWeek.first.dayOfMonth} - ${selectedWeek.second.dayOfMonth})"
            Text(
                text = "Tasks for $weekRangeText",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredScheduledItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No scheduled tasks for this week", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredScheduledItems.forEach { item ->
                        val cardColor = if (item is ScheduledItem.Task)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer

                        TaskCard(
                            title = item.title,
                            date = itemDateFormatter.format(Date(item.time)),
                            category = item.subtitle,
                            color = cardColor,
                            onClick = {
                                when(item) {
                                    is ScheduledItem.Task -> navController.navigate("task_detail/${item.id}")
                                    is ScheduledItem.Note -> navController.navigate(Screen.NoteDetail.route + "/${item.id}")
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- RECENT NOTES ---
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            SectionHeader(
                title = "Recent Notes",
                showSeeAll = true,
                onSeeAllClick = {
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (displayNotes.isEmpty()) {
                Text(
                    "Belum ada catatan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        columnLeftNotes.forEach { note ->
                            NoteCard(
                                title = note.title,
                                content = note.content,
                                // Gunakan Date() karena createdAt adalah Long
                                date = noteDateFormatter.format(Date(note.createdAt)),
                                modifier = Modifier.height(170.dp).clickable { navController.navigate(Screen.NoteDetail.route + "/${note.id}") }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        columnRightNotes.forEach { note ->
                            NoteCard(
                                title = note.title,
                                content = note.content,
                                // Gunakan Date()
                                date = noteDateFormatter.format(Date(note.createdAt)),
                                modifier = Modifier.height(170.dp).clickable { navController.navigate(Screen.NoteDetail.route + "/${note.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

// === KOMPONEN UI ===

@Composable
fun WeekSelector(weeks: List<Pair<LocalDate, LocalDate>>, selectedWeek: Pair<LocalDate, LocalDate>, onWeekSelected: (Pair<LocalDate, LocalDate>) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 2.dp), modifier = Modifier.fillMaxWidth()) {
        items(weeks.size) { index ->
            val week = weeks[index]
            val isSelected = week == selectedWeek
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(backgroundColor).clickable { onWeekSelected(week) }.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Week ${index + 1}", style = MaterialTheme.typography.labelMedium, color = contentColor)
                Text("${week.first.format(dateFormatter)}-${week.second.format(dateFormatter)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

@Composable
fun MonthSelector(currentDate: LocalDate, onMonthChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onMonthChange(-1) }) { Icon(Icons.Filled.ChevronLeft, "Prev") }
        Text(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        IconButton(onClick = { onMonthChange(1) }) { Icon(Icons.Filled.ChevronRight, "Next") }
    }
}

@Composable
fun HomeHeader(userName: String, userInitial: String, isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.cardHighlight), contentAlignment = Alignment.Center) {
            Text(userInitial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Selamat Datang,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onThemeToggle, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surface).size(40.dp)) {
            // FIX: Menggunakan Icons.Default.NightlightRound sebagai pengganti DarkMode
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.NightlightRound,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surface).size(40.dp)) {
            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NoteCard(title: String, content: String, date: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp))
                Text(content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TaskCard(title: String, date: String, category: String, color: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(date, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}