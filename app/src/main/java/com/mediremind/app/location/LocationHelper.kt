package com.mediremind.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.*

data class PharmacyItem(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float,
    val isOpen24Hours: Boolean,
    val phone: String
)

object LocationHelper {

    /**
     * Obtiene la ubicación GPS precisa actual utilizando FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        return try {
            val cancellationToken = CancellationTokenSource()
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Genera una lista de farmacias de muestra calibradas alrededor de las coordenadas del usuario
     * con cálculo exacto de distancia geodésica (Haversine).
     */
    fun getPharmaciesNear(userLat: Double, userLon: Double): List<PharmacyItem> {
        val samplePharmacies = listOf(
            Triple("Farmacia San Pablo (24 Horas)", 0.0035, 0.0022),
            Triple("Farmacias del Ahorro - Sucursal Centro", -0.0028, 0.0041),
            Triple("Farmacias Benavides", 0.0051, -0.0038),
            Triple("Farmacia Guadalajara - Superfarmacia", -0.0042, -0.0029),
            Triple("Farmacia Similares - Consultorio y Medicamentos", 0.0018, -0.0015),
            Triple("Farmacia París - Especialidades y Fórmulas", -0.0065, 0.0055)
        )

        return samplePharmacies.mapIndexed { index, (name, dLat, dLon) ->
            val pLat = userLat + dLat
            val pLon = userLon + dLon
            val dist = calculateDistance(userLat, userLon, pLat, pLon)
            PharmacyItem(
                name = name,
                address = "Aprox. ${formatDistance(dist)} de tu ubicación",
                latitude = pLat,
                longitude = pLon,
                distanceMeters = dist,
                isOpen24Hours = index % 2 == 0,
                phone = "800-123-456$index"
            )
        }.sortedBy { it.distanceMeters }
    }

    /**
     * Abre Google Maps buscando farmacias cercanas a la posición del usuario.
     */
    fun openGoogleMapsPharmacies(context: Context, latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=farmacias+cercanas")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Abrir en navegador si no tiene app de mapas instalada
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/farmacias/@$latitude,$longitude,15z")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Fórmula de Haversine para distancia en metros entre dos puntos geográficos.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000.0 // metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    fun formatDistance(meters: Float): String {
        return if (meters >= 1000) {
            String.format(java.util.Locale.US, "%.1f km", meters / 1000f)
        } else {
            "${meters.toInt()} m"
        }
    }
}
