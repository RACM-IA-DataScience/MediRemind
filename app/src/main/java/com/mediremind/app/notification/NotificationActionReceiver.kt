package com.mediremind.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.mediremind.app.data.local.AppDatabase
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gestiona las acciones directas de la notificación (Tomar / Posponer)
 * sin obligar al usuario a abrir la app.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKE = "com.mediremind.app.ACTION_TAKE"
        const val ACTION_SNOOZE = "com.mediremind.app.ACTION_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val medicationId = intent.getLongExtra(NotificationHelper.EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_NAME) ?: "Medicamento"
        val compound = intent.getStringExtra(NotificationHelper.EXTRA_COMPOUND) ?: ""
        val dosage = intent.getStringExtra(NotificationHelper.EXTRA_DOSAGE) ?: "1 dosis"
        val presentation = intent.getStringExtra(NotificationHelper.EXTRA_PRESENTATION) ?: "Dosis"

        if (reminderId == -1L) return

        // Descartar la notificación actual
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(reminderId.toInt())

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)

                when (intent.action) {
                    ACTION_TAKE -> {
                        // 1. Descontar stock
                        if (medicationId != -1L) {
                            db.medicationDao().decrementStock(medicationId, 1)
                        }

                        // 2. Calcular la hora a la que estuvo programada originalmente la alarma
                        val reminder = db.reminderDao().getReminderById(reminderId)
                        val scheduledMillis = if (reminder != null) {
                            java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, reminder.hour)
                                set(java.util.Calendar.MINUTE, reminder.minute)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        } else System.currentTimeMillis()

                        // 3. Registrar en historial de adherencia con hora programada y hora real
                        db.doseHistoryDao().insertHistory(
                            DoseHistoryEntity(
                                medicationId = medicationId,
                                reminderId = reminderId,
                                medicationName = medicationName,
                                activeCompound = compound,
                                dosageTaken = dosage,
                                scheduledTimeMillis = scheduledMillis,
                                actionTimeMillis = System.currentTimeMillis(),
                                status = "TAKEN",
                                notes = "Registrado desde la notificación"
                            )
                        )

                        // 4. Actualizar última acción en el recordatorio
                        db.reminderDao().updateLastAction(reminderId, System.currentTimeMillis())

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "✅ Dosis registrada: $medicationName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    ACTION_SNOOZE -> {
                        // Posponer 10 minutos
                        val snoozeMillis = System.currentTimeMillis() + (10 * 60 * 1000)
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
                            putExtra(NotificationHelper.EXTRA_MEDICATION_ID, medicationId)
                            putExtra(NotificationHelper.EXTRA_MEDICATION_NAME, medicationName)
                            putExtra(NotificationHelper.EXTRA_COMPOUND, compound)
                            putExtra(NotificationHelper.EXTRA_DOSAGE, dosage)
                            putExtra(NotificationHelper.EXTRA_PRESENTATION, presentation)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            reminderId.toInt() + 9999,
                            snoozeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                snoozeMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                snoozeMillis,
                                pendingIntent
                            )
                        }

                        db.doseHistoryDao().insertHistory(
                            DoseHistoryEntity(
                                medicationId = medicationId,
                                reminderId = reminderId,
                                medicationName = medicationName,
                                activeCompound = compound,
                                dosageTaken = dosage,
                                scheduledTimeMillis = System.currentTimeMillis(),
                                actionTimeMillis = System.currentTimeMillis(),
                                status = "SNOOZED",
                                notes = "Pospuesto por 10 minutos"
                            )
                        )

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "⏰ Pospuesto 10 minutos: $medicationName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
