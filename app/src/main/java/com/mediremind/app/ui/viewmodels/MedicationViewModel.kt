package com.mediremind.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.data.repository.MedicationRepository
import com.mediremind.app.data.repository.OnlineDrugItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MedicationUiState(
    val medications: List<MedicationEntity> = emptyList(),
    val lowStockMeds: List<MedicationEntity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<MedicationEntity> = emptyList(),
    val onlineResults: List<OnlineDrugItem> = emptyList(),
    val isSearchingOnline: Boolean = false,
    val onlineSearchError: String? = null
)

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    private var onlineSearchJob: Job? = null

    init {
        // Observar medicamentos locales
        viewModelScope.launch {
            repository.getAllMedications().collect { list ->
                _uiState.update { it.copy(medications = list) }
            }
        }
        // Observar bajo stock
        viewModelScope.launch {
            repository.getLowStockMedications().collect { lowList ->
                _uiState.update { it.copy(lowStockMeds = lowList) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), onlineResults = emptyList()) }
            return
        }

        // Búsqueda local inmediata
        viewModelScope.launch {
            repository.searchLocalMedications(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }

        // Búsqueda en API de OpenFDA con debounce de 500ms
        onlineSearchJob?.cancel()
        onlineSearchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isSearchingOnline = true, onlineSearchError = null) }
            val result = repository.searchOnlineDrugs(query)
            result.fold(
                onSuccess = { items ->
                    _uiState.update { it.copy(onlineResults = items, isSearchingOnline = false) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSearchingOnline = false,
                            onlineSearchError = err.message
                        )
                    }
                }
            )
        }
    }

    fun saveMedication(medication: MedicationEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (medication.id == 0L) {
                repository.insertMedication(medication)
            } else {
                repository.updateMedication(medication)
            }
            onComplete()
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun updateStock(medicationId: Long, newStock: Int) {
        viewModelScope.launch {
            val med = repository.getMedicationById(medicationId) ?: return@launch
            repository.updateMedication(med.copy(currentStock = newStock))
        }
    }
}
