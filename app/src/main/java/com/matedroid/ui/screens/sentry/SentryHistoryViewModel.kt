package com.matedroid.ui.screens.sentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.local.SentryStateDataStore
import com.matedroid.data.local.dao.SentryAlertLogDao
import com.matedroid.data.local.entity.SentryAlertLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class DayGroup(
    val dateMillis: Long,
    val alerts: List<SentryAlertLog>
)

data class SentryHistoryUiState(
    val isLoading: Boolean = true,
    val isSessionActive: Boolean = false,
    val sessionStartedAt: Long = 0L,
    val currentSessionAlerts: List<SentryAlertLog> = emptyList(),
    val pastAlertsByDay: List<DayGroup> = emptyList()
)

@HiltViewModel
class SentryHistoryViewModel @Inject constructor(
    private val sentryAlertLogDao: SentryAlertLogDao,
    private val sentryStateDataStore: SentryStateDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SentryHistoryUiState())
    val uiState: StateFlow<SentryHistoryUiState> = _uiState.asStateFlow()

    private var carId: Int? = null

    fun setCarId(id: Int) {
        if (carId != id) {
            carId = id
            loadHistory(id)
        }
    }

    private fun loadHistory(carId: Int) {
        viewModelScope.launch {
            val state = sentryStateDataStore.getState(carId)
            _uiState.update {
                it.copy(
                    isSessionActive = state.sentryActive,
                    sessionStartedAt = state.sessionStartedAt
                )
            }

            sentryAlertLogDao.observeAll(carId).collect { allAlerts ->
                val sessionStartedAt = _uiState.value.sessionStartedAt
                val currentSession = if (sessionStartedAt > 0L) {
                    allAlerts.filter { it.sessionStartedAt == sessionStartedAt }
                } else {
                    emptyList()
                }
                val pastAlerts = allAlerts.filter {
                    sessionStartedAt == 0L || it.sessionStartedAt != sessionStartedAt
                }

                val grouped = pastAlerts
                    .groupBy { alert ->
                        Instant.ofEpochMilli(alert.detectedAt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }
                    .map { (dayMillis, alerts) -> DayGroup(dayMillis, alerts) }
                    .sortedByDescending { it.dateMillis }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentSessionAlerts = currentSession,
                        pastAlertsByDay = grouped
                    )
                }
            }
        }
    }
}
