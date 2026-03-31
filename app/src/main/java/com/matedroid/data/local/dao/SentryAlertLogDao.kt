package com.matedroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.matedroid.data.local.entity.SentryAlertLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SentryAlertLogDao {

    @Insert
    suspend fun insert(log: SentryAlertLog)

    /** All alerts for a car, newest first. */
    @Query("SELECT * FROM sentry_alert_log WHERE carId = :carId ORDER BY detectedAt DESC")
    fun observeAll(carId: Int): Flow<List<SentryAlertLog>>

    /** Delete all alerts for a car. */
    @Query("DELETE FROM sentry_alert_log WHERE carId = :carId")
    suspend fun deleteAllForCar(carId: Int)
}
