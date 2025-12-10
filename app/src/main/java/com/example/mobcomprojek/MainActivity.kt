package com.example.mobcomprojek

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
//import com.example.mobcomprojek.data.api.WeatherRepository
import com.example.mobcomprojek.data.local.UserPreferences
import com.example.mobcomprojek.ui.navigation.Screen
import com.example.mobcomprojek.ui.screens.*
import com.example.mobcomprojek.ui.theme.MobcomProjekTheme
import com.example.mobcomprojek.viewmodel.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Client untuk akses GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Launcher untuk meminta izin lokasi secara popup
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
//        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
//
////        if (fineLocationGranted || coarseLocationGranted) {
////            // Izin diberikan user, ambil lokasi
////            getUserLocationAndFetchWeather()
////        } else {
////            // Izin ditolak, biarkan cuaca kosong (fitur nonaktif)
////            Log.d("MainActivity", "Izin lokasi ditolak. Cuaca dinonaktifkan.")
////        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val currentUser = Firebase.auth.currentUser
        val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route
        val userPreferences = UserPreferences(applicationContext)

        // --- LOGIC CUACA BERBASIS LOKASI ---
        // Cek apakah izin sudah ada?
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Sudah diizinkan sebelumnya -> Langsung ambil lokasi
//            getUserLocationAndFetchWeather()
//        } else {
//            // Belum diizinkan -> Minta izin ke user
//            requestPermissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            val fontScale by userPreferences.fontScale.collectAsState(initial = 1.0f)

            MobcomProjekTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = !isDarkTheme },
                        startRoute = startDestination
                    )
                }
            }
        }
    }

    // Fungsi Helper: Ambil Lat/Lon dari HP -> Panggil API
//    private fun getUserLocationAndFetchWeather() {
//        try {
//            // Cek permission lagi (syarat Android) walau sudah dicek di atas
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//
//            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                if (location != null) {
//                    // Lokasi ketemu! Panggil API Weather
//                    Log.d("MainActivity", "Lokasi HP: ${location.latitude}, ${location.longitude}")
//                    lifecycleScope.launch {
//                        WeatherRepository.fetchWeather(location.latitude, location.longitude)
//                    }
//                } else {
//                    // Lokasi null (biasanya HP baru nyala / GPS mati total)
//                    // Kita bisa pakai default (misal Jakarta) atau biarkan kosong
//                    Log.d("MainActivity", "Lokasi null (GPS mati/belum dapat sinyal)")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Error get location: ${e.message}")
//        }
//    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    startRoute: String
) {
    val navController = rememberNavController()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val navigationItems = listOf(Screen.Home, Screen.Notes, Screen.Tasks, Screen.Settings)
    val bottomBarRoutes = navigationItems.map { it.route }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    if (isSheetOpen) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isSheetOpen = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Buat Baru", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                ListItem(
                    headlineContent = { Text("Tugas Baru") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.ListAlt, "Tugas") },
                    modifier = Modifier.clickable {
                        isSheetOpen = false
                        navController.navigate("task_detail/new")
                    }
                )
                ListItem(
                    headlineContent = { Text("Catatan Baru") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Notes, "Catatan") },
                    modifier = Modifier.clickable {
                        isSheetOpen = false
                        navController.navigate(Screen.NoteDetail.route + "/new")
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
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(navController = navController) }
            composable(Screen.Home.route) {
                Home(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }
            composable(Screen.Notes.route) { NotesScreen(navController = navController) }

            composable(
                route = Screen.NoteDetail.route + "/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                NoteDetailScreen(navController = navController, noteId = noteId)
            }

            composable(Screen.Tasks.route) { TasksScreen(navController = navController) }

            composable(
                route = "task_detail/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                val detailVM: TaskDetailViewModel = viewModel()
                LaunchedEffect(taskId) { detailVM.loadTask(taskId) }
                TaskDetailScreen(navController = navController, taskId = taskId, viewModel = detailVM)
            }

            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

// --- KOMPONEN NAVIGASI ---

@Composable
fun AppNavigationBar(navController: NavController, items: List<Screen>, onAddClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        AppNavItem(items[0], currentRoute == items[0].route) { navigateToTab(navController, items[0].route) }
        AppNavItem(items[1], currentRoute == items[1].route) { navigateToTab(navController, items[1].route) }

        Box(modifier = Modifier.weight(1f).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = onAddClick,
                modifier = Modifier.offset(y = (-10).dp).size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, "Tambah")
            }
        }

        AppNavItem(items[2], currentRoute == items[2].route) { navigateToTab(navController, items[2].route) }
        AppNavItem(items[3], currentRoute == items[3].route) { navigateToTab(navController, items[3].route) }
    }
}

fun navigateToTab(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun RowScope.AppNavItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        modifier = Modifier.weight(1f),
        label = { Text(screen.label) },
        icon = { screen.icon?.let { Icon(it, screen.label) } },
        selected = isSelected,
        onClick = onClick
    )
}