package com.matedroid.ui.screens.drives

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import com.matedroid.ui.icons.CustomIcons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import com.matedroid.R
import com.matedroid.data.api.models.DriveDetail
import com.matedroid.data.api.models.DrivePosition
import com.matedroid.data.api.models.Units
import com.matedroid.data.repository.WeatherPoint
import com.matedroid.domain.model.UnitFormatter
import com.matedroid.ui.components.AnnotationRange
import com.matedroid.ui.components.BarChartData
import com.matedroid.ui.components.FullscreenLineChart
import com.matedroid.ui.components.InteractiveBarChart
import com.matedroid.ui.theme.CarColorPalettes
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveDetailScreen(
    carId: Int,
    driveId: Int,
    exteriorColor: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: DriveDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDarkTheme = isSystemInDarkTheme()
    val palette = CarColorPalettes.forExteriorColor(exteriorColor, isDarkTheme)

    LaunchedEffect(carId, driveId) {
        viewModel.loadDriveDetail(carId, driveId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.drive_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.driveDetail?.let { detail ->
                DriveDetailContent(
                    detail = detail,
                    stats = uiState.stats,
                    units = uiState.units,
                    routeColor = palette.accent,
                    weatherPoints = uiState.weatherPoints,
                    isLoadingWeather = uiState.isLoadingWeather,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun DriveDetailContent(
    detail: DriveDetail,
    stats: DriveDetailStats?,
    units: Units?,
    routeColor: Color,
    weatherPoints: List<WeatherPoint>,
    isLoadingWeather: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var sharedXFraction by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .collect { isScrolling -> if (isScrolling) sharedXFraction = null }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) { detectTapGestures { sharedXFraction = null } }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route header card
        RouteHeaderCard(detail = detail)

        // Map showing the route
        if (!detail.positions.isNullOrEmpty()) {
            DriveMapCard(positions = detail.positions, routeColor = routeColor)
        }

        // Stats grid
        stats?.let { s ->
            // Speed section
            StatsSectionCard(
                title = stringResource(R.string.speed),
                icon = Icons.Default.Speed,
                stats = listOf(
                    StatItem(stringResource(R.string.maximum), UnitFormatter.formatSpeed(s.speedMax.toDouble(), units)),
                    StatItem(stringResource(R.string.average), UnitFormatter.formatSpeed(s.speedAvg, units)),
                    StatItem(stringResource(R.string.avg_distance), UnitFormatter.formatSpeed(s.avgSpeedFromDistance, units))
                )
            )

            // Distance & Duration section
            StatsSectionCard(
                title = stringResource(R.string.trip),
                icon = CustomIcons.SteeringWheel,
                stats = listOf(
                    StatItem(stringResource(R.string.distance), UnitFormatter.formatDistance(s.distance, units)),
                    StatItem(stringResource(R.string.duration), formatDuration(s.durationMin)),
                    StatItem(stringResource(R.string.efficiency), UnitFormatter.formatEfficiency(s.efficiency, units))
                )
            )

            // Battery section
            StatsSectionCard(
                title = stringResource(R.string.battery),
                icon = Icons.Default.BatteryChargingFull,
                stats = listOf(
                    StatItem(stringResource(R.string.start), "${s.batteryStart}%"),
                    StatItem(stringResource(R.string.end), "${s.batteryEnd}%"),
                    StatItem(stringResource(R.string.used), "${s.batteryUsed}%"),
                    StatItem(stringResource(R.string.energy), "%.2f kWh".format(s.energyUsed))
                )
            )

            // Power section
            StatsSectionCard(
                title = stringResource(R.string.power),
                icon = Icons.Default.Bolt,
                stats = listOf(
                    StatItem(stringResource(R.string.max_accel), "${s.powerMax} kW"),
                    StatItem(stringResource(R.string.min_regen), "${s.powerMin} kW"),
                    StatItem(stringResource(R.string.average), "%.1f kW".format(s.powerAvg))
                )
            )

            // Elevation section
            if (s.elevationMax > 0 || s.elevationMin > 0) {
                StatsSectionCard(
                    title = stringResource(R.string.elevation),
                    icon = Icons.Default.Landscape,
                    stats = listOf(
                        StatItem(stringResource(R.string.maximum), UnitFormatter.formatElevation(s.elevationMax, units)),
                        StatItem(stringResource(R.string.minimum), UnitFormatter.formatElevation(s.elevationMin, units)),
                        StatItem(stringResource(R.string.gain), "+" + UnitFormatter.formatElevation(s.elevationGain, units)),
                        StatItem(stringResource(R.string.loss), "-" + UnitFormatter.formatElevation(s.elevationLoss, units)),
                    )
                )
            }

            // Temperature section
            if (s.outsideTempAvg != null || s.insideTempAvg != null) {
                StatsSectionCard(
                    title = stringResource(R.string.temperature),
                    icon = Icons.Default.DeviceThermostat,
                    stats = listOfNotNull(
                        s.outsideTempAvg?.let { StatItem(stringResource(R.string.outside), UnitFormatter.formatTemperature(it, units)) },
                        s.insideTempAvg?.let { StatItem(stringResource(R.string.inside), UnitFormatter.formatTemperature(it, units)) }
                    )
                )
            }

            // Charts
            if (!detail.positions.isNullOrEmpty() && detail.positions.size > 2) {
                // Extract time labels for X axis (5 labels: start, 1st quarter, half, 3rd quarter, end)
                val timeLabels = extractTimeLabels(detail.positions)
                val positions = detail.positions
                val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                val fractionToTimeLabel: (Float) -> String = { fraction ->
                    val index = (fraction * positions.lastIndex).roundToInt().coerceIn(0, positions.lastIndex)
                    positions[index].date?.let { dateStr ->
                        try {
                            val dt = try {
                                java.time.OffsetDateTime.parse(dateStr).toLocalDateTime()
                            } catch (e: java.time.format.DateTimeParseException) {
                                java.time.LocalDateTime.parse(dateStr.replace("Z", ""))
                            }
                            dt.format(timeFormatter)
                        } catch (e: Exception) { "" }
                    } ?: ""
                }

                // Compute battery heater annotation ranges
                val heaterAnnotations = remember(positions) {
                    computeBatteryHeaterRanges(positions)
                }

                SpeedChartCard(
                    positions = detail.positions,
                    units = units,
                    timeLabels = timeLabels,
                    externalSelectedFraction = sharedXFraction,
                    onXSelected = { sharedXFraction = it },
                    fractionToTimeLabel = fractionToTimeLabel
                )
                PowerChartCard(
                    positions = detail.positions,
                    timeLabels = timeLabels,
                    externalSelectedFraction = sharedXFraction,
                    onXSelected = { sharedXFraction = it },
                    fractionToTimeLabel = fractionToTimeLabel,
                    annotationRanges = heaterAnnotations
                )
                BatteryChartCard(
                    positions = detail.positions,
                    timeLabels = timeLabels,
                    externalSelectedFraction = sharedXFraction,
                    onXSelected = { sharedXFraction = it },
                    fractionToTimeLabel = fractionToTimeLabel,
                    annotationRanges = heaterAnnotations
                )
                if (detail.positions.any { it.elevation != null && it.elevation != 0 }) {
                    ElevationChartCard(
                        positions = detail.positions,
                        units = units,
                        timeLabels = timeLabels,
                        externalSelectedFraction = sharedXFraction,
                        onXSelected = { sharedXFraction = it },
                        fractionToTimeLabel = fractionToTimeLabel
                    )
                }

                SpeedHistogramCard(
                    positions = detail.positions,
                    units = units
                )
            }
        }

        // Weather along the way - shown when loading or has data
        if (isLoadingWeather || weatherPoints.isNotEmpty()) {
            WeatherAlongTheWayCard(
                weatherPoints = weatherPoints,
                units = units,
                isLoading = isLoadingWeather
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RouteHeaderCard(detail: DriveDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.from),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = detail.startAddress ?: stringResource(R.string.unknown_location),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 36.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            // End location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.to),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = detail.endAddress ?: stringResource(R.string.unknown_location),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 36.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            // Start time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.started),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatDateTime(detail.startDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // End time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.ended),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatDateTime(detail.endDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    detail.durationStr?.let { duration ->
                        Text(
                            text = stringResource(R.string.duration_label, duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveMapCard(positions: List<DrivePosition>, routeColor: Color) {
    val context = LocalContext.current
    val routeColorArgb = routeColor.toArgb()
    val validPositions = positions.filter { it.latitude != null && it.longitude != null }

    if (validPositions.isEmpty()) return

    val startPoint = validPositions.firstOrNull()
    val endPoint = validPositions.lastOrNull()

    fun openInMaps() {
        if (startPoint != null && endPoint != null) {
            // Open Google Maps with directions from start to end
            val uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                        "&origin=${startPoint.latitude},${startPoint.longitude}" +
                        "&destination=${endPoint.latitude},${endPoint.longitude}" +
                        "&travelmode=driving"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openInMaps() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.route_map),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            // Create polyline for the route
                            val geoPoints = validPositions.map { pos ->
                                GeoPoint(pos.latitude!!, pos.longitude!!)
                            }

                            val polyline = Polyline().apply {
                                setPoints(geoPoints)
                                outlinePaint.color = routeColorArgb
                                outlinePaint.strokeWidth = 8f
                                outlinePaint.strokeCap = Paint.Cap.ROUND
                                outlinePaint.strokeJoin = Paint.Join.ROUND
                            }
                            overlays.add(polyline)

                            // Calculate bounding box with padding
                            if (geoPoints.isNotEmpty()) {
                                val north = geoPoints.maxOf { it.latitude }
                                val south = geoPoints.minOf { it.latitude }
                                val east = geoPoints.maxOf { it.longitude }
                                val west = geoPoints.minOf { it.longitude }

                                // Add some padding
                                val latPadding = (north - south) * 0.15
                                val lonPadding = (east - west) * 0.15

                                val boundingBox = BoundingBox(
                                    north + latPadding,
                                    east + lonPadding,
                                    south - latPadding,
                                    west - lonPadding
                                )

                                post {
                                    zoomToBoundingBox(boundingBox, false)
                                    invalidate()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

data class StatItem(val label: String, val value: String)

@Composable
private fun StatsSectionCard(
    title: String,
    icon: ImageVector,
    stats: List<StatItem>
) {
    // Get the current screen settings
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Define how many columns we want according to the available screen width
    val columnCount = when {
        screenWidth > 600 -> 4 // Big screen or landscape orientation
        screenWidth > 340 -> 3 // Standard screen
        else -> 2              // Small screen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divide the list of statistics according to the calculated number of columns
            val chunked = stats.chunked(columnCount)
            chunked.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { stat ->
                        StatItemView(
                            label = stat.label,
                            value = stat.value,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fill the leftover space if the last row is not complete.
                    // This prevents a single item from stretching too much
                    val emptySlots = columnCount - row.size
                    if (emptySlots > 0) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (index < chunked.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItemView(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SpeedChartCard(
    positions: List<DrivePosition>,
    units: Units?,
    timeLabels: List<String>,
    externalSelectedFraction: Float? = null,
    onXSelected: ((Float?) -> Unit)? = null,
    fractionToTimeLabel: ((Float) -> String)? = null
) {
    val speeds = positions.mapNotNull { it.speed?.toFloat() }
    if (speeds.size < 2) return

    ChartCard(
        title = stringResource(R.string.speed_profile),
        icon = Icons.Default.Speed,
        data = speeds,
        color = MaterialTheme.colorScheme.primary,
        unit = UnitFormatter.getSpeedUnit(units),
        timeLabels = timeLabels,
        externalSelectedFraction = externalSelectedFraction,
        onXSelected = onXSelected,
        fractionToTimeLabel = fractionToTimeLabel,
        convertValue = { value ->
            if (units?.isImperial == true) (value * 0.621371f) else value
        }
    )
}

@Composable
private fun PowerChartCard(
    positions: List<DrivePosition>,
    timeLabels: List<String>,
    externalSelectedFraction: Float? = null,
    onXSelected: ((Float?) -> Unit)? = null,
    fractionToTimeLabel: ((Float) -> String)? = null,
    annotationRanges: List<AnnotationRange> = emptyList()
) {
    val powers = positions.mapNotNull { it.power?.toFloat() }
    if (powers.size < 2) return

    ChartCard(
        title = stringResource(R.string.power_profile),
        icon = Icons.Default.Bolt,
        data = powers,
        color = MaterialTheme.colorScheme.tertiary,
        unit = "kW",
        showZeroLine = true,
        timeLabels = timeLabels,
        externalSelectedFraction = externalSelectedFraction,
        onXSelected = onXSelected,
        fractionToTimeLabel = fractionToTimeLabel,
        annotationRanges = annotationRanges
    )
}

@Composable
private fun BatteryChartCard(
    positions: List<DrivePosition>,
    timeLabels: List<String>,
    externalSelectedFraction: Float? = null,
    onXSelected: ((Float?) -> Unit)? = null,
    fractionToTimeLabel: ((Float) -> String)? = null,
    annotationRanges: List<AnnotationRange> = emptyList()
) {
    val batteryLevels = positions.mapNotNull { it.batteryLevel?.toFloat() }
    if (batteryLevels.size < 2) return

    ChartCard(
        title = stringResource(R.string.battery_level),
        icon = Icons.Default.BatteryChargingFull,
        data = batteryLevels,
        color = MaterialTheme.colorScheme.secondary,
        unit = "%",
        fixedMinMax = Pair(0f, 100f),
        timeLabels = timeLabels,
        externalSelectedFraction = externalSelectedFraction,
        onXSelected = onXSelected,
        fractionToTimeLabel = fractionToTimeLabel,
        annotationRanges = annotationRanges
    )
}

@Composable
private fun ElevationChartCard(
    positions: List<DrivePosition>,
    units: Units?,
    timeLabels: List<String>,
    externalSelectedFraction: Float? = null,
    onXSelected: ((Float?) -> Unit)? = null,
    fractionToTimeLabel: ((Float) -> String)? = null
) {
    val elevations = positions.mapNotNull { it.elevation?.toFloat() }
    if (elevations.size < 2) return

    ChartCard(
        title = stringResource(R.string.elevation_profile),
        icon = Icons.Default.Landscape,
        data = elevations,
        color = Color(0xFF8B4513), // Brown color for terrain
        unit = UnitFormatter.getElevationUnit(units),
        timeLabels = timeLabels,
        externalSelectedFraction = externalSelectedFraction,
        onXSelected = onXSelected,
        fractionToTimeLabel = fractionToTimeLabel,
        convertValue = { UnitFormatter.getElevationValue(it, units) }
    )
}

@Composable
private fun SpeedHistogramCard(
    positions: List<DrivePosition>,
    units: Units?
) {
    val speeds = positions.mapNotNull { it.speed }
    if (speeds.size < 2) return

    val isImperial = units?.isImperial == true
    val bucketSize = if (isImperial) 5 else 10
    val speedUnit = UnitFormatter.getSpeedUnit(units)

    val histogramData = remember(speeds, bucketSize) {
        buildSpeedHistogram(speeds, bucketSize)
    }

    if (histogramData.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.speed_distribution),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            InteractiveBarChart(
                data = histogramData,
                barColor = MaterialTheme.colorScheme.primary,
                showEveryNthLabel = if (histogramData.size > 8) 2 else 1,
                valueFormatter = { pct ->
                    if (pct < 10.0) "%.1f%%".format(pct) else "%.0f%%".format(pct)
                },
                yAxisFormatter = { pct ->
                    if (pct < 10.0) "%.1f%%".format(pct) else "%.0f%%".format(pct)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Builds speed histogram data with buckets of [bucketSize] units.
 * Returns bar chart data with percentage values.
 *
 * Note: The TeslaMate API pre-converts speed to the user's unit system,
 * so speeds are already in km/h or mph — no conversion needed here.
 */
private fun buildSpeedHistogram(
    speeds: List<Int>,
    bucketSize: Int
): List<BarChartData> {
    if (speeds.isEmpty()) return emptyList()

    val minSpeed = (speeds.min() / bucketSize) * bucketSize
    val maxSpeed = ((speeds.max() / bucketSize) + 1) * bucketSize
    val total = speeds.size.toDouble()

    val buckets = mutableListOf<BarChartData>()
    var bucketStart = minSpeed
    while (bucketStart < maxSpeed) {
        val bucketEnd = bucketStart + bucketSize
        val count = speeds.count { it >= bucketStart && it < bucketEnd }
        val pct = (count / total) * 100.0
        buckets.add(
            BarChartData(
                label = "$bucketStart",
                value = pct,
                displayValue = if (pct < 10.0) "%.1f%%".format(pct) else "%.0f%%".format(pct)
            )
        )
        bucketStart = bucketEnd
    }

    return buckets
}

@Composable
private fun ChartCard(
    title: String,
    icon: ImageVector,
    data: List<Float>,
    color: Color,
    unit: String,
    showZeroLine: Boolean = false,
    fixedMinMax: Pair<Float, Float>? = null,
    timeLabels: List<String> = emptyList(),
    convertValue: (Float) -> Float = { it },
    externalSelectedFraction: Float? = null,
    onXSelected: ((Float?) -> Unit)? = null,
    fractionToTimeLabel: ((Float) -> String)? = null,
    annotationRanges: List<AnnotationRange> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            FullscreenLineChart(
                data = data,
                color = color,
                unit = unit,
                showZeroLine = showZeroLine,
                fixedMinMax = fixedMinMax,
                timeLabels = timeLabels,
                convertValue = convertValue,
                externalSelectedFraction = externalSelectedFraction,
                onXSelected = onXSelected,
                fractionToTimeLabel = fractionToTimeLabel,
                annotationRanges = annotationRanges,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Extract 5 time labels from drive positions for X axis display.
 * Returns list of 5 time strings at 0%, 25%, 50%, 75%, and 100% positions.
 * Following the chart guidelines: start, 1st quarter, half, 3rd quarter, end.
 */
private fun extractTimeLabels(positions: List<DrivePosition>): List<String> {
    if (positions.isEmpty()) return listOf("", "", "", "", "")

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val times = positions.mapNotNull { position ->
        position.date?.let { dateStr ->
            try {
                val dateTime = try {
                    OffsetDateTime.parse(dateStr).toLocalDateTime()
                } catch (e: DateTimeParseException) {
                    LocalDateTime.parse(dateStr.replace("Z", ""))
                }
                dateTime
            } catch (e: Exception) {
                null
            }
        }
    }

    if (times.isEmpty()) return listOf("", "", "", "", "")

    // 5 positions: start (0%), 1st quarter (25%), half (50%), 3rd quarter (75%), end (100%)
    val indices = listOf(0, times.size / 4, times.size / 2, times.size * 3 / 4, times.size - 1)
    return indices.map { idx ->
        times.getOrNull(idx.coerceIn(0, times.size - 1))?.format(timeFormatter) ?: ""
    }
}

private fun formatDateTime(dateStr: String?): String {
    if (dateStr == null) return "Unknown"
    return try {
        val dateTime = try {
            OffsetDateTime.parse(dateStr).toLocalDateTime()
        } catch (e: DateTimeParseException) {
            LocalDateTime.parse(dateStr.replace("Z", ""))
        }
        // Use locale-aware formatter for proper date/time localization
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return "%d:%02d".format(hours, mins)
}

/**
 * Computes annotation ranges for periods where the battery heater was active.
 *
 * The battery_heater field tends to flap (single-point true surrounded by false/null gaps
 * of ~30-60 positions) because the heater cycles on/off rapidly. To produce clean Grafana-style
 * bands, nearby true-runs separated by gaps smaller than [mergeGap] positions are merged
 * into a single continuous range.
 *
 * Returns fractional ranges (0.0–1.0) suitable for chart annotation overlays.
 */
private fun computeBatteryHeaterRanges(
    positions: List<DrivePosition>,
    mergeGap: Int = 80
): List<AnnotationRange> {
    if (positions.size < 2) return emptyList()

    // Collect indices where heater is on
    val heaterIndices = positions.indices.filter { positions[it].isBatteryHeaterOn }
    if (heaterIndices.isEmpty()) return emptyList()

    val heaterColor = Color(0xFFFF9800) // Material Orange
    val lastIndex = positions.lastIndex.toFloat()

    // Merge nearby indices into contiguous ranges
    val ranges = mutableListOf<AnnotationRange>()
    var rangeStart = heaterIndices[0]
    var rangeEnd = heaterIndices[0]

    for (i in 1 until heaterIndices.size) {
        if (heaterIndices[i] - rangeEnd <= mergeGap) {
            // Close enough — extend current range
            rangeEnd = heaterIndices[i]
        } else {
            // Gap too large — emit current range and start a new one
            ranges.add(
                AnnotationRange(
                    startFraction = rangeStart / lastIndex,
                    endFraction = rangeEnd / lastIndex,
                    color = heaterColor
                )
            )
            rangeStart = heaterIndices[i]
            rangeEnd = heaterIndices[i]
        }
    }
    // Emit last range
    ranges.add(
        AnnotationRange(
            startFraction = rangeStart / lastIndex,
            endFraction = rangeEnd / lastIndex,
            color = heaterColor
        )
    )

    return ranges
}
