package com.mediremind.app.ui.screens.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.ui.screens.home.RecordDoseDialog
import com.mediremind.app.ui.theme.StockGood
import com.mediremind.app.ui.theme.StockLow
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.MedicationViewModel
import com.mediremind.app.ui.viewmodels.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    medicationViewModel: MedicationViewModel,
    reminderViewModel: ReminderViewModel? = null,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToOpenFda: () -> Unit
) {
    val uiState by medicationViewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    var selectedMedForDose by remember { mutableStateOf<MedicationEntity?>(null) }
    var showRecordDoseDialog by remember { mutableStateOf(false) }

    val displayedList = remember(uiState.medications, searchInput) {
        if (searchInput.isBlank()) {
            uiState.medications
        } else {
            uiState.medications.filter {
                it.name.contains(searchInput, ignoreCase = true) ||
                it.activeCompound.contains(searchInput, ignoreCase = true) ||
                it.presentation.contains(searchInput, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Medicamentos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToOpenFda) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Buscar en OpenFDA",
                            tint = TealPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = TealPrimary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Medicamento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Barra de Búsqueda
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o compuesto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchInput.isNotEmpty()) {
                        IconButton(onClick = { searchInput = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de banner para importar desde base de datos externa
            Card(
                onClick = onNavigateToOpenFda,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = TealPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Catálogo Médico Internacional (OpenFDA)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Text(
                            text = "Busca compuestos químicos y presentaciones oficiales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Medicamentos
            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchInput.isBlank()) "Tu botiquín está vacío" else "No se encontraron coincidencias",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedList, key = { it.id }) { med ->
                        MedicationCardItem(
                            medication = med,
                            onEdit = { onNavigateToEdit(med.id) },
                            onRecordDose = {
                                selectedMedForDose = med
                                showRecordDoseDialog = true
                            },
                            onDelete = { medicationViewModel.deleteMedication(med) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // Espacio para FAB
                    }
                }
            }
        }
    }

    if (showRecordDoseDialog && selectedMedForDose != null) {
        RecordDoseDialog(
            initialReminder = null,
            medications = listOf(selectedMedForDose!!),
            onDismiss = {
                showRecordDoseDialog = false
                selectedMedForDose = null
            },
            onConfirm = { medId, remId, medName, compound, dosage, scheduledMillis, actualMillis, notes ->
                reminderViewModel?.recordManualDose(
                    medicationId = medId,
                    reminderId = remId,
                    medicationName = medName,
                    activeCompound = compound,
                    dosageAmount = dosage,
                    scheduledTimeMillis = scheduledMillis,
                    actualTimeMillis = actualMillis,
                    notes = notes
                )
            }
        )
    }
}

@Composable
private fun MedicationCardItem(
    medication: MedicationEntity,
    onEdit: () -> Unit,
    onRecordDose: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔬 Compuesto: ${medication.activeCompound}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TealPrimary
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Registrar Toma con Hora") },
                            onClick = {
                                showMenu = false
                                onRecordDose()
                            },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = TealPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Etiquetas de presentación, concentración y vía
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChipBadge(text = medication.presentation)
                ChipBadge(text = medication.concentration)
                ChipBadge(text = "Vía ${medication.administrationRoute}")
            }

            if (medication.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 ${medication.instructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Indicador de Stock y botón de Registrar Toma Rápida
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val isLow = medication.currentStock <= medication.lowStockThreshold
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLow) StockLow else StockGood)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLow) "¡Stock Bajo! (${medication.currentStock} ${medication.unit})"
                        else "Disponible: ${medication.currentStock} ${medication.unit}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLow) StockLow else StockGood,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    TextButton(onClick = onRecordDose) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar Toma")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}
