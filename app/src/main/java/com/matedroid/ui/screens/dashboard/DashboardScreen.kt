package com.matedroid.ui.screens.dashboard

import android.graphics.Paint
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.matedroid.data.api.models.CarGeodata
import com.matedroid.data.api.models.CarStatus
import com.matedroid.data.api.models.CarStatusDetails
import com.matedroid.data.api.models.Units
import com.matedroid.domain.model.UnitFormatter
import com.matedroid.data.api.models.CarVersions
import com.matedroid.data.api.models.ChargingDetails
import com.matedroid.data.api.models.TpmsDetails
import com.matedroid.data.api.models.ClimateDetails
import com.matedroid.ui.theme.MateDroidTheme
import com.matedroid.ui.theme.StatusError
import com.matedroid.ui.theme.StatusSuccess
import com.matedroid.ui.theme.StatusWarning
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCharges: (carId: Int) -> Unit = {},
    onNavigateToDrives: (carId: Int) -> Unit = {},
    onNavigateToBattery: (carId: Int, efficiency: Double?) -> Unit = { _, _ -> },
    onNavigateToMileage: (carId: Int) -> Unit = {},
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
                        onNavigateToCharges = {
                            uiState.selectedCarId?.let { onNavigateToCharges(it) }
                        },
                        onNavigateToDrives = {
                            uiState.selectedCarId?.let { onNavigateToDrives(it) }
                        },
                        onNavigateToBattery = {
                            uiState.selectedCarId?.let { carId ->
                                onNavigateToBattery(carId, uiState.selectedCarEfficiency)
                            }
                        },
                        onNavigateToMileage = {
                            uiState.selectedCarId?.let { onNavigateToMileage(it) }
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
    onNavigateToCharges: () -> Unit = {},
    onNavigateToDrives: () -> Unit = {},
    onNavigateToBattery: () -> Unit = {},
    onNavigateToMileage: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compact status indicators (right-aligned)
            StatusIndicatorsRow(status, units)

            // Battery Section
            BatteryCard(status, units)

            // Location Section
            status.geofence?.let { geofence ->
                LocationCard(geofence = geofence, status = status)
            }

            // Vehicle Info Section
            VehicleInfoCard(status, units)
        }

        // Fixed bottom quick links
        QuickLinksRow(
            onNavigateToCharges = onNavigateToCharges,
            onNavigateToDrives = onNavigateToDrives,
            onNavigateToBattery = onNavigateToBattery,
            onNavigateToMileage = onNavigateToMileage,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StatusIndicatorsRow(status: CarStatus, units: Units?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: State and Lock
        Row(verticalAlignment = Alignment.CenterVertically) {
            // State indicator
            Icon(
                imageVector = Icons.Filled.Power,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.state?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        }

        // Right side: Temperatures
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Inside temp
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = status.insideTemp?.let { UnitFormatter.formatTemperature(it, units) } ?: "--",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Outside temp
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = status.outsideTemp?.let { UnitFormatter.formatTemperature(it, units) } ?: "--",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BatteryCard(status: CarStatus, units: Units?) {
    val batteryLevel = status.batteryLevel ?: 0
    val batteryColor = when {
        batteryLevel < 20 -> StatusError
        batteryLevel < 40 -> StatusWarning
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.BatteryChargingFull,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = batteryColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = batteryColor
                )
                if (status.isCharging) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ElectricBolt,
                        contentDescription = "Charging",
                        tint = StatusSuccess
                    )
                }
                if (batteryLevel > 90) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "High charge level",
                        tint = StatusWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rated Range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = status.ratedBatteryRangeKm?.let { UnitFormatter.formatDistance(it, units, 0) } ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Charge Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${status.chargeLimitSoc ?: "--"}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Charging progress bar - only shown when charging
            if (status.isCharging) {
                val chargeLimit = status.chargeLimitSoc ?: 100
                Spacer(modifier = Modifier.height(12.dp))
                ChargingProgressBar(
                    currentLevel = batteryLevel,
                    targetLevel = chargeLimit,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${status.chargerPower ?: 0} kW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = StatusSuccess
                    )
                    Text(
                        text = "+${status.chargeEnergyAdded?.let { "%.1f".format(it) } ?: "0"} kWh",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.timeToFullCharge?.let { formatHoursMinutes(it) } ?: "--",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargingProgressBar(
    currentLevel: Int,
    targetLevel: Int,
    modifier: Modifier = Modifier
) {
    val currentFraction = currentLevel / 100f
    val targetFraction = targetLevel / 100f
    val solidGreen = StatusSuccess
    val dimmedGreen = StatusSuccess.copy(alpha = 0.3f)
    val backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)

    Canvas(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        val width = size.width
        val height = size.height

        // Background (remaining after target)
        drawRect(
            color = backgroundColor,
            size = size
        )

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
    }
}

@Composable
private fun LocationCard(geofence: String, status: CarStatus) {
    val latitude = status.latitude
    val longitude = status.longitude

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    text = geofence,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Small map showing car location
            if (latitude != null && longitude != null) {
                Spacer(modifier = Modifier.width(12.dp))
                SmallLocationMap(
                    latitude = latitude,
                    longitude = longitude,
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
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = "MateDroid/1.0"
        onDispose { }
    }

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
        modifier = modifier
    )
}

@Composable
private fun VehicleInfoCard(status: CarStatus, units: Units?) {
    val distanceUnit = UnitFormatter.getDistanceUnit(units)
    val pressureUnit = UnitFormatter.getPressureUnit(units)
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vehicle Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Odometer",
                    value = status.odometer?.let {
                        val value = UnitFormatter.formatDistanceValue(it, units, 0)
                        "%,.0f $distanceUnit".format(value)
                    } ?: "--",
                    icon = Icons.Filled.Speed
                )
                InfoItem(
                    label = "Software",
                    value = status.version ?: "--",
                    icon = Icons.Filled.Settings
                )
            }

            // Tire pressure section
            if (tpms != null && (tpms.pressureFl != null || tpms.pressureFr != null)) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
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
private fun TirePressureDisplay(
    tpms: TpmsDetails,
    units: Units?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left tires column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TirePressureItem(
                label = "FL",
                pressure = tpms.pressureFl,
                hasWarning = tpms.warningFl == true,
                units = units
            )
            TirePressureItem(
                label = "RL",
                pressure = tpms.pressureRl,
                hasWarning = tpms.warningRl == true,
                units = units
            )
        }

        // Car icon in the middle
        Icon(
            imageVector = Icons.Filled.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )

        // Right tires column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TirePressureItem(
                label = "FR",
                pressure = tpms.pressureFr,
                hasWarning = tpms.warningFr == true,
                units = units
            )
            TirePressureItem(
                label = "RR",
                pressure = tpms.pressureRr,
                hasWarning = tpms.warningRr == true,
                units = units
            )
        }
    }
}

@Composable
private fun TirePressureItem(
    label: String,
    pressure: Double?,
    hasWarning: Boolean,
    units: Units?
) {
    val textColor = if (hasWarning) StatusWarning else MaterialTheme.colorScheme.onSurface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = pressure?.let { UnitFormatter.formatPressure(it, units, 2) } ?: "--",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
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
            icon = Icons.Filled.BatteryChargingFull,
            onClick = onNavigateToCharges
        )
        QuickLinkItem(
            title = "Drives",
            icon = Icons.Filled.DirectionsCar,
            onClick = onNavigateToDrives
        )
        QuickLinkItem(
            title = "Battery",
            icon = Icons.Filled.Battery5Bar,
            onClick = onNavigateToBattery
        )
        QuickLinkItem(
            title = "Mileage",
            icon = Icons.Filled.Timeline,
            onClick = onNavigateToMileage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.QuickLinkItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
