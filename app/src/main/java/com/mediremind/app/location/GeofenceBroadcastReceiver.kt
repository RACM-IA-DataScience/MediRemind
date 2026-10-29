package com.mediremind.app.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.mediremind.app.data.local.AppDatabase
import com.mediremind.app.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receptor disparado por el sistema de Geofencing de Google Play Services
 * al cruzar los límites perimetrales de Casa o Trabajo.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            val placeId = geofence.requestId.toLongOrNull() ?: continue

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val place = db.savedPlaceDao().getPlaceById(placeId) ?: return@launch

                    val reminders = db.reminderDao().getAllActiveReminders().first()
                    val totalActive = reminders.size

                    val message = when (geofenceTransition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            "Has llegado a ${place.name}. Tienes $totalActive recordatorios configurados. ¡Verifica si tienes dosis pendientes!"
                        }
                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            "Estás saliendo de ${place.name}. No olvides llevar contigo tus medicamentos prescritos."
                        }
                        else -> null
                    }

                    if (message != null) {
                        NotificationHelper.showGeofenceNotification(context, place.name, message)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
