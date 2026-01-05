package com.matedroid.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.repository.StatsRepository
import com.matedroid.data.sync.SyncLogCollector
import com.matedroid.domain.model.CarStats
import com.matedroid.domain.model.SyncPhase
import com.matedroid.domain.model.SyncProgress
import com.matedroid.domain.model.YearFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val carStats: CarStats? = null,
    val availableYears: List<Int> = emptyList(),
    val selectedYearFilter: YearFilter = YearFilter.AllTime,
    val deepSyncProgress: Float = 0f,
    val syncProgress: SyncProgress? = null,
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val syncLogCollector: SyncLogCollector
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    /** Sync logs for debug viewing */
    val syncLogs: StateFlow<List<String>> = syncLogCollector.logs

    private var carId: Int? = null

    fun setCarId(id: Int) {
        carId = id
        loadStats()
    }

    fun setYearFilter(yearFilter: YearFilter) {
        _uiState.update { it.copy(selectedYearFilter = yearFilter) }
        loadStats()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadStatsInternal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadStatsInternal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadStatsInternal() {
        val id = carId ?: return

        try {
            // Check if we have any data
            val hasData = statsRepository.hasData(id)
            if (!hasData) {
                _uiState.update {
                    it.copy(
                        error = "No data available yet. Sync in progress..."
                    )
                }
                return
            }

            // Load available years for filter
            val years = statsRepository.getAvailableYears(id)

            // Load stats with current filter
            val yearFilter = _uiState.value.selectedYearFilter
            val stats = statsRepository.getStats(id, yearFilter)

            // Get deep sync progress
            val deepProgress = statsRepository.getDeepSyncProgress(id)

            _uiState.update {
                it.copy(
                    carStats = stats,
                    availableYears = years,
                    deepSyncProgress = deepProgress,
                    syncProgress = stats.syncProgress,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: "Failed to load stats"
                )
            }
        }
    }
}
