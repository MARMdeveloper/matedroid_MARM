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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matedroid.R
import com.matedroid.domain.model.CountryRecord
import com.matedroid.domain.model.YearFilter
import com.matedroid.ui.theme.CarColorPalette
import com.matedroid.ui.theme.CarColorPalettes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesVisitedScreen(
    carId: Int,
    yearFilter: YearFilter,
    exteriorColor: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: CountriesVisitedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val palette = remember(exteriorColor, isDarkTheme) {
        CarColorPalettes.forExteriorColor(exteriorColor, isDarkTheme)
    }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(carId, yearFilter) {
        viewModel.loadCountries(carId, yearFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.countries_visited_title)) },
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
                                    viewModel.setSortOrder(CountrySortOrder.FIRST_VISIT)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_alphabetically)) },
                                onClick = {
                                    viewModel.setSortOrder(CountrySortOrder.ALPHABETICAL)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_by_drive_count)) },
                                onClick = {
                                    viewModel.setSortOrder(CountrySortOrder.DRIVE_COUNT)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.surface,
                    titleContentColor = palette.onSurface,
                    navigationIconContentColor = palette.onSurface,
                    actionIconContentColor = palette.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.surface)
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = palette.accent
                    )
                }
                uiState.countries.isEmpty() -> {
                    EmptyState(palette = palette)
                }
                else -> {
                    CountriesList(
                        countries = uiState.countries,
                        palette = palette
                    )
                }
            }
        }
    }
}

@Composable
private fun CountriesList(
    countries: List<CountryRecord>,
    palette: CarColorPalette
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(countries, key = { it.countryCode }) { country ->
            CountryCard(country = country, palette = palette)
        }
    }
}

@Composable
private fun CountryCard(
    country: CountryRecord,
    palette: CarColorPalette
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = palette.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                // Flag emoji
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.accentDim),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = country.flagEmoji,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Country info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = country.countryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(
                            R.string.country_first_visit,
                            formatDate(country.firstVisitDate)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onSurfaceVariant
                    )

                    Text(
                        text = stringResource(
                            R.string.country_last_visit,
                            formatDate(country.lastVisitDate)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onSurfaceVariant
                    )
                }

                // Drive count
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = country.driveCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.drives_count,
                            country.driveCount,
                            country.driveCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onSurfaceVariant
                    )
                }
            }

            // Stats row with colorful icons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Distance
                StatItem(
                    icon = Icons.Default.Route,
                    value = "%.0f km".format(country.totalDistanceKm),
                    tint = palette.onSurfaceVariant
                )

                // Charge energy (MWh if > 999 kWh, otherwise kWh)
                StatItem(
                    icon = Icons.Default.ElectricBolt,
                    value = if (country.totalChargeEnergyKwh > 999) {
                        "%.1f MWh".format(country.totalChargeEnergyKwh / 1000)
                    } else {
                        "%.0f kWh".format(country.totalChargeEnergyKwh)
                    },
                    tint = palette.onSurfaceVariant
                )

                // Charge count
                StatItem(
                    icon = Icons.Default.EvStation,
                    value = pluralStringResource(
                        R.plurals.charges_count,
                        country.chargeCount,
                        country.chargeCount
                    ),
                    tint = palette.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
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
                text = stringResource(R.string.no_countries_found),
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
