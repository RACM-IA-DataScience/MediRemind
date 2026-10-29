package com.mediremind.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Define una alarma o recordatorio periódico para la toma de un medicamento.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val hour: Int,                          // Hora del día (0-23)
    val minute: Int,                        // Minuto (0-59)
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Días activos (1 = Lunes, 7 = Domingo)
    val dosageAmount: Double = 1.0,         // Cantidad a tomar en cada dosis (ej. 1 tableta, 10 ml)
    val isEnabled: Boolean = true,          // Alarma activa o pausada
    val snoozeMinutes: Int = 10,            // Minutos para posponer la alarma
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val lastActionTimestamp: Long = 0L      // Última vez que fue atendida
)
