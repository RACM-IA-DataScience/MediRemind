package com.mediremind.app.data.repository

import android.content.Context
import android.location.Location
import com.mediremind.app.data.local.dao.SavedPlaceDao
import com.mediremind.app.data.local.entities.SavedPlaceEntity
import com.mediremind.app.location.GeofenceManager
import com.mediremind.app.location.LocationHelper
import com.mediremind.app.location.PharmacyItem
import kotlinx.coroutines.flow.Flow

class LocationRepository(
    private val context: Context,
    private val savedPlaceDao: SavedPlaceDao,
    private val geofenceManager: GeofenceManager = GeofenceManager(context)
) {

    suspend fun getCurrentLocation(): Location? =
        LocationHelper.getCurrentLocation(context)

    fun getNearbyPharmacies(latitude: Double, longitude: Double): List<PharmacyItem> =
        LocationHelper.getPharmaciesNear(latitude, longitude)

    fun openInGoogleMaps(latitude: Double, longitude: Double) =
        LocationHelper.openGoogleMapsPharmacies(context, latitude, longitude)

    fun getAllPlaces(): Flow<List<SavedPlaceEntity>> =
        savedPlaceDao.getAllPlaces()

    suspend fun savePlace(place: SavedPlaceEntity): Long {
        val id = savedPlaceDao.insertPlace(place)
        val saved = place.copy(id = id)
        if (saved.isEnabled) {
            geofenceManager.registerPlaceGeofence(saved)
        }
        return id
    }

    suspend fun updatePlace(place: SavedPlaceEntity) {
        savedPlaceDao.updatePlace(place)
        if (place.isEnabled) {
            geofenceManager.registerPlaceGeofence(place)
        } else {
            geofenceManager.removePlaceGeofence(place.id)
        }
    }

    suspend fun deletePlace(place: SavedPlaceEntity) {
        geofenceManager.removePlaceGeofence(place.id)
        savedPlaceDao.deletePlace(place)
    }
}
