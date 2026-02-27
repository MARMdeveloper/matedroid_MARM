package com.matedroid.ui.screens.mileage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.api.models.DriveData
import com.matedroid.data.api.models.Units
import com.matedroid.data.local.SettingsDataStore
import com.matedroid.data.model.Currency
import com.matedroid.data.repository.ApiResult
import com.matedroid.data.repository.StatsRepository
import com.matedroid.data.repository.TeslamateRepository
import com.matedroid.domain.model.YearFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeParseException
import javax.inject.Inject

data class YearlyMileage(
    val year: Int,
    val totalDistance: Double,
    val driveCount: Int,
    val totalEnergy: Double,
    val totalEnergyCost: Double? = null,
    val totalBatteryUsage: Double,
    val drives: List<DriveData>
)

data class MonthlyMileage(
    val yearMonth: YearMonth,
    val totalDistance: Double,
    val driveCount: Int,
    val totalEnergy: Double,
    val totalEnergyCost: Double? = null,
    val totalBatteryUsage: Double,
    val drives: List<DriveData>
)

data class DailyMileage(
    val date: LocalDate,
    val totalDistance: Double,
    val driveCount: Int,
    val totalEnergy: Double,
    val totalEnergyCost: Double? = null,
    val totalBatteryUsage: Double,
    val drives: List<DriveData>
)

data class MileageUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val allDrives: List<DriveData> = emptyList(),
    val currencySymbol: String = "€",
    val units: Units? = null,

    // Lifetime totals (year overview)
    val yearlyData: List<YearlyMileage> = emptyList(),
    val totalLifetimeDistance: Double = 0.0,
    val avgYearlyDistance: Double = 0.0,
    val firstDriveDate: LocalDate? = null,
    val totalLifetimeDriveCount: Int = 0,
    val totalLifetimeEnergy: Double = 0.0,
    val avgLifetimeEnergyDistance: Double = 0.0,
    val totalLifetimeEnergyCost: Double? = null,

    // Year detail view state
    val selectedYear: Int? = null,
    val monthlyData: List<MonthlyMileage> = emptyList(),
    val yearTotalDistance: Double = 0.0,
    val avgMonthlyDistance: Double = 0.0,
    val yearDriveCount: Int = 0,
    val yearTotalEnergy: Double = 0.0,
    val avgYearEnergyDistance: Double = 0.0,
    val yearTotalEnergyCost: Double? = null,

    // Month detail view state
    val selectedMonth: YearMonth? = null,
    val dailyData: List<DailyMileage> = emptyList(),

    // Day detail view state
    val selectedDay: LocalDate? = null,
    val selectedDayData: DailyMileage? = null
)

@HiltViewModel
class MileageViewModel @Inject constructor(
    private val repository: TeslamateRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MileageUiState())
    val uiState: StateFlow<MileageUiState> = _uiState.asStateFlow()

    private var carId: Int? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsDataStore.settings.first()
            val currency = Currency.findByCode(settings.currencyCode)
            _uiState.update { it.copy(currencySymbol = currency.symbol) }
        }
    }

    fun setCarId(id: Int) {
        if (carId != id) {
            carId = id
            viewModelScope.launch {
                val statusResult = repository.getCarStatus(id)
                if (statusResult is ApiResult.Success) {
                    _uiState.update { it.copy(units = statusResult.data.units) }
                }
            }
            loadAllDrives()
        }
    }

    fun refresh() {
        carId?.let {
            _uiState.update { it.copy(isRefreshing = true) }
            loadAllDrives()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        aggregateByMonth(year)
    }

    fun clearSelectedYear() {
        _uiState.update {
            it.copy(
                selectedYear = null,
                monthlyData = emptyList(),
                yearTotalDistance = 0.0,
                avgMonthlyDistance = 0.0,
                yearDriveCount = 0
            )
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = yearMonth) }
        aggregateByDay(yearMonth)
    }

    fun clearSelectedMonth() {
        _uiState.update { it.copy(selectedMonth = null, dailyData = emptyList()) }
    }

    fun selectDay(date: LocalDate) {
        val dayData = _uiState.value.dailyData.find { it.date == date }
        _uiState.update { it.copy(selectedDay = date, selectedDayData = dayData) }
    }

    fun clearSelectedDay() {
        _uiState.update { it.copy(selectedDay = null, selectedDayData = null) }
    }

    /**
     * Navigates directly to a specific day's detail view.
     * This auto-selects the year and month, then the day.
     */
    fun navigateToDay(date: LocalDate) {
        // First ensure the year is selected and month data is aggregated
        selectYear(date.year)
        // Then select the month and aggregate daily data
        selectMonth(YearMonth.of(date.year, date.month))
        // Finally select the day
        selectDay(date)
    }

    private fun loadEnergyCosts() {
        val id = carId ?: return
        viewModelScope.launch {
            try {
                val allTimeStats = statsRepository.getStats(id, YearFilter.AllTime)
                _uiState.update { it.copy(totalLifetimeEnergyCost = allTimeStats.quickStats.totalCost) }
                val updatedYearlyData = _uiState.value.yearlyData.map { yearData ->
                    val yearStats = statsRepository.getStats(id, YearFilter.Year(yearData.year))
                    yearData.copy(totalEnergyCost = yearStats.quickStats.totalCost)
                }
                _uiState.update { it.copy(yearlyData = updatedYearlyData) }

                val updatedMonthlyData = _uiState.value.monthlyData.map { monthData ->
                    val cost = statsRepository.getChargeCostForMonth(
                        id,
                        monthData.yearMonth.year,
                        monthData.yearMonth.monthValue
                    )
                    monthData.copy(totalEnergyCost = cost)
                }
                _uiState.update { it.copy(monthlyData = updatedMonthlyData) }

                val updatedDailyData = _uiState.value.dailyData.map { dayData ->
                    val cost = statsRepository.getChargeCostForDay(id, dayData.date)
                    dayData.copy(totalEnergyCost = cost)
                }
                _uiState.update { it.copy(dailyData = updatedDailyData) }

                // If there is a selected year, load the cost for that year as well
                val selectedYear = _uiState.value.selectedYear
                if (selectedYear != null) {
                    val yearStats = statsRepository.getStats(id, YearFilter.Year(selectedYear))
                    _uiState.update { it.copy(yearTotalEnergyCost = yearStats.quickStats.totalCost) }
                }
            } catch (e: Exception) {
                // If this fails, we just don't show the cost
            }
        }
    }

    private fun loadAllDrives() {
        val id = carId ?: return

        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isRefreshing) {
                _uiState.update { it.copy(isLoading = true) }
            }

            when (val result = repository.getDrives(id)) {
                is ApiResult.Success -> {
                    val drives = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            allDrives = drives,
                            error = null
                        )
                    }
                    aggregateByYear()
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

    private fun aggregateByYear() {
        val drives = _uiState.value.allDrives

        // Group by year
        val grouped = drives.groupBy { drive ->
            parseDateTime(drive.startDate)?.year
        }.filterKeys { it != null }

        // Create yearly aggregates
        val yearlyData = grouped.map { (year, yearDrives) ->
            val totalDistance = yearDrives.sumOf { it.distance ?: 0.0 }
            val totalEnergy = yearDrives.sumOf { it.energyConsumedNet ?: 0.0 }
            val totalEnergyCost = null
            val batteryUsages = yearDrives.mapNotNull { drive ->
                val start = drive.startBatteryLevel
                val end = drive.endBatteryLevel
                if (start != null && end != null) (start - end).toDouble() else null
            }
            val totalBatteryUsage = batteryUsages.sum()

            YearlyMileage(
                year = year!!,
                totalDistance = totalDistance,
                driveCount = yearDrives.size,
                totalEnergy = totalEnergy,
                totalBatteryUsage = totalBatteryUsage,
                drives = yearDrives
            )
        }.sortedByDescending { it.year }

        // Calculate lifetime totals
        val totalLifetimeDistance = yearlyData.sumOf { it.totalDistance }
        val totalLifetimeDriveCount = yearlyData.sumOf { it.driveCount }

        // Find the earliest drive date
        val firstDriveDate = drives.mapNotNull { parseDateTime(it.startDate)?.toLocalDate() }
            .minOrNull()

        // Calculate avg/year based on daily average × 365
        val avgYearlyDistance = if (firstDriveDate != null && totalLifetimeDistance > 0) {
            val daysSinceFirstDrive = java.time.temporal.ChronoUnit.DAYS.between(firstDriveDate, LocalDate.now())
            if (daysSinceFirstDrive > 0) {
                (totalLifetimeDistance / daysSinceFirstDrive) * 365
            } else {
                totalLifetimeDistance // If same day, just show total
            }
        } else {
            0.0
        }

        val totalEnergyKwh = yearlyData.sumOf { it.totalEnergy }
        val isImperial = _uiState.value.units?.isImperial == true
        val distanceForEfficiency = if (isImperial) totalLifetimeDistance * 0.621371 else totalLifetimeDistance
        val avgLifetimeEnergyDistance = if (distanceForEfficiency > 0)
            (totalEnergyKwh * 1000.0) / distanceForEfficiency else 0.0

        _uiState.update {
            it.copy(
                yearlyData = yearlyData,
                totalLifetimeDistance = totalLifetimeDistance,
                avgYearlyDistance = avgYearlyDistance,
                firstDriveDate = firstDriveDate,
                totalLifetimeDriveCount = totalLifetimeDriveCount,
                totalLifetimeEnergy = yearlyData.sumOf { it.totalEnergy },
                avgLifetimeEnergyDistance = avgLifetimeEnergyDistance
            )
        }
        loadEnergyCosts()
    }

    private fun aggregateByMonth(year: Int) {
        val drives = _uiState.value.allDrives

        // Filter drives for selected year
        val yearDrives = drives.filter { drive ->
            parseDateTime(drive.startDate)?.year == year
        }

        // Group by month
        val grouped = yearDrives.groupBy { drive ->
            val dateTime = parseDateTime(drive.startDate)
            if (dateTime != null) {
                YearMonth.of(dateTime.year, dateTime.month)
            } else {
                null
            }
        }.filterKeys { it != null }

        // Create monthly aggregates
        val monthlyData = grouped.map { (yearMonth, monthDrives) ->
            val totalDistance = monthDrives.sumOf { it.distance ?: 0.0 }
            val totalEnergy = monthDrives.sumOf { it.energyConsumedNet ?: 0.0 }
            val batteryUsages = monthDrives.mapNotNull { drive ->
                val start = drive.startBatteryLevel
                val end = drive.endBatteryLevel
                if (start != null && end != null) (start - end).toDouble() else null
            }
            val totalBatteryUsage = batteryUsages.sum()

            MonthlyMileage(
                yearMonth = yearMonth!!,
                totalDistance = totalDistance,
                driveCount = monthDrives.size,
                totalEnergy = totalEnergy,
                totalBatteryUsage = totalBatteryUsage,
                drives = monthDrives
            )
        }.sortedByDescending { it.yearMonth }

        // Calculate totals for selected year
        val yearTotalDistance = monthlyData.sumOf { it.totalDistance }
        val yearDriveCount = monthlyData.sumOf { it.driveCount }
        val avgMonthlyDistance = if (monthlyData.isNotEmpty()) yearTotalDistance / monthlyData.size else 0.0
        val totalEnergyKwh = monthlyData.sumOf { it.totalEnergy }
        val isImperial = _uiState.value.units?.isImperial == true
        val distanceForEfficiency = if (isImperial) yearTotalDistance * 0.621371 else yearTotalDistance
        val avgYearEnergyDistance = if (distanceForEfficiency > 0)
            (totalEnergyKwh * 1000.0) / distanceForEfficiency else 0.0

        _uiState.update {
            it.copy(
                monthlyData = monthlyData,
                yearTotalDistance = yearTotalDistance,
                avgMonthlyDistance = avgMonthlyDistance,
                yearDriveCount = yearDriveCount,
                yearTotalEnergy = monthlyData.sumOf { it.totalEnergy },
                avgYearEnergyDistance = avgYearEnergyDistance
            )
        }
        loadEnergyCosts()
    }

    private fun aggregateByDay(yearMonth: YearMonth) {
        val state = _uiState.value
        val drives = state.allDrives

        // Filter drives for selected month
        val monthDrives = drives.filter { drive ->
            val dateTime = parseDateTime(drive.startDate)
            dateTime != null && YearMonth.of(dateTime.year, dateTime.month) == yearMonth
        }

        // Group by day
        val grouped = monthDrives.groupBy { drive ->
            parseDateTime(drive.startDate)?.toLocalDate()
        }.filterKeys { it != null }

        // Create daily aggregates
        val dailyData = grouped.map { (date, dayDrives) ->
            val totalDistance = dayDrives.sumOf { it.distance ?: 0.0 }
            val totalEnergy = dayDrives.sumOf { it.energyConsumedNet ?: 0.0 }
            val batteryUsages = dayDrives.mapNotNull { drive ->
                val start = drive.startBatteryLevel
                val end = drive.endBatteryLevel
                if (start != null && end != null) (start - end).toDouble() else null
            }
            val totalBatteryUsage = batteryUsages.sum()

            DailyMileage(
                date = date!!,
                totalDistance = totalDistance,
                driveCount = dayDrives.size,
                totalEnergy = totalEnergy,
                totalBatteryUsage = totalBatteryUsage,
                drives = dayDrives.sortedByDescending { it.startDate }
            )
        }.sortedByDescending { it.date }

        _uiState.update {
            it.copy(dailyData = dailyData)
        }
        loadEnergyCosts()
    }

    private fun parseDateTime(dateStr: String?): LocalDateTime? {
        if (dateStr == null) return null
        return try {
            // Parse as OffsetDateTime (handles timezone like +01:00)
            OffsetDateTime.parse(dateStr).toLocalDateTime()
        } catch (e: DateTimeParseException) {
            try {
                // Fallback: try parsing as LocalDateTime directly
                LocalDateTime.parse(dateStr.replace("Z", ""))
            } catch (e2: Exception) {
                null
            }
        }
    }

    // Get yearly data for chart
    fun getYearlyChartData(): List<Pair<Int, Double>> {
        val state = _uiState.value
        return state.yearlyData.map { it.year to it.totalDistance }
            .sortedBy { it.first }
    }

    // Get monthly data for chart (all 12 months, with 0 for missing months)
    fun getMonthlyChartData(): List<Pair<Int, Double>> {
        val state = _uiState.value
        val monthlyMap = state.monthlyData.associate { it.yearMonth.monthValue to it.totalDistance }

        return (1..12).map { month ->
            month to (monthlyMap[month] ?: 0.0)
        }
    }

    // Get daily data for chart within selected month
    fun getDailyChartData(): List<Pair<Int, Double>> {
        val state = _uiState.value
        val selectedMonth = state.selectedMonth ?: return emptyList()
        val dailyMap = state.dailyData.associate { it.date.dayOfMonth to it.totalDistance }

        val daysInMonth = selectedMonth.lengthOfMonth()
        return (1..daysInMonth).map { day ->
            day to (dailyMap[day] ?: 0.0)
        }.filter { it.second > 0 } // Only return days with data for cleaner chart
    }
}
