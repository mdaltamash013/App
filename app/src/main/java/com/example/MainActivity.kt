package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AppBottomNav
import com.example.ui.components.ConnectivityDiagnosticDialog
import com.example.ui.components.OfflineBanner
import com.example.ui.components.TopBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AssessmentScreen
import com.example.ui.screens.DoctorScreen
import com.example.ui.screens.EmergencyScreen
import com.example.ui.screens.FacilitiesScreen
import com.example.ui.screens.HealthRecordScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MedicineScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.wellness.MyWellnessScreen
import com.example.ui.screens.wellness.WellnessAssessmentScreen
import com.example.ui.screens.wellness.WelfareOfficerDashboardScreen
import com.example.ui.theme.SwasthyaSathiTheme
import com.example.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SwasthyaSathiTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: HealthViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val language by viewModel.language.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isHandsFree by viewModel.isHandsFree.collectAsState()

    val isInitialWelcomeNeeded = remember { viewModel.isInitialWelcomeRequired() }
    val startDestination = if (isInitialWelcomeNeeded) "welcome" else "home"
    val isWelcomeScreen = currentRoute == "welcome"
    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    if (showDiagnosticsDialog) {
        ConnectivityDiagnosticDialog(
            viewModel = viewModel,
            onDismiss = { showDiagnosticsDialog = false }
        )
    }

    // Permission launcher for Voice Audio & Location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissions.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) || permissions.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            // Already granted: retrieve current location automatically
            viewModel.requestCurrentLocation()
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Listen to voice navigation commands & triage triggers
    LaunchedEffect(viewModel.navEvent) {
        viewModel.navEvent.collect { route ->
            if (currentRoute != route) {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isWelcomeScreen) {
                TopBar(
                    language = language,
                    networkStatus = networkStatus,
                    isMuted = isMuted,
                    isHandsFree = isHandsFree,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onToggleMute = { viewModel.toggleMute() },
                    onOpenDiagnostics = { showDiagnosticsDialog = true }
                )
            }
        },
        bottomBar = {
            if (!isWelcomeScreen) {
                AppBottomNav(
                    currentRoute = currentRoute,
                    language = language,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Prominent Offline Notification Banner (only when outside welcome screen)
            if (!isWelcomeScreen) {
                OfflineBanner(
                    networkStatus = networkStatus,
                    language = language,
                    onCheckConnectivity = { showDiagnosticsDialog = true }
                )
            }

            // Navigation Host
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("welcome") {
                    WelcomeScreen(
                        viewModel = viewModel,
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("assessment") {
                    AssessmentScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("emergency") {
                    EmergencyScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("facilities") {
                    FacilitiesScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("medicine") {
                    MedicineScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("doctor") {
                    DoctorScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("summary") {
                    SummaryScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("records") {
                    HealthRecordScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("admin") {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable("my_wellness") {
                    MyWellnessScreen(
                        viewModel = viewModel,
                        onNavigateToAssessment = { navController.navigate("wellness_assessment") },
                        onNavigateToOfficerDashboard = { navController.navigate("welfare_officer") }
                    )
                }
                composable("wellness_assessment") {
                    WellnessAssessmentScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("welfare_officer") {
                    WelfareOfficerDashboardScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
