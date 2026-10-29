package com.mediremind.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mediremind.app.MainActivity
import com.mediremind.app.R
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.data.local.entities.ReminderEntity
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_REMINDERS_ID = "channel_medication_reminders"
    const val CHANNEL_GEOFENCE_ID = "channel_geofence_alerts"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_MEDICATION_ID = "extra_medication_id"
    const val EXTRA_MEDICATION_NAME = "extra_medication_name"
    const val EXTRA_COMPOUND = "extra_compound"
    const val EXTRA_DOSAGE = "extra_dosage"
    const val EXTRA_PRESENTATION = "extra_presentation"

    /**
     * Crea los canales de notificación requeridos para Android 8.0 (API 26) o superior.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // Canal 1: Recordatorios de Medicamentos (Alta Importancia)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                context.getString(R.string.notification_channel_reminders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmSound, audioAttributes)
            }

            // Canal 2: Alertas de Geovalla (Casa / Trabajo)
            val geofenceChannel = NotificationChannel(
                CHANNEL_GEOFENCE_ID,
                context.getString(R.string.notification_channel_geofence_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_geofence_desc)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(geofenceChannel)
        }
    }

    /**
     * Muestra la notificación de alarma con acciones rápidas: "Tomar" y "Posponer".
     */
    fun showMedicationNotification(
        context: Context,
        reminderId: Long,
        medicationId: Long,
        medicationName: String,
        compound: String,
        dosage: String,
        presentation: String
    ) {
        val notificationId = reminderId.toInt()

        // Intent para abrir la aplicación al tocar la notificación
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_MEDICATION_ID, medicationId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción: TOMAR
        val takeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_COMPOUND, compound)
            putExtra(EXTRA_DOSAGE, dosage)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId * 10) + 1,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción: POSPONER 10 MIN
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_COMPOUND, compound)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_PRESENTATION, presentation)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId * 10) + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 Hora de tomar: $medicationName")
            .setContentText("Dosis: $dosage ($presentation) • $compound")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Es momento de tu dosis de $medicationName.\nPrincipio activo: $compound\nPresentación: $presentation\nCantidad: $dosage")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_agenda, "✅ Tomar", takePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "⏰ Posponer 10m", snoozePendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permiso POST_NOTIFICATIONS no concedido aún
        }
    }

    /**
     * Muestra alerta cuando el usuario entra o sale de su casa u oficina con medicamentos pendientes.
     */
    fun showGeofenceNotification(context: Context, placeName: String, alertMessage: String) {
        val notificationId = (placeName.hashCode() and 0xFFFF) + 50000

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_GEOFENCE_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("📍 Recordatorio en $placeName")
            .setContentText(alertMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alertMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    /**
     * Programa la alarma exacta mediante AlarmManager.
     */
    fun scheduleExactAlarm(
        context: Context,
        reminder: ReminderEntity,
        medication: MedicationEntity
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerMillis = calculateNextTriggerTime(reminder.hour, reminder.minute)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_MEDICATION_NAME, medication.name)
            putExtra(EXTRA_COMPOUND, medication.activeCompound)
            putExtra(EXTRA_DOSAGE, "${reminder.dosageAmount} ${medication.unit}")
            putExtra(EXTRA_PRESENTATION, medication.presentation)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancela una alarma activa.
     */
    fun cancelAlarm(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Calcula los milisegundos de la próxima ejecución (si ya pasó hoy, se programa para mañana).
     */
    fun calculateNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }
}
