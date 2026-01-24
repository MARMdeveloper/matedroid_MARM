package com.matedroid.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.repository.StatsRepository
import com.matedroid.domain.model.CountryRecord
import com.matedroid.domain.model.YearFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sorting options for the countries list.
 */
enum class CountrySortOrder {
    FIRST_VISIT,    // Chronological by first visit date (default)
    ALPHABETICAL,   // A-Z by country name
    DRIVE_COUNT,    // Most drives first
    DISTANCE,       // Most distance first
    ENERGY,         // Most energy charged first
    CHARGES         // Most charges first
}

data class CountriesVisitedUiState(
    val isLoading: Boolean = true,
    val countries: List<CountryRecord> = emptyList(),
    val sortOrder: CountrySortOrder = CountrySortOrder.FIRST_VISIT,
    val error: String? = null
)

@HiltViewModel
class CountriesVisitedViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountriesVisitedUiState())
    val uiState: StateFlow<CountriesVisitedUiState> = _uiState.asStateFlow()

    private var originalCountries: List<CountryRecord> = emptyList()

    fun loadCountries(carId: Int, yearFilter: YearFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                originalCountries = statsRepository.getCountriesVisited(carId, yearFilter)
                val sorted = sortCountries(originalCountries, _uiState.value.sortOrder)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        countries = sorted,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load countries"
                    )
                }
            }
        }
    }

    fun setSortOrder(order: CountrySortOrder) {
        val sorted = sortCountries(originalCountries, order)
        _uiState.update {
            it.copy(
                sortOrder = order,
                countries = sorted
            )
        }
    }

    private fun sortCountries(
        countries: List<CountryRecord>,
        order: CountrySortOrder
    ): List<CountryRecord> {
        return when (order) {
            CountrySortOrder.FIRST_VISIT -> countries.sortedBy { it.firstVisitDate }
            CountrySortOrder.ALPHABETICAL -> countries.sortedBy { it.countryName }
            CountrySortOrder.DRIVE_COUNT -> countries.sortedByDescending { it.driveCount }
            CountrySortOrder.DISTANCE -> countries.sortedByDescending { it.totalDistanceKm }
            CountrySortOrder.ENERGY -> countries.sortedByDescending { it.totalChargeEnergyKwh }
            CountrySortOrder.CHARGES -> countries.sortedByDescending { it.chargeCount }
        }
    }
}
