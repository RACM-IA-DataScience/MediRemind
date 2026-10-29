package com.mediremind.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediremind.app.data.local.dao.ReminderWithMedication
import com.mediremind.app.data.local.entities.DoseHistoryEntity
import com.mediremind.app.data.local.entities.ReminderEntity
import com.mediremind.app.data.repository.AdherenceStats
import com.mediremind.app.data.repository.ReminderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReminderUiState(
    val reminders: List<ReminderWithMedication> = emptyList(),
    val todayHistory: List<DoseHistoryEntity> = emptyList(),
    val fullHistory: List<DoseHistoryEntity> = emptyList(),
    val stats: AdherenceStats = AdherenceStats(0, 0, 0, 100),
    val isLoading: Boolean = false
)

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        // Observar recordatorios activos
        viewModelScope.launch {
            repository.getActiveReminders().collect { list ->
                _uiState.update { it.copy(reminders = list) }
            }
        }
        // Observar historial de hoy filtrado por el usuario actual
        viewModelScope.launch {
            repository.getTodayHistory().collect { history ->
                _uiState.update { it.copy(todayHistory = history) }
            }
        }
        // Observar historial completo filtrado por el usuario actual
        viewModelScope.launch {
            repository.getAllHistory().collect { full ->
                _uiState.update { it.copy(fullHistory = full) }
                refreshStats()
            }
        }
    }

    fun saveReminder(reminder: ReminderEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveReminder(reminder)
            onComplete()
        }
    }

    fun toggleReminder(reminderId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(reminderId, isEnabled)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    /**
     * Registra una dosis tomada a partir de una alarma programada, permitiendo
     * fijar la hora real y notas opcionales.
     */
    fun markDoseTaken(
        item: ReminderWithMedication,
        actualTimeMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.recordDoseTaken(item, actualTimeMillis, notes)
            refreshStats()
        }
    }

    /**
     * Registra una toma libre o manual indicando la hora exacta en que se tomó el medicamento.
     */
    fun recordManualDose(
        medicationId: Long,
        reminderId: Long? = null,
        medicationName: String,
        activeCompound: String,
        dosageAmount: Double = 1.0,
        scheduledTimeMillis: Long = 0L,
        actualTimeMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.recordManualDoseTaken(
                medicationId = medicationId,
                reminderId = reminderId,
                medicationName = medicationName,
                activeCompound = activeCompound,
                dosageAmount = dosageAmount,
                scheduledTimeMillis = scheduledTimeMillis,
                actualTimeMillis = actualTimeMillis,
                notes = notes
            )
            refreshStats()
        }
    }

    /**
     * Permite eliminar una toma del historial por equivocación o corrección del paciente.
     * Si restoreStock es verdadero, reintegra la dosis tomada al inventario disponible.
     */
    fun deleteHistoryEntry(history: DoseHistoryEntity, restoreStock: Boolean = true) {
        viewModelScope.launch {
            repository.deleteHistoryEntry(history, restoreStock)
            refreshStats()
        }
    }

    fun markDoseSkipped(item: ReminderWithMedication, reason: String = "") {
        viewModelScope.launch {
            repository.recordDoseSkipped(item, reason)
            refreshStats()
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            val stats = repository.getAdherenceStats(days = 7)
            _uiState.update { it.copy(stats = stats) }
        }
    }
}
