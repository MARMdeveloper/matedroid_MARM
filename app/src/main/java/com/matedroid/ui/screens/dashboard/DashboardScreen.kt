package com.matedroid.ui.screens.dashboard

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.matedroid.data.api.models.BatteryDetails
import com.matedroid.data.api.models.CarExterior
import com.matedroid.data.api.models.CarGeodata
import com.matedroid.data.api.models.CarStatus
import com.matedroid.domain.model.CarImageResolver
import com.matedroid.data.api.models.CarStatusDetails
import com.matedroid.data.api.models.Units
import com.matedroid.domain.model.UnitFormatter
import com.matedroid.data.api.models.CarVersions
import com.matedroid.data.api.models.ChargingDetails
import com.matedroid.data.api.models.TpmsDetails
import com.matedroid.data.api.models.ClimateDetails
import com.matedroid.ui.theme.CarColorPalette
import com.matedroid.ui.theme.CarColorPalettes
import com.matedroid.ui.theme.MateDroidTheme
import com.matedroid.ui.theme.StatusError
import com.matedroid.ui.theme.StatusSuccess
import com.matedroid.ui.theme.StatusWarning
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCharges: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToDrives: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToBattery: (carId: Int, efficiency: Double?, exteriorColor: String?) -> Unit = { _, _, _ -> },
    onNavigateToMileage: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    onNavigateToUpdates: (carId: Int, exteriorColor: String?) -> Unit = { _, _ -> },
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.carStatus?.displayName ?: "MateDroid")
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.carStatus != null -> {
                    DashboardContent(
                        status = uiState.carStatus!!,
                        units = uiState.units,
                        carModel = uiState.selectedCarModel,
                        carTrimBadging = uiState.selectedCarTrimBadging,
                        carExterior = uiState.selectedCarExterior,
                        resolvedAddress = uiState.resolvedAddress,
                        totalCharges = uiState.totalCharges,
                        totalDrives = uiState.totalDrives,
                        onNavigateToCharges = {
                            uiState.selectedCarId?.let { carId ->
                                onNavigateToCharges(carId, uiState.selectedCarExterior?.exteriorColor)
                            }
                        },
                        onNavigateToDrives = {
                            uiState.selectedCarId?.let { carId ->
                                onNavigateToDrives(carId, uiState.selectedCarExterior?.exteriorColor)
                            }
                        },
                        onNavigateToBattery = {
                            uiState.selectedCarId?.let { carId ->
                                onNavigateToBattery(carId, uiState.selectedCarEfficiency, uiState.selectedCarExterior?.exteriorColor)
                            }
                        },
                        onNavigateToMileage = {
                            uiState.selectedCarId?.let { carId ->
                                onNavigateToMileage(carId, uiState.selectedCarExterior?.exteriorColor)
                            }
                        },
                        onNavigateToUpdates = {
                            uiState.selectedCarId?.let { carId ->
                                onNavigateToUpdates(carId, uiState.selectedCarExterior?.exteriorColor)
                            }
                        }
                    )
                }
                uiState.cars.isEmpty() && uiState.error == null -> {
                    EmptyContent()
                }
                uiState.error != null -> {
                    ErrorContent(message = uiState.error!!)
                }
                else -> {
                    // Car status still loading after cars loaded
                    LoadingContent()
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading vehicle data...")
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No vehicles found",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Make sure TeslamateApi is properly configured",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error loading data",
                style = MaterialTheme.typography.titleMedium,
                color = StatusError
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardContent(
    status: CarStatus,
    units: Units? = null,
    carModel: String? = null,
    carTrimBadging: String? = null,
    carExterior: CarExterior? = null,
    resolvedAddress: String? = null,
    totalCharges: Int? = null,
    totalDrives: Int? = null,
    onNavigateToCharges: () -> Unit = {},
    onNavigateToDrives: () -> Unit = {},
    onNavigateToBattery: () -> Unit = {},
    onNavigateToMileage: () -> Unit = {},
    onNavigateToUpdates: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val palette = CarColorPalettes.forExteriorColor(carExterior?.exteriorColor, isDarkTheme)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Battery Section with Car Image
            BatteryCard(
                status = status,
                units = units,
                carModel = carModel,
                carTrimBadging = carTrimBadging,
                carExterior = carExterior
            )

            // Location Section - show if we have coordinates
            if (status.latitude != null && status.longitude != null) {
                LocationCard(status = status, units = units, resolvedAddress = resolvedAddress)
            }

            // Vehicle Info Section
            VehicleInfoCard(
                status = status,
                units = units,
                totalCharges = totalCharges,
                totalDrives = totalDrives,
                onNavigateToUpdates = onNavigateToUpdates
            )
        }

        // Fixed bottom quick links
        QuickLinksRow(
            palette = palette,
            onNavigateToCharges = onNavigateToCharges,
            onNavigateToDrives = onNavigateToDrives,
            onNavigateToBattery = onNavigateToBattery,
            onNavigateToMileage = onNavigateToMileage,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun CarImage(
    carModel: String?,
    carTrimBadging: String?,
    carExterior: CarExterior?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetPath = remember(carModel, carTrimBadging, carExterior) {
        CarImageResolver.getAssetPath(
            model = carModel,
            exteriorColor = carExterior?.exteriorColor,
            wheelType = carExterior?.wheelType,
            trimBadging = carTrimBadging
        )
    }

    val scaleFactor = remember(carModel, carTrimBadging, carExterior) {
        CarImageResolver.getScaleFactor(
            model = carModel,
            exteriorColor = carExterior?.exteriorColor,
            wheelType = carExterior?.wheelType,
            trimBadging = carTrimBadging
        )
    }

    val bitmap = remember(assetPath) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            // Try fallback to default
            try {
                val fallbackPath = CarImageResolver.getDefaultAssetPath(carModel)
                context.assets.open(fallbackPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e2: Exception) {
                null
            }
        }
    }

    if (bitmap != null) {
        Box(
            modifier = modifier.height(210.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Car image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun StatusIndicatorsRow(
    status: CarStatus,
    units: Units?,
    palette: CarColorPalette,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: State and Lock
        Row(verticalAlignment = Alignment.CenterVertically) {
            // State indicator - icon changes based on state
            val stateIcon = when {
                status.isCharging || status.pluggedIn == true -> Icons.Filled.PowerSettingsNew
                status.state?.lowercase() == "online" -> Icons.Filled.Circle
                status.state?.lowercase() in listOf("asleep", "offline", "suspended") -> Icons.Filled.Bedtime
                else -> Icons.Filled.Circle
            }
            val stateColor = when {
                status.isCharging -> StatusSuccess
                status.pluggedIn == true -> palette.accent
                status.state?.lowercase() == "online" -> StatusSuccess
                else -> palette.onSurfaceVariant
            }
            Icon(
                imageVector = stateIcon,
                contentDescription = null,
                modifier = Modifier.size(if (stateIcon == Icons.Filled.Circle) 10.dp else 16.dp),
                tint = stateColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.state?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                style = MaterialTheme.typography.labelMedium,
                color = stateColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Locked indicator
            val isLocked = status.locked == true
            Icon(
                imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isLocked) StatusSuccess else StatusWarning
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isLocked) "Locked" else "Unlocked",
                style = MaterialTheme.typography.labelMedium,
                color = if (isLocked) StatusSuccess else StatusWarning
            )

            // Plug indicator (only when plugged in but not charging - charging already shows state)
            if (status.pluggedIn == true && !status.isCharging) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Power,
                    contentDescription = "Plugged in",
                    modifier = Modifier.size(16.dp),
                    tint = palette.accent
                )
            }
        }

        // Right side: Temperatures
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Inside temp
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = palette.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = status.insideTemp?.let { UnitFormatter.formatTemperature(it, units) } ?: "--",
                style = MaterialTheme.typography.labelMedium,
                color = palette.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Outside temp
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = palette.accent
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = status.outsideTemp?.let { UnitFormatter.formatTemperature(it, units) } ?: "--",
                style = MaterialTheme.typography.labelMedium,
                color = palette.accent
            )
        }
    }
}

@Composable
private fun BatteryCard(
    status: CarStatus,
    units: Units?,
    carModel: String? = null,
    carTrimBadging: String? = null,
    carExterior: CarExterior? = null
) {
    val isDarkTheme = isSystemInDarkTheme()
    val palette = CarColorPalettes.forExteriorColor(carExterior?.exteriorColor, isDarkTheme)

    val batteryLevel = status.batteryLevel ?: 0
    val batteryColor = when {
        batteryLevel < 20 -> StatusError
        batteryLevel < 40 -> StatusWarning
        else -> palette.onSurface
    }
    val chargeLimit = status.chargeLimitSoc ?: 100

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = palette.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Status indicators row at the top
            StatusIndicatorsRow(
                status = status,
                units = units,
                palette = palette,
                modifier = Modifier.padding(top = 4.dp, bottom = 0.dp)
            )

            // Car image
            CarImage(
                carModel = carModel,
                carTrimBadging = carTrimBadging,
                carExterior = carExterior,
                modifier = Modifier.fillMaxWidth()
            )

            // Battery info row - closer to car image
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Battery percentage with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.BatteryChargingFull,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = batteryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = batteryColor
                    )
                    if (status.isCharging) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = "Charging",
                            modifier = Modifier.size(20.dp),
                            tint = StatusSuccess
                        )
                    }
                    if (batteryLevel > 90) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "High charge level",
                            modifier = Modifier.size(20.dp),
                            tint = StatusWarning
                        )
                    }
                }

                // Right: Range and limit
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = status.ratedBatteryRangeKm?.let { UnitFormatter.formatDistance(it, units, 0) } ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = palette.onSurface
                    )
                    Text(
                        text = "Limit: ${status.chargeLimitSoc ?: "--"}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onSurfaceVariant
                    )
                }
            }

            // Charging section - always reserve space for consistent card height
            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar - always shown but different appearance when not charging
            ChargingProgressBar(
                currentLevel = batteryLevel,
                targetLevel = chargeLimit,
                isCharging = status.isCharging,
                palette = palette,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Charging info row - content visible only when charging, but space always reserved
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status.isCharging) {
                    Text(
                        text = "${status.chargerPower ?: 0} kW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = StatusSuccess
                    )
                    Text(
                        text = "+${status.chargeEnergyAdded?.let { "%.1f".format(it) } ?: "0"} kWh",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = palette.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.timeToFullCharge?.let { formatHoursMinutes(it) } ?: "--",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.onSurfaceVariant
                        )
                    }
                }
                // When not charging, row is empty but maintains height
            }
        }
    }
}

@Composable
private fun ChargingProgressBar(
    currentLevel: Int,
    targetLevel: Int,
    isCharging: Boolean = false,
    palette: CarColorPalette,
    modifier: Modifier = Modifier
) {
    val currentFraction = currentLevel / 100f
    val targetFraction = targetLevel / 100f
    val solidGreen = StatusSuccess
    val dimmedGreen = StatusSuccess.copy(alpha = 0.3f)

    Canvas(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        val width = size.width
        val height = size.height

        // Background
        drawRect(
            color = palette.progressTrack,
            size = size
        )

        if (isCharging) {
            // Charging: show green with target area
            // Dimmed green for target area (from current to target)
            if (targetFraction > currentFraction) {
                drawRect(
                    color = dimmedGreen,
                    topLeft = androidx.compose.ui.geometry.Offset(width * currentFraction, 0f),
                    size = androidx.compose.ui.geometry.Size(
                        width * (targetFraction - currentFraction),
                        height
                    )
                )
            }
            // Solid green for current charge level
            drawRect(
                color = solidGreen,
                size = androidx.compose.ui.geometry.Size(width * currentFraction, height)
            )
        } else {
            // Not charging: show accent color with limit marker
            // Dimmed accent for limit area
            if (targetFraction > currentFraction) {
                drawRect(
                    color = palette.accentDim,
                    topLeft = androidx.compose.ui.geometry.Offset(width * currentFraction, 0f),
                    size = androidx.compose.ui.geometry.Size(
                        width * (targetFraction - currentFraction),
                        height
                    )
                )
            }
            // Solid accent for current charge level
            drawRect(
                color = palette.accent,
                size = androidx.compose.ui.geometry.Size(width * currentFraction, height)
            )
        }
    }
}

@Composable
private fun LocationCard(status: CarStatus, units: Units?, resolvedAddress: String? = null) {
    val context = LocalContext.current
    val latitude = status.latitude
    val longitude = status.longitude
    val geofence = status.geofence
    val elevation = status.elevation

    // Location text: geofence name if available, then resolved address, then coordinates
    val locationText = geofence ?: resolvedAddress ?: run {
        if (latitude != null && longitude != null) {
            "%.5f, %.5f".format(latitude, longitude)
        } else {
            "Unknown"
        }
    }

    // Format elevation with unit conversion
    val elevationText = elevation?.let {
        val isImperial = units?.unitOfLength == "mi"
        if (isImperial) {
            val feet = (it * 3.28084).toInt()
            "$feet ft"
        } else {
            "$it m"
        }
    }

    fun openInMaps() {
        if (latitude != null && longitude != null) {
            val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
            val intent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(intent)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openInMaps() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.titleMedium
                )

                // Elevation row
                if (elevationText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Terrain,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Elevation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = elevationText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Small map showing car location
            if (latitude != null && longitude != null) {
                Spacer(modifier = Modifier.width(12.dp))
                SmallLocationMap(
                    latitude = latitude,
                    longitude = longitude,
                    onClick = { openInMaps() },
                    modifier = Modifier
                        .width(140.dp)
                        .height(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
private fun SmallLocationMap(
    latitude: Double,
    longitude: Double,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = "MateDroid/1.0"
        onDispose { }
    }

    Box(
        modifier = modifier.clickable { onClick() }
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(false)

                    // Disable all interactions for this small preview map
                    setBuiltInZoomControls(false)
                    isClickable = false
                    isFocusable = false

                    val carLocation = GeoPoint(latitude, longitude)
                    controller.setZoom(15.0)
                    controller.setCenter(carLocation)

                // Add a marker for the car
                val marker = Marker(this).apply {
                    position = carLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ctx.getDrawable(android.R.drawable.ic_menu_mylocation)
                }
                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    }
}

@Composable
private fun VehicleInfoCard(
    status: CarStatus,
    units: Units?,
    totalCharges: Int? = null,
    totalDrives: Int? = null,
    onNavigateToUpdates: () -> Unit = {}
) {
    val distanceUnit = UnitFormatter.getDistanceUnit(units)
    val tpms = status.tpmsDetails

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vehicle Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compact 2x2 grid: Label: Value | Label: Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactInfoItem(
                    label = "Odometer",
                    value = status.odometer?.let {
                        val value = UnitFormatter.formatDistanceValue(it, units, 0)
                        "%,.0f $distanceUnit".format(value)
                    } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                CompactInfoItem(
                    label = "Drives",
                    value = totalDrives?.let { "%,d".format(it) } ?: "--",
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactInfoItem(
                    label = "Charges",
                    value = totalCharges?.let { "%,d".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                CompactInfoItem(
                    label = "SW",
                    value = status.version ?: "--",
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                    onClick = onNavigateToUpdates
                )
            }

            // Tire pressure section
            if (tpms != null && (tpms.pressureFl != null || tpms.pressureFr != null)) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                TirePressureDisplay(
                    tpms = tpms,
                    units = units
                )
            }
        }
    }
}

@Composable
private fun CompactInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val textColor = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onClick)
                } else Modifier
            ),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun TirePressureDisplay(
    tpms: TpmsDetails,
    units: Units?
) {
    val normalColor = MaterialTheme.colorScheme.onSurface
    val warningColor = StatusWarning
    val carBodyColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    // Use API warning flags only - no hardcoded thresholds
    val flColor = if (tpms.warningFl == true) warningColor else normalColor
    val frColor = if (tpms.warningFr == true) warningColor else normalColor
    val rlColor = if (tpms.warningRl == true) warningColor else normalColor
    val rrColor = if (tpms.warningRr == true) warningColor else normalColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left pressure values
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TirePressureValue(
                label = "FL",
                pressure = tpms.pressureFl,
                color = flColor,
                units = units
            )
            Spacer(modifier = Modifier.height(24.dp))
            TirePressureValue(
                label = "RL",
                pressure = tpms.pressureRl,
                color = rlColor,
                units = units
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Car top-down view with tires
        Canvas(
            modifier = Modifier
                .width(60.dp)
                .height(100.dp)
        ) {
            val carWidth = size.width * 0.65f
            val carHeight = size.height * 0.9f
            val carLeft = (size.width - carWidth) / 2
            val carTop = (size.height - carHeight) / 2

            val tireWidth = size.width * 0.15f
            val tireHeight = size.height * 0.18f
            val tireCornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())

            // Tire inset from car edges
            val tireInsetX = carWidth * 0.08f
            val tireInsetY = carHeight * 0.08f

            // Front left tire (inside car body at top-left corner)
            drawRoundRect(
                color = flColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    carLeft + tireInsetX,
                    carTop + tireInsetY
                ),
                size = androidx.compose.ui.geometry.Size(tireWidth, tireHeight),
                cornerRadius = tireCornerRadius
            )

            // Front right tire (inside car body at top-right corner)
            drawRoundRect(
                color = frColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    carLeft + carWidth - tireWidth - tireInsetX,
                    carTop + tireInsetY
                ),
                size = androidx.compose.ui.geometry.Size(tireWidth, tireHeight),
                cornerRadius = tireCornerRadius
            )

            // Rear left tire (inside car body at bottom-left corner)
            drawRoundRect(
                color = rlColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    carLeft + tireInsetX,
                    carTop + carHeight - tireHeight - tireInsetY
                ),
                size = androidx.compose.ui.geometry.Size(tireWidth, tireHeight),
                cornerRadius = tireCornerRadius
            )

            // Rear right tire (inside car body at bottom-right corner)
            drawRoundRect(
                color = rrColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    carLeft + carWidth - tireWidth - tireInsetX,
                    carTop + carHeight - tireHeight - tireInsetY
                ),
                size = androidx.compose.ui.geometry.Size(tireWidth, tireHeight),
                cornerRadius = tireCornerRadius
            )

            // Draw car body (rounded rectangle with 30% transparency) - drawn last so it's on top
            drawRoundRect(
                color = carBodyColor,
                topLeft = androidx.compose.ui.geometry.Offset(carLeft, carTop),
                size = androidx.compose.ui.geometry.Size(carWidth, carHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right pressure values
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TirePressureValue(
                label = "FR",
                pressure = tpms.pressureFr,
                color = frColor,
                units = units
            )
            Spacer(modifier = Modifier.height(24.dp))
            TirePressureValue(
                label = "RR",
                pressure = tpms.pressureRr,
                color = rrColor,
                units = units
            )
        }
    }
}

@Composable
private fun TirePressureValue(
    label: String,
    pressure: Double?,
    color: androidx.compose.ui.graphics.Color,
    units: Units?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = pressure?.let { UnitFormatter.formatPressure(it, units, 1) } ?: "--",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val innerModifier = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    } else {
        Modifier.padding(8.dp)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.then(innerModifier)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatHoursMinutes(hours: Double): String {
    val totalMinutes = (hours * 60).roundToInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
private fun QuickLinksRow(
    palette: CarColorPalette,
    onNavigateToCharges: () -> Unit,
    onNavigateToDrives: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToMileage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickLinkItem(
            title = "Charges",
            icon = Icons.Filled.ElectricBolt,
            palette = palette,
            onClick = onNavigateToCharges
        )
        QuickLinkItem(
            title = "Battery",
            icon = Icons.Filled.Battery5Bar,
            palette = palette,
            onClick = onNavigateToBattery
        )
        QuickLinkItem(
            title = "Drives",
            icon = Icons.Filled.DirectionsCar,
            palette = palette,
            onClick = onNavigateToDrives
        )
        QuickLinkItem(
            title = "Mileage",
            icon = Icons.Filled.Timeline,
            palette = palette,
            onClick = onNavigateToMileage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.QuickLinkItem(
    title: String,
    icon: ImageVector,
    palette: CarColorPalette,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = palette.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = palette.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    MateDroidTheme {
        DashboardContent(
            status = CarStatus(
                displayName = "My Tesla",
                state = "online",
                odometer = 45678.0,
                carStatus = CarStatusDetails(locked = true),
                carGeodata = CarGeodata(geofence = "Home"),
                carVersions = CarVersions(version = "2024.8.7"),
                climateDetails = ClimateDetails(
                    isClimateOn = false,
                    insideTemp = 21.5,
                    outsideTemp = 15.2
                ),
                batteryDetails = BatteryDetails(
                    batteryLevel = 72,
                    ratedBatteryRange = 312.5
                ),
                chargingDetails = ChargingDetails(
                    pluggedIn = true,
                    chargerPower = 11,
                    chargeEnergyAdded = 15.3,
                    timeToFullCharge = 1.5,
                    chargeLimitSoc = 80
                )
            )
        )
    }
}
