package com.example.mobcomprojek.ui.screens


import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobcomprojek.ui.theme.cardHighlight
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale



// Helper function (tidak berubah)
fun getWeeksForMonth(date: LocalDate): List<Pair<LocalDate, LocalDate>> {
    val weeks = mutableListOf<Pair<LocalDate, LocalDate>>()
    val firstDayOfMonth = date.withDayOfMonth(1)
    val lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth())

    val weekFields = WeekFields.of(Locale.getDefault())
    var current = firstDayOfMonth.with(weekFields.dayOfWeek(), 1)

    while (current.isBefore(lastDayOfMonth) || current.isEqual(lastDayOfMonth)) {
        val startOfWeek = if (current.month == date.month) current else firstDayOfMonth
        var endOfWeek = startOfWeek.plusDays(6)
        if (endOfWeek.month != date.month) {
            endOfWeek = lastDayOfMonth
        }

        weeks.add(Pair(startOfWeek, endOfWeek))
        current = current.plusWeeks(1)
    }
    return weeks
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(modifier: Modifier = Modifier, navController: NavController) {
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

    if (selectedWeek.first.month != currentDate.month) {
        selectedWeek = weeksInMonth.first()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 20.dp)
    ) {
        HomeHeader()
        Spacer(modifier = Modifier.height(26.dp))

        // === BAGIAN SCHEDULED TASKS ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            SectionHeader(title = "Scheduled Tasks")
            Spacer(modifier = Modifier.height(12.dp))

            MonthSelector(
                currentDate = currentDate,
                onMonthChange = { monthsToAdd ->
                    currentDate = currentDate.plusMonths(monthsToAdd.toLong())
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            WeekSelector(
                weeks = weeksInMonth,
                selectedWeek = selectedWeek,
                onWeekSelected = { week ->
                    selectedWeek = week
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            val weekRangeText = "Week ${weeksInMonth.indexOf(selectedWeek) + 1} (${selectedWeek.first.dayOfMonth} - ${selectedWeek.second.dayOfMonth})"

            Text(
                text = "Tasks for $weekRangeText",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            TaskCard(
                title = "Presentasi Proyek",
                date = selectedWeek.first.format(DateTimeFormatter.ofPattern("MMM dd")),
                category = "Pekerjaan",
                color = MaterialTheme.colorScheme.cardHighlight
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // === BAGIAN RECENT NOTES ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            SectionHeader(title = "Recent Notes")
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kolom Kiri
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NoteCard(
                        title = "Ide Proyek DueCal",
                        content = "Fitur utama: sinkronisasi GCal, subtask, dan notes...",
                        modifier = Modifier.height(170.dp)
                    )
                    NoteCard(
                        title = "Resep Ayam",
                        content = "Beli jahe, laos, kunyit...",
                        modifier = Modifier.height(170.dp)
                    )
                }
                // Kolom Kanan
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NoteCard(
                        title = "Catatan Meeting",
                        content = "Bahas pembagian tugas backend dan frontend...",
                        modifier = Modifier.height(170.dp)
                    )
                    NoteCard(
                        title = "List Belanja",
                        content = "Susu, Roti, Telur, Keju, Sereal...",
                        modifier = Modifier.height(170.dp)
                    )
                }
            }
        }
    }
}

// === COMPOSABLE BARU (UNTUK SELEKTOR MINGGU) ===
@Composable
fun WeekSelector(
    weeks: List<Pair<LocalDate, LocalDate>>,
    selectedWeek: Pair<LocalDate, LocalDate>,
    onWeekSelected: (Pair<LocalDate, LocalDate>) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(weeks.size) { index ->
            val week = weeks[index]
            val weekLabel = "Week ${index + 1}"
            val dateRange = "${week.first.format(dateFormatter)}-${week.second.format(dateFormatter)}"

            WeekItem(
                weekLabel = weekLabel,
                dateRange = dateRange,
                isSelected = week == selectedWeek,
                onClick = { onWeekSelected(week) }
            )
        }
    }
}


// === COMPOSABLE BARU (UNTUK ITEM MINGGU) ===
@Composable
fun WeekItem(
    weekLabel: String,
    dateRange: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(weekLabel, style = MaterialTheme.typography.labelMedium, color = contentColor)
        Text(dateRange, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
    }
}


// === COMPOSABLE SELEKTOR BULAN (IKON DIPERBAIKI) ===
@Composable
fun MonthSelector(
    currentDate: LocalDate,
    onMonthChange: (Int) -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(-1) }) {
            // ✔ FIXED: Menggunakan 'Icons.Filled'
            Icon(Icons.Filled.ChevronLeft, "Previous Month")
        }

        Text(
            text = currentDate.format(monthFormatter),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(onClick = { onMonthChange(1) }) {
            // ✔ FIXED: Menggunakan 'Icons.Filled'
            Icon(Icons.Filled.ChevronRight, "Next Month")
        }
    }
}


// === COMPOSABLE HEADER (TIDAK BERUBAH) ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.cardHighlight),
            contentAlignment = Alignment.Center
        ) {
            Text("R", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Selamat Datang!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "IRS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifikasi",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// === COMPOSABLE NOTE CARD (TIDAK BERUBAH) ===
@Composable
fun NoteCard(title: String, content: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4
                )
            }
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// === COMPOSABLE HEADER SECTION (TIDAK BERUBAH) ===
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold
            )
        )
        TextButton(onClick = { /* TODO */ }) {
            Text(
                text = "Lihat Semua",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// === COMPOSABLE TASK CARD (TIDAK BERUBAH) ===
@Composable
fun TaskCard(title: String, date: String, category: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}