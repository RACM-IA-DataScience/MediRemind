package com.mediremind.app.data.local.dao

import androidx.room.*
import com.mediremind.app.data.local.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

data class ReminderWithMedication(
    @Embedded val reminder: ReminderEntity,
    val medicationName: String,
    val activeCompound: String,
    val presentation: String,
    val concentration: String,
    val currentStock: Int,
    val colorHex: String
)

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE medicationId = :medicationId")
    fun getRemindersForMedication(medicationId: Long): Flow<List<ReminderEntity>>

    @Query("""
        SELECT r.*, 
               m.name AS medicationName, 
               m.activeCompound AS activeCompound, 
               m.presentation AS presentation, 
               m.concentration AS concentration, 
               m.currentStock AS currentStock, 
               m.colorHex AS colorHex
        FROM reminders r
        INNER JOIN medications m ON r.medicationId = m.id
        WHERE r.isEnabled = 1
        ORDER BY r.hour ASC, r.minute ASC
    """)
    fun getActiveRemindersWithMedication(): Flow<List<ReminderWithMedication>>

    @Query("""
        SELECT r.*, 
               m.name AS medicationName, 
               m.activeCompound AS activeCompound, 
               m.presentation AS presentation, 
               m.concentration AS concentration, 
               m.currentStock AS currentStock, 
               m.colorHex AS colorHex
        FROM reminders r
        INNER JOIN medications m ON r.medicationId = m.id
        WHERE r.id = :reminderId LIMIT 1
    """)
    suspend fun getReminderWithMedicationById(reminderId: Long): ReminderWithMedication?

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET lastActionTimestamp = :timestamp WHERE id = :reminderId")
    suspend fun updateLastAction(reminderId: Long, timestamp: Long)
}
