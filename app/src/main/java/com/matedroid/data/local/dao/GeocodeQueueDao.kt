package com.matedroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matedroid.data.local.entity.GeocodeQueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GeocodeQueueDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(item: GeocodeQueueItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueueAll(items: List<GeocodeQueueItem>)

    @Query("""
        SELECT * FROM geocode_queue
        WHERE attempts < 3
        ORDER BY addedAt ASC
        LIMIT :limit
    """)
    suspend fun getNextBatch(limit: Int = 10): List<GeocodeQueueItem>

    @Query("DELETE FROM geocode_queue WHERE gridLat = :gridLat AND gridLon = :gridLon")
    suspend fun remove(gridLat: Int, gridLon: Int)

    @Query("SELECT COUNT(*) FROM geocode_queue WHERE attempts < 3")
    suspend fun countPending(): Int

    // Flow-based query for real-time UI updates (Room emits on table changes)
    @Query("SELECT COUNT(*) FROM geocode_queue WHERE attempts < 3")
    fun observePendingCount(): Flow<Int>

    @Query("""
        UPDATE geocode_queue
        SET attempts = attempts + 1, lastAttemptAt = :timestamp
        WHERE gridLat = :gridLat AND gridLon = :gridLon
    """)
    suspend fun markAttempt(gridLat: Int, gridLon: Int, timestamp: Long)

    // Clear queue for a specific car (for resync)
    @Query("DELETE FROM geocode_queue WHERE carId = :carId")
    suspend fun clearForCar(carId: Int)

    // Clear all failed items (attempts >= 3)
    @Query("DELETE FROM geocode_queue WHERE attempts >= 3")
    suspend fun clearFailed()
}
