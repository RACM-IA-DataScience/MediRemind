package com.mediremind.app.ui.screens.medications

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.data.local.entities.ReminderEntity
import com.mediremind.app.ui.theme.TealPrimary
import com.mediremind.app.ui.viewmodels.MedicationViewModel
import com.mediremind.app.ui.viewmodels.ReminderViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicationScreen(
    medicationId: Long,
    medicationViewModel: MedicationViewModel,
    reminderViewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by medicationViewModel.uiState.collectAsState()

    val existingMed = remember(medicationId, uiState.medications) {
        if (medicationId != 0L) {
            uiState.medications.find { it.id == medicationId }
        } else null
    }

    var name by remember(existingMed) { mutableStateOf(existingMed?.name ?: "") }
    var compound by remember(existingMed) { mutableStateOf(existingMed?.activeCompound ?: "") }
    var presentation by remember(existingMed) { mutableStateOf(existingMed?.presentation ?: "Tabletas") }
    var concentration by remember(existingMed) { mutableStateOf(existingMed?.concentration ?: "500 mg") }
    var route by remember(existingMed) { mutableStateOf(existingMed?.administrationRoute ?: "Oral") }
    var instructions by remember(existingMed) { mutableStateOf(existingMed?.instructions ?: "") }
    var stockStr by remember(existingMed) { mutableStateOf((existingMed?.currentStock ?: 30).toString()) }
    var unit by remember(existingMed) { mutableStateOf(existingMed?.unit ?: "pastillas") }

    // Estado del recordatorio inicial
    var scheduleAlarm by remember { mutableStateOf(true) }
    var alarmHour by remember { mutableIntStateOf(8) }
    var alarmMinute by remember { mutableIntStateOf(0) }
    var doseAmountStr by remember { mutableStateOf("1.0") }

    val presentationOptions = listOf(
        "Tabletas", "Cápsulas", "Jarabe", "Gotas", "Inhalador", "Inyección", "Crema / Pomada", "Solución"
    )

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                alarmHour = hourOfDay
                alarmMinute = minute
            },
            alarmHour,
            alarmMinute,
            false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medicationId == 0L) "Nuevo Medicamento" else "Editar Medicamento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank() && compound.isNotBlank()) {
                                val stock = stockStr.toIntOrNull() ?: 30
                                val medToSave = (existingMed ?: MedicationEntity(
                                    name = name,
                                    activeCompound = compound,
                                    presentation = presentation,
                                    concentration = concentration,
                                    administrationRoute = route,
                                    instructions = instructions,
                                    currentStock = stock,
                                    unit = unit,
                                    isCustom = true
                                )).copy(
                                    name = name,
                                    activeCompound = compound,
                                    presentation = presentation,
                                    concentration = concentration,
                                    administrationRoute = route,
                                    instructions = instructions,
                                    currentStock = stock,
                                    unit = unit
                                )

                                medicationViewModel.saveMedication(medToSave) {
                                    if (medicationId == 0L && scheduleAlarm) {
                                        // Si es nuevo y se pidió alarma, programarla
                                        val dose = doseAmountStr.toDoubleOrNull() ?: 1.0
                                        reminderViewModel.saveReminder(
                                            ReminderEntity(
                                                medicationId = medToSave.id,
                                                hour = alarmHour,
                                                minute = alarmMinute,
                                                dosageAmount = dose,
                                                isEnabled = true
                                            )
                                        )
                                    }
                                    onNavigateBack()
                                }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar", tint = TealPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección 1: Información Farmacológica
            Text(
                text = "Detalles del Compuesto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre Comercial / Referencia *") },
                placeholder = { Text("Ej. Tempra, Motrin, Amoxil") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = compound,
                onValueChange = { compound = it },
                label = { Text("Principio Activo (Compuesto Químico) *") },
                placeholder = { Text("Ej. Paracetamol, Ibuprofeno, Amoxicilina") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = concentration,
                onValueChange = { concentration = it },
                label = { Text("Concentración / Dosificación") },
                placeholder = { Text("Ej. 500 mg, 100 mg / 5 ml, 50 mcg") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Presentación
            Text(
                text = "Forma Farmacéutica (Presentación):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presentationOptions.forEach { opt ->
                    FilterChip(
                        selected = presentation == opt,
                        onClick = { presentation = opt },
                        label = { Text(opt) }
                    )
                }
            }

            OutlinedTextField(
                value = route,
                onValueChange = { route = it },
                label = { Text("Vía de Administración") },
                placeholder = { Text("Oral, Tópica, Inhalatoria, etc.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Indicaciones y Advertencias") },
                placeholder = { Text("Ej. Tomar después de comer con abundante agua") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Divider()

            // Sección 2: Inventario / Stock
            Text(
                text = "Control de Inventario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock Actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad") },
                    placeholder = { Text("pastillas, ml, etc.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Divider()

            // Sección 3: Programación de Alarma (Si es nuevo)
            if (medicationId == 0L) {
                Text(
                    text = "Configuración de Alarma Inicial",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activar recordatorio periódico")
                    Switch(
                        checked = scheduleAlarm,
                        onCheckedChange = { scheduleAlarm = it }
                    )
                }

                if (scheduleAlarm) {
                    val formattedTime = remember(alarmHour, alarmMinute) {
                        val amPm = if (alarmHour >= 12) "PM" else "AM"
                        val h12 = if (alarmHour % 12 == 0) 12 else alarmHour % 12
                        val m = String.format(Locale.US, "%02d", alarmMinute)
                        "$h12:$m $amPm"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timePickerDialog.show() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = TealPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Hora del Recordatorio", style = MaterialTheme.typography.bodySmall)
                                    Text(formattedTime, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Cambiar", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedTextField(
                        value = doseAmountStr,
                        onValueChange = { doseAmountStr = it },
                        label = { Text("Cantidad por dosis") },
                        placeholder = { Text("1.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && compound.isNotBlank()) {
                        val stock = stockStr.toIntOrNull() ?: 30
                        val medToSave = (existingMed ?: MedicationEntity(
                            name = name,
                            activeCompound = compound,
                            presentation = presentation,
                            concentration = concentration,
                            administrationRoute = route,
                            instructions = instructions,
                            currentStock = stock,
                            unit = unit,
                            isCustom = true
                        )).copy(
                            name = name,
                            activeCompound = compound,
                            presentation = presentation,
                            concentration = concentration,
                            administrationRoute = route,
                            instructions = instructions,
                            currentStock = stock,
                            unit = unit
                        )

                        medicationViewModel.saveMedication(medToSave) {
                            if (medicationId == 0L && scheduleAlarm) {
                                val dose = doseAmountStr.toDoubleOrNull() ?: 1.0
                                reminderViewModel.saveReminder(
                                    ReminderEntity(
                                        medicationId = medToSave.id,
                                        hour = alarmHour,
                                        minute = alarmMinute,
                                        dosageAmount = dose,
                                        isEnabled = true
                                    )
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Guardar Medicamento", fontWeight = FontWeight.Bold)
            }
        }
    }
}
