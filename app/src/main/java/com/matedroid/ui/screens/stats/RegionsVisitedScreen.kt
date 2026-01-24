package com.matedroid.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matedroid.R
import com.matedroid.domain.model.CountryRecord
import com.matedroid.domain.model.RegionRecord
import com.matedroid.domain.model.YearFilter
import com.matedroid.ui.theme.CarColorPalette
import com.matedroid.ui.theme.CarColorPalettes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsVisitedScreen(
    carId: Int,
    countryCode: String,
    countryName: String,
    yearFilter: YearFilter,
    exteriorColor: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: RegionsVisitedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val palette = remember(exteriorColor, isDarkTheme) {
        CarColorPalettes.forExteriorColor(exteriorColor, isDarkTheme)
    }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(carId, countryCode, yearFilter) {
        viewModel.loadRegions(carId, countryCode, yearFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.regions_visited_title, countryName)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_first_visit)) },
                                onClick = {
                                    viewModel.setSortOrder(RegionSortOrder.FIRST_VISIT)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_alphabetically)) },
                                onClick = {
                                    viewModel.setSortOrder(RegionSortOrder.ALPHABETICAL)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_drive_count)) },
                                onClick = {
                                    viewModel.setSortOrder(RegionSortOrder.DRIVE_COUNT)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_distance)) },
                                onClick = {
                                    viewModel.setSortOrder(RegionSortOrder.DISTANCE)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_energy)) },
                                onClick = {
                                    viewModel.setSortOrder(RegionSortOrder.ENERGY)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_charges)) },
                                onClick = {
                                    viewModel.setSortOrder(RegionSortOrder.CHARGES)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = palette.accent
                    )
                }
                uiState.regions.isEmpty() && uiState.countryRecord == null -> {
                    EmptyState(palette = palette)
                }
                else -> {
                    RegionsContent(
                        countryRecord = uiState.countryRecord,
                        regions = uiState.regions,
                        palette = palette
                    )
                }
            }
        }
    }
}

@Composable
private fun RegionsContent(
    countryRecord: CountryRecord?,
    regions: List<RegionRecord>,
    palette: CarColorPalette
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header card with country summary
        countryRecord?.let { country ->
            item(key = "header") {
                CountrySummaryCard(
                    country = country,
                    localizedName = getLocalizedCountryName(country.countryCode),
                    palette = palette
                )
            }
        }

        // Region cards
        items(regions, key = { it.regionName }) { region ->
            RegionCard(region = region, palette = palette)
        }
    }
}

@Composable
private fun CountrySummaryCard(
    country: CountryRecord,
    localizedName: String,
    palette: CarColorPalette
) {
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                spotColor = palette.onSurface.copy(alpha = 0.1f)
            )
            .clip(cardShape)
            .background(palette.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with flag and country name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag emoji
                Text(
                    text = country.flagEmoji,
                    fontSize = 40.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Country name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizedName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.onSurface
                    )
                }

                // Drive count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = country.driveCount.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.drives_count,
                            country.driveCount,
                            country.driveCount
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onSurfaceVariant
                    )
                }
            }

            // First and last visit dates
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.country_first_visit, formatDate(country.firstVisitDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.country_last_visit, formatDate(country.lastVisitDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.onSurfaceVariant
                )
            }

            // Stats row in chips
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    icon = Icons.Default.Route,
                    value = "%.0f km".format(country.totalDistanceKm),
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                StatChip(
                    icon = Icons.Default.ElectricBolt,
                    value = if (country.totalChargeEnergyKwh > 999) {
                        "%.1f MWh".format(country.totalChargeEnergyKwh / 1000)
                    } else {
                        "%.0f kWh".format(country.totalChargeEnergyKwh)
                    },
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                StatChip(
                    icon = Icons.Default.EvStation,
                    value = pluralStringResource(
                        R.plurals.charges_count,
                        country.chargeCount,
                        country.chargeCount
                    ),
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RegionCard(
    region: RegionRecord,
    palette: CarColorPalette
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = cardShape,
                spotColor = palette.onSurface.copy(alpha = 0.08f)
            )
            .clip(cardShape)
            .background(palette.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Region name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = region.regionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.onSurface
                    )
                }

                // Drive count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = region.driveCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = pluralStringResource(
                            R.plurals.drives_count,
                            region.driveCount,
                            region.driveCount
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onSurfaceVariant
                    )
                }
            }

            // Stats row
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    icon = Icons.Default.Route,
                    value = "%.0f km".format(region.totalDistanceKm),
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                StatChip(
                    icon = Icons.Default.ElectricBolt,
                    value = if (region.totalChargeEnergyKwh > 999) {
                        "%.1f MWh".format(region.totalChargeEnergyKwh / 1000)
                    } else {
                        "%.0f kWh".format(region.totalChargeEnergyKwh)
                    },
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                StatChip(
                    icon = Icons.Default.EvStation,
                    value = pluralStringResource(
                        R.plurals.charges_count,
                        region.chargeCount,
                        region.chargeCount
                    ),
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    palette: CarColorPalette,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.onSurface.copy(alpha = 0.05f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = palette.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = palette.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyState(palette: CarColorPalette) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.no_regions_found),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val dateTime = LocalDateTime.parse(dateStr, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        // Fallback: try parsing just the date portion
        try {
            dateStr.take(10)
        } catch (e2: Exception) {
            dateStr
        }
    }
}

/**
 * Get the localized country name for a given ISO country code.
 * Falls back to the country code if localization fails.
 */
private fun getLocalizedCountryName(countryCode: String): String {
    return try {
        Locale("", countryCode).getDisplayCountry(Locale.getDefault())
            .takeIf { it.isNotBlank() && it != countryCode } ?: countryCode
    } catch (e: Exception) {
        countryCode
    }
}
