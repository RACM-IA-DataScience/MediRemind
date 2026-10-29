package com.mediremind.app.data.repository

import android.content.Context
import com.mediremind.app.data.local.dao.DoseHistoryDao
import com.mediremind.app.data.local.dao.MedicationDao
import com.mediremind.app.data.local.dao.ReminderDao
import com.mediremind.app.data.local.dao.ReminderWithMedication
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import com.mediremind.app.data.local.entities.ReminderEntity
import com.mediremind.app.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class AdherenceStats(
    val takenCount: Int,
    val skippedCount: Int,
    val totalCount: Int,
    val percentage: Int
)

class ReminderRepository(
    private val context: Context,
    private val reminderDao: ReminderDao,
    private val medicationDao: MedicationDao,
    private val doseHistoryDao: DoseHistoryDao
) {

    fun getActiveReminders(): Flow<List<ReminderWithMedication>> =
        reminderDao.getActiveRemindersWithMedication()

    fun getRemindersForMedication(medicationId: Long): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersForMedication(medicationId)

    suspend fun saveReminder(reminder: ReminderEntity): Long {
        val id = reminderDao.insertReminder(reminder)
        val saved = reminder.copy(id = id)
        val medication = medicationDao.getMedicationById(reminder.medicationId)
        if (medication != null && reminder.isEnabled) {
            NotificationHelper.scheduleExactAlarm(context, saved, medication)
        }
        return id
    }

    suspend fun toggleReminder(reminderId: Long, isEnabled: Boolean) {
        val reminder = reminderDao.getReminderById(reminderId) ?: return
        val updated = reminder.copy(isEnabled = isEnabled)
        reminderDao.updateReminder(updated)

        val medication = medicationDao.getMedicationById(reminder.medicationId)
        if (medication != null) {
            if (isEnabled) {
                NotificationHelper.scheduleExactAlarm(context, updated, medication)
            } else {
                NotificationHelper.cancelAlarm(context, reminderId)
            }
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        NotificationHelper.cancelAlarm(context, reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    /**
     * Registra una dosis tomada a partir de una alarma programada.
     * Permite especificar una hora real de toma diferente al momento actual,
     * guardando con precisión el momento exacto en que se consumió el fármaco.
     */
    suspend fun recordDoseTaken(
        item: ReminderWithMedication,
        actualTimeMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ) {
        // Calcular la hora a la que estuvo programada la alarma el día de hoy
        val scheduledCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.reminder.hour)
            set(Calendar.MINUTE, item.reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val scheduledMillis = scheduledCal.timeInMillis

        // Descontar inventario
        medicationDao.decrementStock(item.reminder.medicationId, item.reminder.dosageAmount.toInt().coerceAtLeast(1))

        // Registrar en historial con hora programada y hora real de ingestión
        doseHistoryDao.insertHistory(
            DoseHistoryEntity(
                medicationId = item.reminder.medicationId,
                reminderId = item.reminder.id,
                medicationName = item.medicationName,
                activeCompound = item.activeCompound,
                dosageTaken = "${item.reminder.dosageAmount}",
                scheduledTimeMillis = scheduledMillis,
                actionTimeMillis = actualTimeMillis,
                status = "TAKEN",
                notes = notes
            )
        )
        reminderDao.updateLastAction(item.reminder.id, actualTimeMillis)
    }

    /**
     * Registra una toma manual o extemporánea (por ejemplo si no se tenía una alarma activa
     * o se tomó un medicamento de rescate), permitiendo indicar la hora exacta de la toma.
     */
    suspend fun recordManualDoseTaken(
        medicationId: Long,
        reminderId: Long? = null,
        medicationName: String,
        activeCompound: String,
        dosageAmount: Double = 1.0,
        scheduledTimeMillis: Long = 0L,
        actualTimeMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ) {
        medicationDao.decrementStock(medicationId, dosageAmount.toInt().coerceAtLeast(1))

        doseHistoryDao.insertHistory(
            DoseHistoryEntity(
                medicationId = medicationId,
                reminderId = reminderId,
                medicationName = medicationName,
                activeCompound = activeCompound,
                dosageTaken = "$dosageAmount",
                scheduledTimeMillis = scheduledTimeMillis,
                actionTimeMillis = actualTimeMillis,
                status = "TAKEN",
                notes = notes
            )
        )

        if (reminderId != null) {
            reminderDao.updateLastAction(reminderId, actualTimeMillis)
        }
    }

    suspend fun recordDoseSkipped(item: ReminderWithMedication, reason: String = "") {
        val scheduledCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.reminder.hour)
            set(Calendar.MINUTE, item.reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        doseHistoryDao.insertHistory(
            DoseHistoryEntity(
                medicationId = item.reminder.medicationId,
                reminderId = item.reminder.id,
                medicationName = item.medicationName,
                activeCompound = item.activeCompound,
                dosageTaken = "0",
                scheduledTimeMillis = scheduledCal.timeInMillis,
                actionTimeMillis = System.currentTimeMillis(),
                status = "SKIPPED",
                notes = reason
            )
        )
        reminderDao.updateLastAction(item.reminder.id, System.currentTimeMillis())
    }

    fun getAllHistory(): Flow<List<DoseHistoryEntity>> = doseHistoryDao.getAllHistory()

    fun getTodayHistory(): Flow<List<DoseHistoryEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return doseHistoryDao.getTodayHistory(calendar.timeInMillis)
    }

    suspend fun getAdherenceStats(days: Int = 7): AdherenceStats {
        val sinceMillis = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val taken = doseHistoryDao.getTakenCountSince(sinceMillis)
        val skipped = doseHistoryDao.getSkippedCountSince(sinceMillis)
        val total = taken + skipped
        val percentage = if (total > 0) ((taken.toFloat() / total) * 100).toInt() else 100

        return AdherenceStats(
            takenCount = taken,
            skippedCount = skipped,
            totalCount = total,
            percentage = percentage
        )
    }
}
