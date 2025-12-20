package com.matedroid.data.repository

import com.matedroid.data.api.TeslamateApi
import com.matedroid.data.api.models.BatteryHealth
import com.matedroid.data.api.models.CarData
import com.matedroid.data.api.models.CarStatus
import com.matedroid.data.api.models.ChargeData
import com.matedroid.data.api.models.ChargeDetail
import com.matedroid.data.api.models.DriveData
import com.matedroid.data.api.models.DriveDetail
import com.matedroid.data.api.models.UpdateData
import com.matedroid.data.local.SettingsDataStore
import com.matedroid.di.TeslamateApiFactory
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

@Singleton
class TeslamateRepository @Inject constructor(
    private val apiFactory: TeslamateApiFactory,
    private val settingsDataStore: SettingsDataStore
) {
    private suspend fun getApi(): TeslamateApi? {
        val settings = settingsDataStore.settings.first()
        if (settings.serverUrl.isBlank()) return null
        return apiFactory.create(settings.serverUrl)
    }

    suspend fun testConnection(serverUrl: String, acceptInvalidCerts: Boolean = false): ApiResult<Unit> {
        return try {
            val api = apiFactory.create(serverUrl, acceptInvalidCerts)
            val response = api.ping()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Server returned ${response.code()}", response.code())
            }
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            ApiResult.Error("SSL certificate error. Enable 'Accept invalid certificates' for self-signed certs.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun getCars(): ApiResult<List<CarData>> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getCars()
            if (response.isSuccessful) {
                val cars = response.body()?.data?.cars ?: emptyList()
                ApiResult.Success(cars)
            } else {
                ApiResult.Error("Failed to fetch cars: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch cars")
        }
    }

    suspend fun getCarStatus(carId: Int): ApiResult<CarStatus> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getCarStatus(carId)
            if (response.isSuccessful) {
                val status = response.body()?.data?.status
                if (status != null) {
                    ApiResult.Success(status)
                } else {
                    ApiResult.Error("No status data returned")
                }
            } else {
                ApiResult.Error("Failed to fetch status: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch status")
        }
    }

    suspend fun getCharges(
        carId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): ApiResult<List<ChargeData>> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getCharges(carId, startDate, endDate, page = 1, show = 50000)
            if (response.isSuccessful) {
                val charges = response.body()?.data?.charges ?: emptyList()
                ApiResult.Success(charges)
            } else {
                ApiResult.Error("Failed to fetch charges: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch charges")
        }
    }

    suspend fun getChargeDetail(carId: Int, chargeId: Int): ApiResult<ChargeDetail> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getChargeDetail(carId, chargeId)
            if (response.isSuccessful) {
                val detail = response.body()?.data
                if (detail != null) {
                    ApiResult.Success(detail)
                } else {
                    ApiResult.Error("No charge detail returned")
                }
            } else {
                ApiResult.Error("Failed to fetch charge detail: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch charge detail")
        }
    }

    suspend fun getDrives(
        carId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): ApiResult<List<DriveData>> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getDrives(carId, startDate, endDate, page = 1, show = 50000)
            if (response.isSuccessful) {
                val drives = response.body()?.data?.drives ?: emptyList()
                ApiResult.Success(drives)
            } else {
                ApiResult.Error("Failed to fetch drives: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch drives")
        }
    }

    suspend fun getDriveDetail(carId: Int, driveId: Int): ApiResult<DriveDetail> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getDriveDetail(carId, driveId)
            if (response.isSuccessful) {
                val detail = response.body()?.data
                if (detail != null) {
                    ApiResult.Success(detail)
                } else {
                    ApiResult.Error("No drive detail returned")
                }
            } else {
                ApiResult.Error("Failed to fetch drive detail: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch drive detail")
        }
    }

    suspend fun getBatteryHealth(carId: Int): ApiResult<BatteryHealth> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getBatteryHealth(carId)
            if (response.isSuccessful) {
                val health = response.body()?.data?.batteryHealth
                if (health != null) {
                    ApiResult.Success(health)
                } else {
                    ApiResult.Error("No battery health data returned")
                }
            } else {
                ApiResult.Error("Failed to fetch battery health: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch battery health")
        }
    }

    suspend fun getUpdates(carId: Int): ApiResult<List<UpdateData>> {
        return try {
            val api = getApi() ?: return ApiResult.Error("Server not configured")
            val response = api.getUpdates(carId)
            if (response.isSuccessful) {
                val updates = response.body()?.data ?: emptyList()
                ApiResult.Success(updates)
            } else {
                ApiResult.Error("Failed to fetch updates: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to fetch updates")
        }
    }
}
