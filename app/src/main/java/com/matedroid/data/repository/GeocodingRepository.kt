package com.matedroid.data.repository

import com.matedroid.data.api.NominatimApi
import com.matedroid.data.api.NominatimAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepository @Inject constructor(
    private val nominatimApi: NominatimApi
) {
    // Simple in-memory cache to avoid repeated API calls for the same location
    private val cache = mutableMapOf<String, String>()

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        // Round coordinates to 4 decimal places for caching (~11m accuracy)
        val cacheKey = "%.4f,%.4f".format(latitude, longitude)

        // Return cached result if available
        cache[cacheKey]?.let { return it }

        return try {
            val response = nominatimApi.reverseGeocode(latitude, longitude)
            if (response.isSuccessful) {
                val result = response.body()
                val address = formatAddress(result?.address)
                    ?: result?.displayName?.split(",")?.take(3)?.joinToString(", ")

                address?.also { cache[cacheKey] = it }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatAddress(address: NominatimAddress?): String? {
        if (address == null) return null

        val parts = mutableListOf<String>()

        // Street with house number
        val street = listOfNotNull(address.road, address.house_number).joinToString(" ")
        if (street.isNotBlank()) parts.add(street)

        // City/town/village
        val city = address.city ?: address.town ?: address.village ?: address.municipality
        if (city != null) parts.add(city)

        return if (parts.isNotEmpty()) parts.joinToString(", ") else null
    }
}
