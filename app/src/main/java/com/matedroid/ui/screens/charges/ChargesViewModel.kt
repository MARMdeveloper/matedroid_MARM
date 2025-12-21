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
import javax.inject.Inject

data class MonthlyChargeData(
    val yearMonth: YearMonth,
    val count: Int,
    val totalEnergy: Double
)

data class ChargesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val charges: List<ChargeData> = emptyList(),
    val monthlyData: List<MonthlyChargeData> = emptyList(),
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
                    val monthlyData = calculateMonthlyData(charges)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            charges = charges,
                            monthlyData = monthlyData,
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

    private fun calculateMonthlyData(charges: List<ChargeData>): List<MonthlyChargeData> {
        if (charges.isEmpty()) return emptyList()

        val formatter = DateTimeFormatter.ISO_DATE_TIME

        return charges
            .mapNotNull { charge ->
                charge.startDate?.let { dateStr ->
                    try {
                        val date = LocalDate.parse(dateStr, formatter)
                        YearMonth.of(date.year, date.month) to charge
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .groupBy { it.first }
            .map { (yearMonth, chargesInMonth) ->
                MonthlyChargeData(
                    yearMonth = yearMonth,
                    count = chargesInMonth.size,
                    totalEnergy = chargesInMonth.sumOf { it.second.chargeEnergyAdded ?: 0.0 }
                )
            }
            .sortedBy { it.yearMonth }
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
