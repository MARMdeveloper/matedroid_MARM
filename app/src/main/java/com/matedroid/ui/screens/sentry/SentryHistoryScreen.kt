package com.matedroid.ui.screens.sentry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matedroid.R
import com.matedroid.data.local.entity.SentryAlertLog
import com.matedroid.ui.theme.CarColorPalettes
import com.matedroid.ui.theme.StatusError
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentryHistoryScreen(
    carId: Int,
    exteriorColor: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: SentryHistoryViewModel = hiltViewModel()
) {
    viewModel.setCarId(carId)
    val uiState by viewModel.uiState.collectAsState()
    val palette = CarColorPalettes.forExteriorColor(exteriorColor, darkTheme = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sentry_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.surface,
                    titleContentColor = palette.onSurface,
                    navigationIconContentColor = palette.onSurface
                )
            )
        },
        containerColor = palette.surface
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "...",
                    color = palette.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // -- Current Session --
                item {
                    SectionHeader(
                        text = stringResource(R.string.sentry_history_current_session),
                        palette = palette
                    )
                }

                if (uiState.currentSessionAlerts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = palette.surface.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.sentry_history_no_alerts),
                                modifier = Modifier.padding(16.dp),
                                color = palette.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(uiState.currentSessionAlerts, key = { it.id }) { alert ->
                        AlertRow(alert = alert, palette = palette)
                    }
                }

                // -- Past Alerts --
                if (uiState.pastAlertsByDay.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            text = stringResource(R.string.sentry_history_past),
                            palette = palette
                        )
                    }

                    uiState.pastAlertsByDay.forEach { dayGroup ->
                        item(key = "day_${dayGroup.dateMillis}") {
                            DayHeader(dateMillis = dayGroup.dateMillis, palette = palette)
                        }
                        items(dayGroup.alerts, key = { it.id }) { alert ->
                            AlertRow(alert = alert, palette = palette)
                        }
                    }
                } else if (uiState.currentSessionAlerts.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.sentry_history_empty),
                            color = palette.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    palette: com.matedroid.ui.theme.CarColorPalette
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = palette.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun DayHeader(
    dateMillis: Long,
    palette: com.matedroid.ui.theme.CarColorPalette
) {
    val localDate = Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()

    val label = when (localDate) {
        today -> stringResource(R.string.sentry_history_today)
        today.minusDays(1) -> stringResource(R.string.sentry_history_yesterday)
        else -> localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = palette.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun AlertRow(
    alert: SentryAlertLog,
    palette: com.matedroid.ui.theme.CarColorPalette
) {
    val time = Instant.ofEpochMilli(alert.detectedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    val timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = palette.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tesla-style sentry indicator: red dot + grey ring
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(2.dp, palette.onSurfaceVariant.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(StatusError, CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sentry_alert_detected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onSurface
                )
            }
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = palette.onSurfaceVariant
            )
        }
    }
}
