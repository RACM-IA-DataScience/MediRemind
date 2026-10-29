package com.mediremind.app.data.local.dao

import androidx.room.*
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseHistoryDao {
    @Query("SELECT * FROM dose_history ORDER BY actionTimeMillis DESC")
    fun getAllHistory(): Flow<List<DoseHistoryEntity>>

    @Query("SELECT * FROM dose_history WHERE medicationId = :medicationId ORDER BY actionTimeMillis DESC")
    fun getHistoryForMedication(medicationId: Long): Flow<List<DoseHistoryEntity>>

    @Query("SELECT * FROM dose_history WHERE actionTimeMillis >= :startOfDayMillis ORDER BY actionTimeMillis DESC")
    fun getTodayHistory(startOfDayMillis: Long): Flow<List<DoseHistoryEntity>>

    @Query("SELECT COUNT(*) FROM dose_history WHERE status = 'TAKEN' AND actionTimeMillis >= :sinceMillis")
    suspend fun getTakenCountSince(sinceMillis: Long): Int

    @Query("SELECT COUNT(*) FROM dose_history WHERE status = 'SKIPPED' AND actionTimeMillis >= :sinceMillis")
    suspend fun getSkippedCountSince(sinceMillis: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DoseHistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: DoseHistoryEntity)

    @Query("DELETE FROM dose_history")
    suspend fun clearHistory()
}
