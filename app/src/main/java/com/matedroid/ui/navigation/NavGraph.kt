package com.matedroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matedroid.ui.screens.battery.BatteryScreen
import com.matedroid.ui.screens.charges.ChargesScreen
import com.matedroid.ui.screens.dashboard.DashboardScreen
import com.matedroid.ui.screens.drives.DriveDetailScreen
import com.matedroid.ui.screens.drives.DrivesScreen
import com.matedroid.ui.screens.mileage.MileageScreen
import com.matedroid.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Settings : Screen("settings")
    data object Dashboard : Screen("dashboard")
    data object Charges : Screen("charges/{carId}") {
        fun createRoute(carId: Int) = "charges/$carId"
    }
    data object ChargeDetail : Screen("charges/{carId}/detail/{chargeId}") {
        fun createRoute(carId: Int, chargeId: Int) = "charges/$carId/detail/$chargeId"
    }
    data object Drives : Screen("drives/{carId}") {
        fun createRoute(carId: Int) = "drives/$carId"
    }
    data object DriveDetail : Screen("drives/{carId}/detail/{driveId}") {
        fun createRoute(carId: Int, driveId: Int) = "drives/$carId/detail/$driveId"
    }
    data object Battery : Screen("battery/{carId}?efficiency={efficiency}") {
        fun createRoute(carId: Int, efficiency: Double? = null): String {
            return if (efficiency != null) {
                "battery/$carId?efficiency=$efficiency"
            } else {
                "battery/$carId"
            }
        }
    }
    data object Mileage : Screen("mileage/{carId}") {
        fun createRoute(carId: Int) = "mileage/$carId"
    }
    data object Updates : Screen("updates/{carId}") {
        fun createRoute(carId: Int) = "updates/$carId"
    }
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
                },
                onNavigateToCharges = { carId ->
                    navController.navigate(Screen.Charges.createRoute(carId))
                },
                onNavigateToDrives = { carId ->
                    navController.navigate(Screen.Drives.createRoute(carId))
                },
                onNavigateToBattery = { carId, efficiency ->
                    navController.navigate(Screen.Battery.createRoute(carId, efficiency))
                },
                onNavigateToMileage = { carId ->
                    navController.navigate(Screen.Mileage.createRoute(carId))
                }
            )
        }

        composable(
            route = Screen.Charges.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: return@composable
            ChargesScreen(
                carId = carId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Drives.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: return@composable
            DrivesScreen(
                carId = carId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDriveDetail = { driveId ->
                    navController.navigate(Screen.DriveDetail.createRoute(carId, driveId))
                }
            )
        }

        composable(
            route = Screen.DriveDetail.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.IntType },
                navArgument("driveId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: return@composable
            val driveId = backStackEntry.arguments?.getInt("driveId") ?: return@composable
            DriveDetailScreen(
                carId = carId,
                driveId = driveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Battery.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.IntType },
                navArgument("efficiency") {
                    type = NavType.FloatType
                    defaultValue = 0f
                }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: return@composable
            val efficiency = backStackEntry.arguments?.getFloat("efficiency")?.toDouble()
                ?.takeIf { it > 0 }
            BatteryScreen(
                carId = carId,
                efficiency = efficiency,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Mileage.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: return@composable
            MileageScreen(
                carId = carId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
