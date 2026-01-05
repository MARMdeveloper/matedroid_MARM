package com.matedroid.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.matedroid.data.local.entity.DriveSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveSummaryDao {

    // === CRUD Operations ===

    @Upsert
    suspend fun upsertAll(drives: List<DriveSummary>)

    @Upsert
    suspend fun upsert(drive: DriveSummary)

    @Query("SELECT * FROM drives_summary WHERE driveId = :driveId")
    suspend fun get(driveId: Int): DriveSummary?

    @Query("SELECT * FROM drives_summary WHERE carId = :carId ORDER BY startDate DESC")
    fun observeAll(carId: Int): Flow<List<DriveSummary>>

    @Query("SELECT MAX(driveId) FROM drives_summary WHERE carId = :carId")
    suspend fun getMaxDriveId(carId: Int): Int?

    @Query("DELETE FROM drives_summary WHERE carId = :carId")
    suspend fun deleteAllForCar(carId: Int)

    // === Quick Stats Queries ===

    // Total count
    @Query("SELECT COUNT(*) FROM drives_summary WHERE carId = :carId")
    suspend fun count(carId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun countInRange(carId: Int, startDate: String, endDate: String): Int

    // Total distance
    @Query("SELECT COALESCE(SUM(distance), 0) FROM drives_summary WHERE carId = :carId")
    suspend fun sumDistance(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(distance), 0) FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun sumDistanceInRange(carId: Int, startDate: String, endDate: String): Double

    // Total energy consumed
    @Query("SELECT COALESCE(SUM(energyConsumed), 0) FROM drives_summary WHERE carId = :carId")
    suspend fun sumEnergyConsumed(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(energyConsumed), 0) FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun sumEnergyConsumedInRange(carId: Int, startDate: String, endDate: String): Double

    // Average efficiency
    @Query("""
        SELECT COALESCE(SUM(energyConsumed) * 1000 / NULLIF(SUM(distance), 0), 0)
        FROM drives_summary WHERE carId = :carId
    """)
    suspend fun avgEfficiency(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(energyConsumed) * 1000 / NULLIF(SUM(distance), 0), 0)
        FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun avgEfficiencyInRange(carId: Int, startDate: String, endDate: String): Double

    // Max speed ever
    @Query("SELECT MAX(speedMax) FROM drives_summary WHERE carId = :carId")
    suspend fun maxSpeed(carId: Int): Int?

    @Query("""
        SELECT MAX(speedMax) FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
    """)
    suspend fun maxSpeedInRange(carId: Int, startDate: String, endDate: String): Int?

    // Longest drive (by distance)
    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId
        ORDER BY distance DESC LIMIT 1
    """)
    suspend fun longestDrive(carId: Int): DriveSummary?

    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY distance DESC LIMIT 1
    """)
    suspend fun longestDriveInRange(carId: Int, startDate: String, endDate: String): DriveSummary?

    // Drive with max speed
    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId
        ORDER BY speedMax DESC LIMIT 1
    """)
    suspend fun fastestDrive(carId: Int): DriveSummary?

    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY speedMax DESC LIMIT 1
    """)
    suspend fun fastestDriveInRange(carId: Int, startDate: String, endDate: String): DriveSummary?

    // Best efficiency (lowest Wh/km, excluding very short drives)
    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId AND efficiency > 0 AND distance > 5
        ORDER BY efficiency ASC LIMIT 1
    """)
    suspend fun mostEfficientDrive(carId: Int): DriveSummary?

    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId AND efficiency > 0 AND distance > 5
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY efficiency ASC LIMIT 1
    """)
    suspend fun mostEfficientDriveInRange(carId: Int, startDate: String, endDate: String): DriveSummary?

    // Worst efficiency (highest Wh/km)
    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId AND efficiency > 0 AND distance > 5
        ORDER BY efficiency DESC LIMIT 1
    """)
    suspend fun leastEfficientDrive(carId: Int): DriveSummary?

    @Query("""
        SELECT * FROM drives_summary
        WHERE carId = :carId AND efficiency > 0 AND distance > 5
        AND startDate >= :startDate AND startDate < :endDate
        ORDER BY efficiency DESC LIMIT 1
    """)
    suspend fun leastEfficientDriveInRange(carId: Int, startDate: String, endDate: String): DriveSummary?

    // Average drive duration
    @Query("SELECT AVG(durationMin) FROM drives_summary WHERE carId = :carId")
    suspend fun avgDuration(carId: Int): Double?

    // First drive date
    @Query("SELECT MIN(startDate) FROM drives_summary WHERE carId = :carId")
    suspend fun firstDriveDate(carId: Int): String?

    // Busiest day (most drives)
    @Query("""
        SELECT DATE(startDate) as day, COUNT(*) as count
        FROM drives_summary
        WHERE carId = :carId
        GROUP BY DATE(startDate)
        ORDER BY count DESC LIMIT 1
    """)
    suspend fun busiestDay(carId: Int): BusiestDayResult?

    @Query("""
        SELECT DATE(startDate) as day, COUNT(*) as count
        FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
        GROUP BY DATE(startDate)
        ORDER BY count DESC LIMIT 1
    """)
    suspend fun busiestDayInRange(carId: Int, startDate: String, endDate: String): BusiestDayResult?

    // Count of unique driving days
    @Query("SELECT COUNT(DISTINCT DATE(startDate)) FROM drives_summary WHERE carId = :carId")
    suspend fun countDrivingDays(carId: Int): Int

    // Most distance in a single day
    @Query("""
        SELECT DATE(startDate) as day, SUM(distance) as totalDistance
        FROM drives_summary
        WHERE carId = :carId
        GROUP BY DATE(startDate)
        ORDER BY totalDistance DESC LIMIT 1
    """)
    suspend fun mostDistanceDay(carId: Int): MostDistanceDayResult?

    @Query("""
        SELECT DATE(startDate) as day, SUM(distance) as totalDistance
        FROM drives_summary
        WHERE carId = :carId
        AND startDate >= :startDate AND startDate < :endDate
        GROUP BY DATE(startDate)
        ORDER BY totalDistance DESC LIMIT 1
    """)
    suspend fun mostDistanceDayInRange(carId: Int, startDate: String, endDate: String): MostDistanceDayResult?

    // === Queries for Detail Sync ===

    // Get drive IDs that need detail processing
    @Query("""
        SELECT d.driveId FROM drives_summary d
        LEFT JOIN drive_detail_aggregates a ON d.driveId = a.driveId
        WHERE d.carId = :carId
        AND (a.driveId IS NULL OR a.schemaVersion < :currentVersion)
        ORDER BY d.driveId
    """)
    suspend fun getUnprocessedDriveIds(carId: Int, currentVersion: Int): List<Int>

    // Count unprocessed drives
    @Query("""
        SELECT COUNT(*) FROM drives_summary d
        LEFT JOIN drive_detail_aggregates a ON d.driveId = a.driveId
        WHERE d.carId = :carId
        AND (a.driveId IS NULL OR a.schemaVersion < :currentVersion)
    """)
    suspend fun countUnprocessedDrives(carId: Int, currentVersion: Int): Int

    // === Year List for Filter ===

    @Query("""
        SELECT DISTINCT CAST(strftime('%Y', startDate) AS INTEGER) as year
        FROM drives_summary
        WHERE carId = :carId
        ORDER BY year DESC
    """)
    suspend fun getYears(carId: Int): List<Int>
}

data class BusiestDayResult(
    val day: String,
    val count: Int
)

data class MostDistanceDayResult(
    val day: String,
    val totalDistance: Double
)
