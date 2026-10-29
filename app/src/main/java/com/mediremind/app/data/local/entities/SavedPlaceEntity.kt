package com.mediremind.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un lugar de interés para recordatorios geolocalizados (Casa, Trabajo, Escuela).
 */
@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                  // Ej. "Casa", "Oficina"
    val address: String = "",          // Dirección legible
    val latitude: Double,              // Latitud GPS
    val longitude: Double,             // Longitud GPS
    val radiusMeters: Float = 150f,    // Radio de la geovalla en metros
    val notifyOnEntry: Boolean = true, // Notificar al entrar al lugar
    val notifyOnExit: Boolean = false, // Notificar al salir del lugar
    val isEnabled: Boolean = true
)
