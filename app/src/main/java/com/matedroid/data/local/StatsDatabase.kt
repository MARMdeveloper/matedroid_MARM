package com.matedroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matedroid.data.local.dao.AggregateDao
import com.matedroid.data.local.dao.ChargeSummaryDao
import com.matedroid.data.local.dao.DriveSummaryDao
import com.matedroid.data.local.dao.SyncStateDao
import com.matedroid.data.local.entity.ChargeDetailAggregate
import com.matedroid.data.local.entity.ChargeSummary
import com.matedroid.data.local.entity.DriveDetailAggregate
import com.matedroid.data.local.entity.DriveSummary
import com.matedroid.data.local.entity.SyncState

/**
 * Room database for storing stats data locally.
 *
 * Tables:
 * - sync_state: Tracks sync progress per car
 * - drives_summary: Drive list data for Quick Stats
 * - charges_summary: Charge list data for Quick Stats
 * - drive_detail_aggregates: Computed aggregates for Deep Stats
 * - charge_detail_aggregates: Computed aggregates for Deep Stats
 *
 * Storage estimate: ~10 MB for heavy user (15k drives, 8k charges)
 */
@Database(
    entities = [
        SyncState::class,
        DriveSummary::class,
        ChargeSummary::class,
        DriveDetailAggregate::class,
        ChargeDetailAggregate::class
    ],
    version = 3,
    exportSchema = true
)
abstract class StatsDatabase : RoomDatabase() {

    abstract fun syncStateDao(): SyncStateDao
    abstract fun driveSummaryDao(): DriveSummaryDao
    abstract fun chargeSummaryDao(): ChargeSummaryDao
    abstract fun aggregateDao(): AggregateDao

    companion object {
        const val DATABASE_NAME = "matedroid_stats.db"

        /** Migration from V1 to V2: Add start/end elevation for net climb calculation */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add startElevation and endElevation columns to drive_detail_aggregates
                db.execSQL("ALTER TABLE drive_detail_aggregates ADD COLUMN startElevation INTEGER")
                db.execSQL("ALTER TABLE drive_detail_aggregates ADD COLUMN endElevation INTEGER")
            }
        }

        /** Migration from V2 to V3: Fix isFastCharger using Teslamate's charger_phases logic */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Teslamate logic: DC charging has charger_phases = 0 or null
                // AC charging has charger_phases = 1, 2, or 3
                db.execSQL("""
                    UPDATE charge_detail_aggregates
                    SET isFastCharger = CASE
                        WHEN chargerPhases IS NULL OR chargerPhases = 0 THEN 1
                        ELSE 0
                    END
                """)
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
