package com.mediremind.app.data.local.dao

import androidx.room.*
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseHistoryDao {
    @Query("SELECT * FROM dose_history WHERE userId = :userId ORDER BY actionTimeMillis DESC")
    fun getAllHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>>

    @Query("SELECT * FROM dose_history WHERE userId = :userId AND medicationId = :medicationId ORDER BY actionTimeMillis DESC")
    fun getHistoryForMedication(userId: String, medicationId: Long): Flow<List<DoseHistoryEntity>>

    @Query("SELECT * FROM dose_history WHERE userId = :userId AND actionTimeMillis >= :startOfDayMillis ORDER BY actionTimeMillis DESC")
    fun getTodayHistoryForUser(userId: String, startOfDayMillis: Long): Flow<List<DoseHistoryEntity>>

    @Query("SELECT COUNT(*) FROM dose_history WHERE userId = :userId AND status = 'TAKEN' AND actionTimeMillis >= :sinceMillis")
    suspend fun getTakenCountSince(userId: String, sinceMillis: Long): Int

    @Query("SELECT COUNT(*) FROM dose_history WHERE userId = :userId AND status = 'SKIPPED' AND actionTimeMillis >= :sinceMillis")
    suspend fun getSkippedCountSince(userId: String, sinceMillis: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DoseHistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: DoseHistoryEntity)

    @Query("DELETE FROM dose_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM dose_history WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String)
}
