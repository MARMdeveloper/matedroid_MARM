package com.matedroid.ui.screens.charges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.api.models.ChargeData
import com.matedroid.data.local.SettingsDataStore
import com.matedroid.data.model.Currency
import com.matedroid.data.repository.ApiResult
import com.matedroid.data.repository.TeslamateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

enum class ChartGranularity {
    DAILY, WEEKLY, MONTHLY
}

data class ChargeChartData(
    val label: String,
    val count: Int,
    val totalEnergy: Double,
    val sortKey: Long // For sorting (epoch day, week number, or year-month)
)

data class ChargesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val charges: List<ChargeData> = emptyList(),
    val chartData: List<ChargeChartData> = emptyList(),
    val chartGranularity: ChartGranularity = ChartGranularity.MONTHLY,
    val error: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val summary: ChargesSummary = ChargesSummary(),
    val currencySymbol: String = "€"
)

data class ChargesSummary(
    val totalCharges: Int = 0,
    val totalEnergyAdded: Double = 0.0,
    val totalCost: Double = 0.0,
    val avgEnergyPerCharge: Double = 0.0,
    val avgCostPerCharge: Double = 0.0
)

@HiltViewModel
class ChargesViewModel @Inject constructor(
    private val repository: TeslamateRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChargesUiState())
    val uiState: StateFlow<ChargesUiState> = _uiState.asStateFlow()

    private var carId: Int? = null

    init {
        loadCurrency()
    }

    private fun loadCurrency() {
        viewModelScope.launch {
            val settings = settingsDataStore.settings.first()
            val currency = Currency.findByCode(settings.currencyCode)
            _uiState.update { it.copy(currencySymbol = currency.symbol) }
        }
    }

    fun setCarId(id: Int) {
        if (carId != id) {
            carId = id
            loadCharges()
        }
    }

    fun setDateFilter(startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.update { it.copy(startDate = startDate, endDate = endDate) }
        loadCharges(startDate, endDate)
    }

    fun clearDateFilter() {
        _uiState.update { it.copy(startDate = null, endDate = null) }
        loadCharges(null, null)
    }

    fun refresh() {
        carId?.let {
            _uiState.update { it.copy(isRefreshing = true) }
            val state = _uiState.value
            loadCharges(state.startDate, state.endDate)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadCharges(startDate: LocalDate? = null, endDate: LocalDate? = null) {
        val id = carId ?: return

        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isRefreshing) {
                _uiState.update { it.copy(isLoading = true) }
            }

            // API expects RFC3339 format: 2006-01-02T15:04:05Z
            val startDateStr = startDate?.let { "${it}T00:00:00Z" }
            val endDateStr = endDate?.let { "${it}T23:59:59Z" }

            when (val result = repository.getCharges(id, startDateStr, endDateStr)) {
                is ApiResult.Success -> {
                    val charges = result.data
                    val summary = calculateSummary(charges)
                    val granularity = determineGranularity(startDate, endDate)
                    val chartData = calculateChartData(charges, granularity)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            charges = charges,
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

    private fun determineGranularity(startDate: LocalDate?, endDate: LocalDate?): ChartGranularity {
        if (startDate == null || endDate == null) return ChartGranularity.MONTHLY
        val days = ChronoUnit.DAYS.between(startDate, endDate)
        return when {
            days <= 30 -> ChartGranularity.DAILY
            days <= 90 -> ChartGranularity.WEEKLY
            else -> ChartGranularity.MONTHLY
        }
    }

    private fun calculateChartData(charges: List<ChargeData>, granularity: ChartGranularity): List<ChargeChartData> {
        if (charges.isEmpty()) return emptyList()

        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val weekFields = WeekFields.of(Locale.getDefault())

        return charges
            .mapNotNull { charge ->
                charge.startDate?.let { dateStr ->
                    try {
                        val date = LocalDate.parse(dateStr, formatter)
                        val (label, sortKey) = when (granularity) {
                            ChartGranularity.DAILY -> {
                                val dayLabel = date.format(DateTimeFormatter.ofPattern("d/M"))
                                dayLabel to date.toEpochDay()
                            }
                            ChartGranularity.WEEKLY -> {
                                val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
                                val year = date.get(weekFields.weekBasedYear())
                                "W$weekOfYear" to (year * 100L + weekOfYear)
                            }
                            ChartGranularity.MONTHLY -> {
                                val yearMonth = YearMonth.of(date.year, date.month)
                                yearMonth.format(DateTimeFormatter.ofPattern("MMM yy")) to (date.year * 12L + date.monthValue)
                            }
                        }
                        Triple(label, sortKey, charge)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .groupBy { Pair(it.first, it.second) }
            .map { (key, chargesInPeriod) ->
                ChargeChartData(
                    label = key.first,
                    count = chargesInPeriod.size,
                    totalEnergy = chargesInPeriod.sumOf { it.third.chargeEnergyAdded ?: 0.0 },
                    sortKey = key.second
                )
            }
            .sortedBy { it.sortKey }
    }

    private fun calculateSummary(charges: List<ChargeData>): ChargesSummary {
        if (charges.isEmpty()) return ChargesSummary()

        val totalEnergy = charges.sumOf { it.chargeEnergyAdded ?: 0.0 }
        val totalCost = charges.sumOf { it.cost ?: 0.0 }
        val count = charges.size

        return ChargesSummary(
            totalCharges = count,
            totalEnergyAdded = totalEnergy,
            totalCost = totalCost,
            avgEnergyPerCharge = if (count > 0) totalEnergy / count else 0.0,
            avgCostPerCharge = if (count > 0) totalCost / count else 0.0
        )
    }
}
