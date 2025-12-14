package com.matedroid.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.api.models.CarData
import com.matedroid.data.api.models.CarStatus
import com.matedroid.data.repository.ApiResult
import com.matedroid.data.repository.TeslamateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val cars: List<CarData> = emptyList(),
    val selectedCarId: Int? = null,
    val carStatus: CarStatus? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    companion object {
        private const val AUTO_REFRESH_INTERVAL_MS = 5000L
    }

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.getCars()) {
                is ApiResult.Success -> {
                    val cars = result.data
                    val selectedCarId = cars.firstOrNull()?.carId
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cars = cars,
                        selectedCarId = selectedCarId
                    )
                    selectedCarId?.let { loadCarStatus(it) }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun selectCar(carId: Int) {
        _uiState.value = _uiState.value.copy(selectedCarId = carId)
        loadCarStatus(carId)
    }

    fun refresh() {
        val carId = _uiState.value.selectedCarId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            loadCarStatus(carId)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private fun loadCarStatus(carId: Int) {
        viewModelScope.launch {
            when (val result = repository.getCarStatus(carId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        carStatus = result.data,
                        error = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
            }
        }
        startAutoRefresh(carId)
    }

    private fun startAutoRefresh(carId: Int) {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                when (val result = repository.getCarStatus(carId)) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            carStatus = result.data
                        )
                    }
                    is ApiResult.Error -> {
                        // Silently ignore errors during auto-refresh
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
