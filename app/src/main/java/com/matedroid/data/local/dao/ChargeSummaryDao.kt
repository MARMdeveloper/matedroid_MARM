package com.matedroid.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.matedroid.data.local.entity.ChargeSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeSummaryDao {

    // === CRUD Operations ===

    @Upsert
    suspend fun upsertAll(charges: List<ChargeSummary>)

    @Upsert
    suspend fun upsert(charge: ChargeSummary)

    @Query("SELECT * FROM charges_summary WHERE chargeId = :chargeId")
    suspend fun get(chargeId: Int): ChargeSummary?

    @Query("SELECT * FROM charges_summary WHERE carId = :carId ORDER BY startDate DESC")
    fun observeAll(carId: Int): Flow<List<ChargeSummary>>

    @Query("SELECT MAX(chargeId) FROM charges_summary WHERE carId = :carId")
    suspend fun getMaxChargeId(carId: Int): Int?

    @Query("DELETE FROM charges_summary WHERE carId = :carId")
    suspend fun deleteAllForCar(carId: Int)

    // === Quick Stats Queries ===

    // Total count
    @Query("SELECT COUNT(*) FROM charges_summary WHERE carId = :carId")
    suspend fun count(carId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM charges_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun countInRange(carId: Int, startDate: String, endDate: String): Int

    // Total energy added
    @Query("SELECT COALESCE(SUM(energyAdded), 0) FROM charges_summary WHERE carId = :carId")
    suspend fun sumEnergyAdded(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(energyAdded), 0) FROM charges_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun sumEnergyAddedInRange(carId: Int, startDate: String, endDate: String): Double

    // Total cost
    @Query("SELECT COALESCE(SUM(cost), 0) FROM charges_summary WHERE carId = :carId")
    suspend fun sumCost(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(cost), 0) FROM charges_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun sumCostInRange(carId: Int, startDate: String, endDate: String): Double

    // Average cost per kWh
    @Query("""
        SELECT COALESCE(SUM(cost) / NULLIF(SUM(energyAdded), 0), 0)
        FROM charges_summary WHERE carId = :carId AND cost IS NOT NULL
    """)
    suspend fun avgCostPerKwh(carId: Int): Double

    // Biggest single charge (by energy added)
    @Query("""
        SELECT * FROM charges_summary
        WHERE carId = :carId
        ORDER BY energyAdded DESC LIMIT 1
    """)
    suspend fun biggestCharge(carId: Int): ChargeSummary?

    @Query("""
        SELECT * FROM charges_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY energyAdded DESC LIMIT 1
    """)
    suspend fun biggestChargeInRange(carId: Int, startDate: String, endDate: String): ChargeSummary?

    // Most expensive single charge
    @Query("""
        SELECT * FROM charges_summary
        WHERE carId = :carId AND cost IS NOT NULL
        ORDER BY cost DESC LIMIT 1
    """)
    suspend fun mostExpensiveCharge(carId: Int): ChargeSummary?

    @Query("""
        SELECT * FROM charges_summary
        WHERE carId = :carId AND cost IS NOT NULL
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY cost DESC LIMIT 1
    """)
    suspend fun mostExpensiveChargeInRange(carId: Int, startDate: String, endDate: String): ChargeSummary?

    // Most expensive per kWh charge
    @Query("""
        SELECT * FROM charges_summary
        WHERE carId = :carId AND cost IS NOT NULL AND energyAdded > 0
        ORDER BY (cost / energyAdded) DESC LIMIT 1
    """)
    suspend fun mostExpensivePerKwhCharge(carId: Int): ChargeSummary?

    @Query("""
        SELECT * FROM charges_summary
        WHERE carId = :carId AND cost IS NOT NULL AND energyAdded > 0
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY (cost / energyAdded) DESC LIMIT 1
    """)
    suspend fun mostExpensivePerKwhChargeInRange(carId: Int, startDate: String, endDate: String): ChargeSummary?

    // Average charge duration
    @Query("SELECT AVG(durationMin) FROM charges_summary WHERE carId = :carId")
    suspend fun avgDuration(carId: Int): Double?

    // First charge date
    @Query("SELECT MIN(startDate) FROM charges_summary WHERE carId = :carId")
    suspend fun firstChargeDate(carId: Int): String?

    // === Queries for Detail Sync ===

    // Get charge IDs that need detail processing
    @Query("""
        SELECT c.chargeId FROM charges_summary c
        LEFT JOIN charge_detail_aggregates a ON c.chargeId = a.chargeId
        WHERE c.carId = :carId
        AND (a.chargeId IS NULL OR a.schemaVersion < :currentVersion)
        ORDER BY c.chargeId
    """)
    suspend fun getUnprocessedChargeIds(carId: Int, currentVersion: Int): List<Int>

    // Count unprocessed charges
    @Query("""
        SELECT COUNT(*) FROM charges_summary c
        LEFT JOIN charge_detail_aggregates a ON c.chargeId = a.chargeId
        WHERE c.carId = :carId
        AND (a.chargeId IS NULL OR a.schemaVersion < :currentVersion)
    """)
    suspend fun countUnprocessedCharges(carId: Int, currentVersion: Int): Int

    // === Year List for Filter ===

    @Query("""
        SELECT DISTINCT CAST(strftime('%Y', startDate) AS INTEGER) as year
        FROM charges_summary
        WHERE carId = :carId
        ORDER BY year DESC
    """)
    suspend fun getYears(carId: Int): List<Int>
}
