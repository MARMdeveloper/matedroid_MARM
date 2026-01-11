package com.matedroid.ui.screens.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.matedroid.data.local.SettingsDataStore
import com.matedroid.data.model.Currency
import com.matedroid.data.repository.StatsRepository
import com.matedroid.data.sync.DataSyncWorker
import com.matedroid.data.sync.SyncLogCollector
import com.matedroid.data.sync.SyncManager
import com.matedroid.domain.model.CarStats
import com.matedroid.domain.model.SyncPhase
import com.matedroid.domain.model.SyncProgress
import com.matedroid.domain.model.YearFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val carStats: CarStats? = null,
    val availableYears: List<Int> = emptyList(),
    val selectedYearFilter: YearFilter = YearFilter.AllTime,
    val deepSyncProgress: Float = 0f,
    val syncProgress: SyncProgress? = null,
    val currencySymbol: String = "€",
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val statsRepository: StatsRepository,
    private val syncManager: SyncManager,
    private val syncLogCollector: SyncLogCollector,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    /** Sync logs for debug viewing */
    val syncLogs: StateFlow<List<String>> = syncLogCollector.logs

    /** Expose sync status for UI to observe */
    val syncStatus = syncManager.syncStatus

    private var carId: Int? = null
    private var syncObserverJob: Job? = null

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
        carId = id
        loadStats()
        startObservingSyncStatus()
    }

    /**
     * Observe sync status changes and reload stats when sync progresses.
     * This ensures the UI updates as new data is synced.
     */
    private fun startObservingSyncStatus() {
        syncObserverJob?.cancel()
        syncObserverJob = viewModelScope.launch {
            syncManager.syncStatus.collect { status ->
                val id = carId ?: return@collect
                val carProgress = status.carProgresses[id]

                // Update syncing state
                val isSyncing = carProgress != null &&
                    carProgress.phase != SyncPhase.COMPLETE &&
                    carProgress.phase != SyncPhase.ERROR &&
                    carProgress.phase != SyncPhase.IDLE

                _uiState.update { it.copy(isSyncing = isSyncing, syncProgress = carProgress) }

                // Reload stats periodically while syncing to show new data
                if (isSyncing) {
                    loadStatsInternal()
                }
            }
        }
    }

    fun setYearFilter(yearFilter: YearFilter) {
        _uiState.update { it.copy(selectedYearFilter = yearFilter) }
        loadStats()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            // Trigger sync to fetch new data from server
            triggerSync()
            loadStatsInternal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Trigger a background sync to fetch new data from the server.
     * Uses KEEP policy to avoid interrupting a running sync.
     */
    fun triggerSync() {
        // Skip if sync is already running
        if (syncManager.syncStatus.value.isAnySyncing) {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(DataSyncWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            DataSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP, // Don't interrupt running sync
            syncRequest
        )
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

            // Get deep sync progress (even if no data yet)
            val deepProgress = statsRepository.getDeepSyncProgress(id)

            if (!hasData) {
                // No data yet - show sync progress if available
                _uiState.update {
                    it.copy(
                        carStats = null,
                        deepSyncProgress = deepProgress,
                        error = null // Don't show error, show empty state with progress
                    )
                }
                return
            }

            // Load available years for filter
            val years = statsRepository.getAvailableYears(id)

            // Load stats with current filter
            val yearFilter = _uiState.value.selectedYearFilter
            val stats = statsRepository.getStats(id, yearFilter)

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
