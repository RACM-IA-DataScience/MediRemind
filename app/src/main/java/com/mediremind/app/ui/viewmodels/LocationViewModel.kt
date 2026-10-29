package com.mediremind.app.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediremind.app.data.local.entities.SavedPlaceEntity
import com.mediremind.app.data.repository.LocationRepository
import com.mediremind.app.location.PharmacyItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LocationUiState(
    val userLocation: Location? = null,
    val pharmacies: List<PharmacyItem> = emptyList(),
    val savedPlaces: List<SavedPlaceEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllPlaces().collect { places ->
                _uiState.update { it.copy(savedPlaces = places) }
            }
        }
    }

    fun loadUserLocationAndPharmacies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val location = repository.getCurrentLocation()
            if (location != null) {
                val pharmacies = repository.getNearbyPharmacies(location.latitude, location.longitude)
                _uiState.update {
                    it.copy(
                        userLocation = location,
                        pharmacies = pharmacies,
                        isLoading = false
                    )
                }
            } else {
                // Si aún no hay GPS fijado, usar ubicación de muestra (ej. Ciudad de México Centro)
                val defaultLat = 19.432608
                val defaultLon = -99.133209
                val samplePharmacies = repository.getNearbyPharmacies(defaultLat, defaultLon)
                _uiState.update {
                    it.copy(
                        pharmacies = samplePharmacies,
                        isLoading = false,
                        errorMessage = "No se pudo obtener señal GPS exacta. Mostrando farmacias referenciales."
                    )
                }
            }
        }
    }

    fun openInGoogleMaps() {
        val loc = _uiState.value.userLocation
        val lat = loc?.latitude ?: 19.432608
        val lon = loc?.longitude ?: -99.133209
        repository.openInGoogleMaps(lat, lon)
    }

    fun savePlace(place: SavedPlaceEntity) {
        viewModelScope.launch {
            if (place.id == 0L) {
                repository.savePlace(place)
            } else {
                repository.updatePlace(place)
            }
        }
    }

    fun deletePlace(place: SavedPlaceEntity) {
        viewModelScope.launch {
            repository.deletePlace(place)
        }
    }
}
