package com.matedroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matedroid.ui.screens.dashboard.DashboardScreen
import com.matedroid.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Settings : Screen("settings")
    data object Dashboard : Screen("dashboard")
    data object Charges : Screen("charges")
    data object ChargeDetail : Screen("charges/{chargeId}") {
        fun createRoute(chargeId: Int) = "charges/$chargeId"
    }
    data object Drives : Screen("drives")
    data object DriveDetail : Screen("drives/{driveId}") {
        fun createRoute(driveId: Int) = "drives/$driveId"
    }
    data object Battery : Screen("battery")
    data object Updates : Screen("updates")
}

@Composable
fun NavGraph(
    startViewModel: StartDestinationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by startViewModel.startDestination.collectAsState()

    if (startDestination == null) {
        return // Wait for determination
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    }
}
