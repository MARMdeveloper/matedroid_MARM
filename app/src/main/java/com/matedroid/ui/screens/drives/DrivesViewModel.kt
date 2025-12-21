package com.matedroid.ui.screens.drives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.api.models.DriveData
import com.matedroid.data.repository.ApiResult
import com.matedroid.data.repository.TeslamateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

enum class DriveChartGranularity {
    DAILY, WEEKLY, MONTHLY
}

data class DriveChartData(
    val label: String,
    val count: Int,
    val totalDistance: Double,
    val sortKey: Long
)

data class DrivesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val drives: List<DriveData> = emptyList(),
    val chartData: List<DriveChartData> = emptyList(),
    val chartGranularity: DriveChartGranularity = DriveChartGranularity.MONTHLY,
    val error: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val summary: DrivesSummary = DrivesSummary()
)

data class DrivesSummary(
    val totalDrives: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalDurationMin: Int = 0,
    val avgDistancePerDrive: Double = 0.0,
    val avgDurationPerDrive: Int = 0,
    val maxSpeedKmh: Int = 0
)

@HiltViewModel
class DrivesViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrivesUiState())
    val uiState: StateFlow<DrivesUiState> = _uiState.asStateFlow()

    private var carId: Int? = null

    fun setCarId(id: Int) {
        if (carId != id) {
            carId = id
            loadDrives()
        }
    }

    fun setDateFilter(startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.update { it.copy(startDate = startDate, endDate = endDate) }
        loadDrives(startDate, endDate)
    }

    fun clearDateFilter() {
        _uiState.update { it.copy(startDate = null, endDate = null) }
        loadDrives(null, null)
    }

    fun refresh() {
        carId?.let {
            _uiState.update { it.copy(isRefreshing = true) }
            val state = _uiState.value
            loadDrives(state.startDate, state.endDate)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadDrives(startDate: LocalDate? = null, endDate: LocalDate? = null) {
        val id = carId ?: return

        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isRefreshing) {
                _uiState.update { it.copy(isLoading = true) }
            }

            // API expects RFC3339 format: 2006-01-02T15:04:05Z
            val startDateStr = startDate?.let { "${it}T00:00:00Z" }
            val endDateStr = endDate?.let { "${it}T23:59:59Z" }

            when (val result = repository.getDrives(id, startDateStr, endDateStr)) {
                is ApiResult.Success -> {
                    val drives = result.data
                    val summary = calculateSummary(drives)
                    val granularity = determineGranularity(startDate, endDate)
                    val chartData = calculateChartData(drives, granularity)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            drives = drives,
                            chartData = chartData,
                            chartGranularity = granularity,
                            summary = summary,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun determineGranularity(startDate: LocalDate?, endDate: LocalDate?): DriveChartGranularity {
        if (startDate == null || endDate == null) return DriveChartGranularity.MONTHLY
        val days = ChronoUnit.DAYS.between(startDate, endDate)
        return when {
            days <= 30 -> DriveChartGranularity.DAILY
            days <= 90 -> DriveChartGranularity.WEEKLY
            else -> DriveChartGranularity.MONTHLY
        }
    }

    private fun calculateChartData(drives: List<DriveData>, granularity: DriveChartGranularity): List<DriveChartData> {
        if (drives.isEmpty()) return emptyList()

        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val weekFields = WeekFields.of(Locale.getDefault())

        return drives
            .mapNotNull { drive ->
                drive.startDate?.let { dateStr ->
                    try {
                        val date = LocalDate.parse(dateStr, formatter)
                        val (label, sortKey) = when (granularity) {
                            DriveChartGranularity.DAILY -> {
                                val dayLabel = date.format(DateTimeFormatter.ofPattern("d/M"))
                                dayLabel to date.toEpochDay()
                            }
                            DriveChartGranularity.WEEKLY -> {
                                val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
                                val year = date.get(weekFields.weekBasedYear())
                                "W$weekOfYear" to (year * 100L + weekOfYear)
                            }
                            DriveChartGranularity.MONTHLY -> {
                                val yearMonth = YearMonth.of(date.year, date.month)
                                yearMonth.format(DateTimeFormatter.ofPattern("MMM yy")) to (date.year * 12L + date.monthValue)
                            }
                        }
                        Triple(label, sortKey, drive)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .groupBy { Pair(it.first, it.second) }
            .map { (key, drivesInPeriod) ->
                DriveChartData(
                    label = key.first,
                    count = drivesInPeriod.size,
                    totalDistance = drivesInPeriod.sumOf { it.third.distance ?: 0.0 },
                    sortKey = key.second
                )
            }
            .sortedBy { it.sortKey }
    }

    private fun calculateSummary(drives: List<DriveData>): DrivesSummary {
        if (drives.isEmpty()) return DrivesSummary()

        val totalDistance = drives.sumOf { it.distance ?: 0.0 }
        val totalDuration = drives.sumOf { it.durationMin ?: 0 }
        val maxSpeed = drives.mapNotNull { it.speedMax }.maxOrNull() ?: 0
        val count = drives.size

        return DrivesSummary(
            totalDrives = count,
            totalDistanceKm = totalDistance,
            totalDurationMin = totalDuration,
            avgDistancePerDrive = if (count > 0) totalDistance / count else 0.0,
            avgDurationPerDrive = if (count > 0) totalDuration / count else 0,
            maxSpeedKmh = maxSpeed
        )
    }
}
