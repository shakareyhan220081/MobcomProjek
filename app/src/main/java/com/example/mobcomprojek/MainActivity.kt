package com.example.mobcomprojek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobcomprojek.ui.navigation.Screen
import com.example.mobcomprojek.ui.screens.TaskDetailScreen
import com.example.mobcomprojek.ui.screens.TasksScreen
import com.example.mobcomprojek.ui.theme.MobcomProjekTheme
import com.example.mobcomprojek.viewmodel.TaskDetailViewModel
import com.example.mobcomprojek.viewmodel.TaskDetailViewModelFactory
import com.example.mobcomprojek.viewmodel.TaskViewModelFactory
import com.example.mobcomprojek.viewmodel.TasksViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobcomProjekTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {

    val navController = rememberNavController()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // --- Database & Repository Setup ---
    val application = LocalContext.current.applicationContext as TaskApplication
    val repository = application.repository

    val navigationItems = listOf(
        Screen.Home,
        Screen.Notes,
        Screen.Tasks,
        Screen.Settings
    )

    val bottomBarRoutes = navigationItems.map { it.route }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Buat Baru",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Tugas Baru") },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Tugas Baru")
                    },
                    modifier = Modifier.clickable {
                        // TODO: Navigasi ke layar buat tugas baru
                        isSheetOpen = false
                    }
                )

                ListItem(
                    headlineContent = { Text("Catatan Baru") },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = "Catatan Baru")
                    },
                    modifier = Modifier.clickable {
                        // TODO: Navigasi ke layar buat catatan baru
                        isSheetOpen = false
                    }
                )
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppNavigationBar(
                    navController = navController,
                    items = navigationItems,
                    onAddClick = { isSheetOpen = true }
                )
            }
        }
    ) { innerPadding ->
        // --- NAVHOST ---
        // AppNavHost call removed and replaced with NavHost definition
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route, // Start at Home
            modifier = Modifier.padding(innerPadding)
        ) {

            // --- Home Screen (Placeholder) ---
            composable(Screen.Home.route) {
                // TODO: Create HomeScreen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Home Screen")
                }
            }

            // --- Notes Screen (Placeholder) ---
            composable(Screen.Notes.route) {
                // TODO: Create NotesScreen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Notes Screen")
                }
            }

            // --- Tasks Screen (Implemented) ---
            composable(Screen.Tasks.route) {
                // Create TasksViewModel using the factory
                val tasksViewModel: TasksViewModel = viewModel(
                    factory = TaskViewModelFactory(repository)
                )
                TasksScreen(
                    navController = navController,
                    viewModel = tasksViewModel
                )
            }

            // --- Settings Screen (Placeholder) ---
            composable(Screen.Settings.route) {
                // TODO: Create SettingsScreen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Settings Screen")
                }
            }

            // --- Task Detail Screen (Implemented) ---
            val taskDetailRoute = "task_detail/{taskId}"
            composable(taskDetailRoute) { backStackEntry ->
                // Extract taskId
                val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0

                // Create TaskDetailViewModel using its specific factory
                val detailViewModel: TaskDetailViewModel = viewModel(
                    factory = TaskDetailViewModelFactory(repository, taskId)
                )
                TaskDetailScreen(
                    navController = navController,
                    viewModel = detailViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavigationBar(
    navController: NavController,
    items: List<Screen>,
    onAddClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val home = items[0]
        val notes = items[1]
        val tasks = items[2]
        val settings = items[3]

        // Home
        AppNavItem(
            screen = home,
            isSelected = currentRoute == home.route,
            onClick = {
                navController.navigate(home.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // Notes
        AppNavItem(
            screen = notes,
            isSelected = currentRoute == notes.route,
            onClick = {
                navController.navigate(notes.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // ADD button in the center
        Box(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }

        // Tasks
        AppNavItem(
            screen = tasks,
            isSelected = currentRoute == tasks.route,
            onClick = {
                navController.navigate(tasks.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // Settings
        AppNavItem(
            screen = settings,
            isSelected = currentRoute == settings.route,
            onClick = {
                navController.navigate(settings.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Composable
fun RowScope.AppNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        modifier = Modifier.weight(1f),
        label = { Text(screen.label) },
        icon = { screen.icon?.let { Icon(it, contentDescription = screen.label) } },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}