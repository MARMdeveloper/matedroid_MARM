package com.matedroid.ui.screens.drives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matedroid.data.api.models.DriveDetail
import com.matedroid.data.api.models.DrivePosition
import com.matedroid.data.api.models.Units
import com.matedroid.data.repository.ApiResult
import com.matedroid.data.repository.TeslamateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriveDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val driveDetail: DriveDetail? = null,
    val units: Units? = null,
    val stats: DriveDetailStats? = null
)

data class DriveDetailStats(
    val speedMax: Int,
    val speedAvg: Double,
    val speedMin: Int,
    val powerMax: Int,
    val powerMin: Int,
    val powerAvg: Double,
    val elevationMax: Int,
    val elevationMin: Int,
    val elevationGain: Int,
    val elevationLoss: Int,
    val batteryStart: Int,
    val batteryEnd: Int,
    val batteryUsed: Int,
    val energyUsed: Double,
    val efficiency: Double,
    val distance: Double,
    val durationMin: Int,
    val avgSpeedFromDistance: Double,
    val outsideTempAvg: Double?,
    val insideTempAvg: Double?
)

@HiltViewModel
class DriveDetailViewModel @Inject constructor(
    private val repository: TeslamateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriveDetailUiState())
    val uiState: StateFlow<DriveDetailUiState> = _uiState.asStateFlow()

    private var carId: Int? = null
    private var driveId: Int? = null

    fun loadDriveDetail(carId: Int, driveId: Int) {
        if (this.carId == carId && this.driveId == driveId && _uiState.value.driveDetail != null) {
            return // Already loaded
        }

        this.carId = carId
        this.driveId = driveId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Fetch drive detail and units in parallel
            val detailResult = repository.getDriveDetail(carId, driveId)
            val statusResult = repository.getCarStatus(carId)

            val units = when (statusResult) {
                is ApiResult.Success -> statusResult.data.units
                is ApiResult.Error -> null
            }

            when (detailResult) {
                is ApiResult.Success -> {
                    val detail = detailResult.data
                    val stats = calculateStats(detail)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            driveDetail = detail,
                            units = units,
                            stats = stats,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = detailResult.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun calculateStats(detail: DriveDetail): DriveDetailStats {
        val positions = detail.positions ?: emptyList()

        // Speed stats from positions
        val speeds = positions.mapNotNull { it.speed }
        val speedMax = speeds.maxOrNull() ?: detail.speedMax ?: 0
        val speedMin = speeds.filter { it > 0 }.minOrNull() ?: 0
        val speedAvg = if (speeds.isNotEmpty()) speeds.average() else detail.speedAvg ?: 0.0

        // Power stats from positions
        val powers = positions.mapNotNull { it.power }
        val powerMax = powers.maxOrNull() ?: detail.powerMax ?: 0
        val powerMin = powers.minOrNull() ?: detail.powerMin ?: 0
        val powerAvg = if (powers.isNotEmpty()) powers.average() else 0.0

        // Elevation stats
        val elevations = positions.mapNotNull { it.elevation }
        val elevationMax = elevations.maxOrNull() ?: 0
        val elevationMin = elevations.minOrNull() ?: 0
        val (elevationGain, elevationLoss) = calculateElevationChange(elevations)

        // Battery stats
        val batteryLevels = positions.mapNotNull { it.batteryLevel }
        val batteryStart = batteryLevels.firstOrNull() ?: detail.startBatteryLevel ?: 0
        val batteryEnd = batteryLevels.lastOrNull() ?: detail.endBatteryLevel ?: 0
        val batteryUsed = batteryStart - batteryEnd

        // Energy and efficiency
        val distance = detail.distance ?: 0.0
        val energyUsed = detail.energyConsumedNet ?: 0.0
        val efficiency = if (distance > 0) (energyUsed * 1000) / distance else 0.0

        // Duration and average speed from distance
        val durationMin = detail.durationMin ?: 0
        val avgSpeedFromDistance = if (durationMin > 0) (distance / durationMin) * 60 else 0.0

        return DriveDetailStats(
            speedMax = speedMax,
            speedAvg = speedAvg,
            speedMin = speedMin,
            powerMax = powerMax,
            powerMin = powerMin,
            powerAvg = powerAvg,
            elevationMax = elevationMax,
            elevationMin = elevationMin,
            elevationGain = elevationGain,
            elevationLoss = elevationLoss,
            batteryStart = batteryStart,
            batteryEnd = batteryEnd,
            batteryUsed = batteryUsed,
            energyUsed = energyUsed,
            efficiency = efficiency,
            distance = distance,
            durationMin = durationMin,
            avgSpeedFromDistance = avgSpeedFromDistance,
            outsideTempAvg = detail.outsideTempAvg,
            insideTempAvg = detail.insideTempAvg
        )
    }

    private fun calculateElevationChange(elevations: List<Int>): Pair<Int, Int> {
        if (elevations.size < 2) return Pair(0, 0)

        var gain = 0
        var loss = 0

        for (i in 1 until elevations.size) {
            val diff = elevations[i] - elevations[i - 1]
            if (diff > 0) gain += diff
            else loss += -diff
        }

        return Pair(gain, loss)
    }
}
