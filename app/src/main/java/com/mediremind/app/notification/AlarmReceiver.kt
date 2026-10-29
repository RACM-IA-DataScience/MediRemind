package com.mediremind.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mediremind.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receptor que captura la alarma exacta emitida por AlarmManager.
 * Muestra la notificación de alta prioridad y reprograma el siguiente ciclo.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val medicationId = intent.getLongExtra(NotificationHelper.EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_NAME) ?: "Medicamento"
        val compound = intent.getStringExtra(NotificationHelper.EXTRA_COMPOUND) ?: "Compuesto"
        val dosage = intent.getStringExtra(NotificationHelper.EXTRA_DOSAGE) ?: "1 dosis"
        val presentation = intent.getStringExtra(NotificationHelper.EXTRA_PRESENTATION) ?: "Dosis"

        if (reminderId == -1L) return

        // 1. Mostrar la notificación interactiva
        NotificationHelper.showMedicationNotification(
            context = context,
            reminderId = reminderId,
            medicationId = medicationId,
            medicationName = medicationName,
            compound = compound,
            dosage = dosage,
            presentation = presentation
        )

        // 2. Reprogramar para el siguiente día si el recordatorio sigue activo
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminder = db.reminderDao().getReminderById(reminderId)
                val medication = if (medicationId != -1L) db.medicationDao().getMedicationById(medicationId) else null

                if (reminder != null && reminder.isEnabled && medication != null) {
                    NotificationHelper.scheduleExactAlarm(context, reminder, medication)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
