package com.mediremind.app.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.mediremind.app.data.local.entities.SavedPlaceEntity

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun registerPlaceGeofence(
        place: SavedPlaceEntity,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (!place.isEnabled) return

        var transitionTypes = 0
        if (place.notifyOnEntry) transitionTypes = transitionTypes or Geofence.GEOFENCE_TRANSITION_ENTER
        if (place.notifyOnExit) transitionTypes = transitionTypes or Geofence.GEOFENCE_TRANSITION_EXIT

        if (transitionTypes == 0) return

        val geofence = Geofence.Builder()
            .setRequestId(place.id.toString())
            .setCircularRegion(place.latitude, place.longitude, place.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        try {
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
        } catch (e: SecurityException) {
            onError(e)
        }
    }

    fun removePlaceGeofence(placeId: Long) {
        geofencingClient.removeGeofences(listOf(placeId.toString()))
    }
}
