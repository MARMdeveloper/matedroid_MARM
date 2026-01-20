package com.matedroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matedroid.data.local.entity.ChargeDetailAggregate
import com.matedroid.data.local.entity.DriveDetailAggregate

@Dao
interface AggregateDao {

    // === Drive Detail Aggregates ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDriveAggregate(aggregate: DriveDetailAggregate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDriveAggregates(aggregates: List<DriveDetailAggregate>)

    @Query("SELECT * FROM drive_detail_aggregates WHERE driveId = :driveId")
    suspend fun getDriveAggregate(driveId: Int): DriveDetailAggregate?

    @Query("DELETE FROM drive_detail_aggregates WHERE carId = :carId")
    suspend fun deleteDriveAggregatesForCar(carId: Int)

    // === Charge Detail Aggregates ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChargeAggregate(aggregate: ChargeDetailAggregate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChargeAggregates(aggregates: List<ChargeDetailAggregate>)

    @Query("SELECT * FROM charge_detail_aggregates WHERE chargeId = :chargeId")
    suspend fun getChargeAggregate(chargeId: Int): ChargeDetailAggregate?

    @Query("DELETE FROM charge_detail_aggregates WHERE carId = :carId")
    suspend fun deleteChargeAggregatesForCar(carId: Int)

    // === Deep Stats: Elevation ===

    // Highest altitude ever reached
    @Query("""
        SELECT MAX(maxElevation) FROM drive_detail_aggregates
        WHERE carId = :carId AND hasElevationData = 1
    """)
    suspend fun maxElevation(carId: Int): Int?

    @Query("""
        SELECT MAX(maxElevation) FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.hasElevationData = 1
        AND d.startDate >= :startDate AND d.startDate < :endDate
    """)
    suspend fun maxElevationInRange(carId: Int, startDate: String, endDate: String): Int?

    // Drive with highest altitude
    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        WHERE a.carId = :carId AND a.hasElevationData = 1
        ORDER BY a.maxElevation DESC LIMIT 1
    """)
    suspend fun driveWithMaxElevation(carId: Int): DriveDetailAggregate?

    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.hasElevationData = 1
        AND d.startDate >= :startDate AND d.startDate < :endDate
        ORDER BY a.maxElevation DESC LIMIT 1
    """)
    suspend fun driveWithMaxElevationInRange(carId: Int, startDate: String, endDate: String): DriveDetailAggregate?

    // Lowest altitude ever reached
    @Query("""
        SELECT MIN(minElevation) FROM drive_detail_aggregates
        WHERE carId = :carId AND hasElevationData = 1
    """)
    suspend fun minElevation(carId: Int): Int?

    // Drive with lowest altitude
    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        WHERE a.carId = :carId AND a.hasElevationData = 1
        ORDER BY a.minElevation ASC LIMIT 1
    """)
    suspend fun driveWithMinElevation(carId: Int): DriveDetailAggregate?

    // Drive with most accumulated elevation gain (total meters climbed)
    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        WHERE a.carId = :carId
        AND a.elevationGain IS NOT NULL
        ORDER BY a.elevationGain DESC LIMIT 1
    """)
    suspend fun driveWithMostElevationGain(carId: Int): DriveDetailAggregate?

    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId
        AND a.elevationGain IS NOT NULL
        AND d.startDate >= :startDate AND d.startDate < :endDate
        ORDER BY a.elevationGain DESC LIMIT 1
    """)
    suspend fun driveWithMostElevationGainInRange(carId: Int, startDate: String, endDate: String): DriveDetailAggregate?

    // === Deep Stats: Temperature (Driving) ===

    // Hottest outside temperature while driving
    @Query("""
        SELECT MAX(maxOutsideTemp) FROM drive_detail_aggregates
        WHERE carId = :carId
    """)
    suspend fun maxOutsideTempDriving(carId: Int): Double?

    @Query("""
        SELECT MAX(maxOutsideTemp) FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId
        AND d.startDate >= :startDate AND d.startDate < :endDate
    """)
    suspend fun maxOutsideTempDrivingInRange(carId: Int, startDate: String, endDate: String): Double?

    // Drive with hottest temperature
    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        WHERE a.carId = :carId AND a.maxOutsideTemp IS NOT NULL
        ORDER BY a.maxOutsideTemp DESC LIMIT 1
    """)
    suspend fun hottestDrive(carId: Int): DriveDetailAggregate?

    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.maxOutsideTemp IS NOT NULL
        AND d.startDate >= :startDate AND d.startDate < :endDate
        ORDER BY a.maxOutsideTemp DESC LIMIT 1
    """)
    suspend fun hottestDriveInRange(carId: Int, startDate: String, endDate: String): DriveDetailAggregate?

    // Coldest outside temperature while driving
    @Query("""
        SELECT MIN(minOutsideTemp) FROM drive_detail_aggregates
        WHERE carId = :carId
    """)
    suspend fun minOutsideTempDriving(carId: Int): Double?

    // Drive with coldest temperature
    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        WHERE a.carId = :carId AND a.minOutsideTemp IS NOT NULL
        ORDER BY a.minOutsideTemp ASC LIMIT 1
    """)
    suspend fun coldestDrive(carId: Int): DriveDetailAggregate?

    @Query("""
        SELECT a.* FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.minOutsideTemp IS NOT NULL
        AND d.startDate >= :startDate AND d.startDate < :endDate
        ORDER BY a.minOutsideTemp ASC LIMIT 1
    """)
    suspend fun coldestDriveInRange(carId: Int, startDate: String, endDate: String): DriveDetailAggregate?

    // Hottest cabin temperature
    @Query("SELECT MAX(maxInsideTemp) FROM drive_detail_aggregates WHERE carId = :carId")
    suspend fun maxInsideTemp(carId: Int): Double?

    // Coldest cabin temperature
    @Query("SELECT MIN(minInsideTemp) FROM drive_detail_aggregates WHERE carId = :carId")
    suspend fun minInsideTemp(carId: Int): Double?

    // === Deep Stats: Temperature (Charging) ===

    // Hottest outside temperature while charging
    @Query("SELECT MAX(maxOutsideTemp) FROM charge_detail_aggregates WHERE carId = :carId")
    suspend fun maxOutsideTempCharging(carId: Int): Double?

    @Query("""
        SELECT MAX(maxOutsideTemp) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId
        AND c.startDate >= :startDate AND c.startDate < :endDate
    """)
    suspend fun maxOutsideTempChargingInRange(carId: Int, startDate: String, endDate: String): Double?

    // Charge with hottest temperature
    @Query("""
        SELECT a.* FROM charge_detail_aggregates a
        WHERE a.carId = :carId AND a.maxOutsideTemp IS NOT NULL
        ORDER BY a.maxOutsideTemp DESC LIMIT 1
    """)
    suspend fun hottestCharge(carId: Int): ChargeDetailAggregate?

    @Query("""
        SELECT a.* FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.maxOutsideTemp IS NOT NULL
        AND c.startDate >= :startDate AND c.startDate < :endDate
        ORDER BY a.maxOutsideTemp DESC LIMIT 1
    """)
    suspend fun hottestChargeInRange(carId: Int, startDate: String, endDate: String): ChargeDetailAggregate?

    // Coldest outside temperature while charging
    @Query("SELECT MIN(minOutsideTemp) FROM charge_detail_aggregates WHERE carId = :carId")
    suspend fun minOutsideTempCharging(carId: Int): Double?

    // Charge with coldest temperature
    @Query("""
        SELECT a.* FROM charge_detail_aggregates a
        WHERE a.carId = :carId AND a.minOutsideTemp IS NOT NULL
        ORDER BY a.minOutsideTemp ASC LIMIT 1
    """)
    suspend fun coldestCharge(carId: Int): ChargeDetailAggregate?

    @Query("""
        SELECT a.* FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.minOutsideTemp IS NOT NULL
        AND c.startDate >= :startDate AND c.startDate < :endDate
        ORDER BY a.minOutsideTemp ASC LIMIT 1
    """)
    suspend fun coldestChargeInRange(carId: Int, startDate: String, endDate: String): ChargeDetailAggregate?

    // === Deep Stats: Charging Power ===

    // Max charge power ever achieved
    @Query("SELECT MAX(maxChargerPower) FROM charge_detail_aggregates WHERE carId = :carId")
    suspend fun maxChargerPower(carId: Int): Int?

    @Query("""
        SELECT MAX(maxChargerPower) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId
        AND c.startDate >= :startDate AND c.startDate < :endDate
    """)
    suspend fun maxChargerPowerInRange(carId: Int, startDate: String, endDate: String): Int?

    // Charge with max power
    @Query("""
        SELECT a.* FROM charge_detail_aggregates a
        WHERE a.carId = :carId AND a.maxChargerPower IS NOT NULL
        ORDER BY a.maxChargerPower DESC LIMIT 1
    """)
    suspend fun chargeWithMaxPower(carId: Int): ChargeDetailAggregate?

    @Query("""
        SELECT a.* FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.maxChargerPower IS NOT NULL
        AND c.startDate >= :startDate AND c.startDate < :endDate
        ORDER BY a.maxChargerPower DESC LIMIT 1
    """)
    suspend fun chargeWithMaxPowerInRange(carId: Int, startDate: String, endDate: String): ChargeDetailAggregate?

    // === Deep Stats: AC/DC Ratio ===

    // Count of AC charges
    @Query("SELECT COUNT(*) FROM charge_detail_aggregates WHERE carId = :carId AND isFastCharger = 0")
    suspend fun countAcCharges(carId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.isFastCharger = 0
        AND c.startDate >= :startDate AND c.startDate < :endDate
    """)
    suspend fun countAcChargesInRange(carId: Int, startDate: String, endDate: String): Int

    // Count of DC charges
    @Query("SELECT COUNT(*) FROM charge_detail_aggregates WHERE carId = :carId AND isFastCharger = 1")
    suspend fun countDcCharges(carId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.isFastCharger = 1
        AND c.startDate >= :startDate AND c.startDate < :endDate
    """)
    suspend fun countDcChargesInRange(carId: Int, startDate: String, endDate: String): Int

    // Get set of DC charge IDs (for UI badges)
    @Query("SELECT chargeId FROM charge_detail_aggregates WHERE carId = :carId AND isFastCharger = 1")
    suspend fun getDcChargeIds(carId: Int): List<Int>

    // Sum of energy added for AC charges (join with summary to get energyAdded)
    @Query("""
        SELECT COALESCE(SUM(c.energyAdded), 0.0) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.isFastCharger = 0
    """)
    suspend fun sumAcChargeEnergy(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(c.energyAdded), 0.0) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.isFastCharger = 0
        AND c.startDate >= :startDate AND c.startDate < :endDate
    """)
    suspend fun sumAcChargeEnergyInRange(carId: Int, startDate: String, endDate: String): Double

    // Sum of energy added for DC charges
    @Query("""
        SELECT COALESCE(SUM(c.energyAdded), 0.0) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.isFastCharger = 1
    """)
    suspend fun sumDcChargeEnergy(carId: Int): Double

    @Query("""
        SELECT COALESCE(SUM(c.energyAdded), 0.0) FROM charge_detail_aggregates a
        JOIN charges_summary c ON a.chargeId = c.chargeId
        WHERE a.carId = :carId AND a.isFastCharger = 1
        AND c.startDate >= :startDate AND c.startDate < :endDate
    """)
    suspend fun sumDcChargeEnergyInRange(carId: Int, startDate: String, endDate: String): Double

    // Get all processed charge IDs (for checking if we have aggregate data)
    @Query("SELECT chargeId FROM charge_detail_aggregates WHERE carId = :carId")
    suspend fun getAllProcessedChargeIds(carId: Int): List<Int>

    // Total processed aggregates count (for progress)
    @Query("SELECT COUNT(*) FROM drive_detail_aggregates WHERE carId = :carId")
    suspend fun countDriveAggregates(carId: Int): Int

    @Query("SELECT COUNT(*) FROM charge_detail_aggregates WHERE carId = :carId")
    suspend fun countChargeAggregates(carId: Int): Int

    // Flow-based counts for real-time progress updates (Room emits on table changes)
    @Query("SELECT COUNT(*) FROM drive_detail_aggregates WHERE carId = :carId")
    fun observeDriveAggregateCount(carId: Int): kotlinx.coroutines.flow.Flow<Int>

    @Query("SELECT COUNT(*) FROM charge_detail_aggregates WHERE carId = :carId")
    fun observeChargeAggregateCount(carId: Int): kotlinx.coroutines.flow.Flow<Int>

    // === Deep Stats: Countries Visited ===

    // Count unique countries visited
    @Query("""
        SELECT COUNT(DISTINCT startCountryCode) FROM drive_detail_aggregates
        WHERE carId = :carId AND startCountryCode IS NOT NULL
    """)
    suspend fun countUniqueCountries(carId: Int): Int

    @Query("""
        SELECT COUNT(DISTINCT a.startCountryCode) FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.startCountryCode IS NOT NULL
        AND d.startDate >= :startDate AND d.startDate < :endDate
    """)
    suspend fun countUniqueCountriesInRange(carId: Int, startDate: String, endDate: String): Int

    // Get countries visited with aggregated data
    @Query("""
        SELECT a.startCountryCode as countryCode, a.startCountryName as countryName,
               MIN(d.startDate) as firstVisitDate, MAX(d.startDate) as lastVisitDate,
               COUNT(*) as driveCount
        FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.startCountryCode IS NOT NULL
        GROUP BY a.startCountryCode
        ORDER BY firstVisitDate ASC
    """)
    suspend fun getCountriesVisited(carId: Int): List<CountryVisitResult>

    @Query("""
        SELECT a.startCountryCode as countryCode, a.startCountryName as countryName,
               MIN(d.startDate) as firstVisitDate, MAX(d.startDate) as lastVisitDate,
               COUNT(*) as driveCount
        FROM drive_detail_aggregates a
        JOIN drives_summary d ON a.driveId = d.driveId
        WHERE a.carId = :carId AND a.startCountryCode IS NOT NULL
        AND d.startDate >= :startDate AND d.startDate < :endDate
        GROUP BY a.startCountryCode
        ORDER BY firstVisitDate ASC
    """)
    suspend fun getCountriesVisitedInRange(carId: Int, startDate: String, endDate: String): List<CountryVisitResult>
}

/**
 * Result of a country visit aggregation query.
 */
data class CountryVisitResult(
    val countryCode: String,
    val countryName: String,
    val firstVisitDate: String,
    val lastVisitDate: String,
    val driveCount: Int
)
