package com.mediremind.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Registro de auditoría y adherencia para cada toma de medicamento.
 * Registra tanto la hora programada originalmente como la hora real en que se tomó el medicamento,
 * encapsulado de manera estricta por el identificador de usuario (userId).
 */
@Entity(
    tableName = "dose_history",
    indices = [Index(value = ["userId"])]
)
data class DoseHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "guest_local",        // ID único del usuario (ej. 'guest_local', 'google_user_...')
    val medicationId: Long,
    val reminderId: Long? = null,
    val medicationName: String,
    val activeCompound: String,
    val dosageTaken: String,
    val scheduledTimeMillis: Long,              // Hora en que sonó / debía tomarse la alarma
    val actionTimeMillis: Long = System.currentTimeMillis(), // Hora real y efectiva en que se ingirió
    val status: String,                         // "TAKEN" (Tomado), "SKIPPED" (Omitido), "SNOOZED" (Pospuesto)
    val notes: String = ""
) {
    /**
     * Calcula y formatea la diferencia de tiempo entre la alarma programada y la toma real.
     */
    fun getDelayFormatted(): String? {
        if (status != "TAKEN") return null
        if (scheduledTimeMillis <= 0L) return "Toma fuera de horario"

        val diffMillis = actionTimeMillis - scheduledTimeMillis
        val diffMinutes = diffMillis / (60 * 1000L)

        return when {
            diffMinutes > 10 -> {
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                if (hours > 0) "+${hours}h ${mins}m de retraso" else "+${mins}m de retraso"
            }
            diffMinutes < -10 -> {
                val mins = -diffMinutes
                "${mins}m antes"
            }
            else -> "A tiempo"
        }
    }

    fun isDelayed(): Boolean {
        if (status != "TAKEN" || scheduledTimeMillis <= 0L) return false
        val diffMinutes = (actionTimeMillis - scheduledTimeMillis) / (60 * 1000L)
        return diffMinutes > 15
    }

    fun getFormattedScheduledTime(): String {
        if (scheduledTimeMillis <= 0L) return "Sin hora programada"
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(scheduledTimeMillis))
    }

    fun getFormattedActionTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(actionTimeMillis))
    }
}
