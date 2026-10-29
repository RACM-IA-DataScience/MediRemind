package com.mediremind.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mediremind.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Se ejecuta automáticamente al reiniciar o encender el dispositivo (BOOT_COMPLETED).
 * Recupera todos los recordatorios activos de la base de datos y vuelve a programar
 * sus alarmas en AlarmManager para que el usuario no pierda ninguna toma.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeReminders = db.reminderDao().getAllActiveReminders().first()

                    for (reminder in activeReminders) {
                        val medication = db.medicationDao().getMedicationById(reminder.medicationId)
                        if (medication != null) {
                            NotificationHelper.scheduleExactAlarm(context, reminder, medication)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
