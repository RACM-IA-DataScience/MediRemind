package com.mediremind.app.ui.screens.home

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediremind.app.data.local.dao.ReminderWithMedication
import com.mediremind.app.data.local.entities.MedicationEntity
import com.mediremind.app.ui.theme.PillPending
import com.mediremind.app.ui.theme.PillTaken
import com.mediremind.app.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDoseDialog(
    initialReminder: ReminderWithMedication? = null,
    medications: List<MedicationEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        medicationId: Long,
        reminderId: Long?,
        medicationName: String,
        activeCompound: String,
        dosage: Double,
        scheduledMillis: Long,
        actualMillis: Long,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current

    // Medicamento seleccionado
    var selectedMedication by remember {
        mutableStateOf(
            if (initialReminder != null) {
                medications.find { it.id == initialReminder.reminder.medicationId }
                    ?: MedicationEntity(
                        id = initialReminder.reminder.medicationId,
                        name = initialReminder.medicationName,
                        activeCompound = initialReminder.activeCompound,
                        presentation = initialReminder.presentation,
                        concentration = initialReminder.concentration
                    )
            } else {
                medications.firstOrNull()
            }
        )
    }

    var showMedDropdown by remember { mutableStateOf(false) }

    // Hora programada (si viene de un recordatorio)
    val scheduledCal = remember(initialReminder) {
        if (initialReminder != null) {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, initialReminder.reminder.hour)
                set(Calendar.MINUTE, initialReminder.reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } else null
    }

    // Hora real de la toma
    val nowCalendar = Calendar.getInstance()
    var actualHour by remember { mutableIntStateOf(nowCalendar.get(Calendar.HOUR_OF_DAY)) }
    var actualMinute by remember { mutableIntStateOf(nowCalendar.get(Calendar.MINUTE)) }
    var notes by remember { mutableStateOf("") }
    var dosageAmountStr by remember {
        mutableStateOf(initialReminder?.reminder?.dosageAmount?.toString() ?: "1.0")
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                actualHour = hourOfDay
                actualMinute = minute
            },
            actualHour,
            actualMinute,
            false
        )
    }

    // Cálculo del tiempo real en milisegundos
    val actualMillis = remember(actualHour, actualMinute) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, actualHour)
            set(Calendar.MINUTE, actualMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val scheduledMillis = scheduledCal?.timeInMillis ?: 0L

    val formattedActualTime = remember(actualHour, actualMinute) {
        val amPm = if (actualHour >= 12) "PM" else "AM"
        val h12 = if (actualHour % 12 == 0) 12 else actualHour % 12
        val m = String.format(Locale.US, "%02d", actualMinute)
        "$h12:$m $amPm"
    }

    val formattedScheduledTime = remember(scheduledCal) {
        if (scheduledCal != null) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(scheduledCal.time)
        } else "Sin alarma fija"
    }

    // Retraso calculado
    val delayMinutes = remember(actualMillis, scheduledMillis) {
        if (scheduledMillis > 0L) {
            (actualMillis - scheduledMillis) / (60 * 1000L)
        } else 0L
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialReminder != null) "Registrar Toma de Alarma" else "Registrar Toma de Medicamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Registra la hora exacta en la que tomaste el medicamento, incluso si la alarma sonó antes y lo tomaste después.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Selector de Medicamento (si no viene fijo de un recordatorio)
                if (initialReminder == null && medications.isNotEmpty()) {
                    Text(
                        text = "Seleccionar Medicamento:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box {
                        OutlinedCard(
                            onClick = { showMedDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selectedMedication?.name ?: "Seleccionar",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = selectedMedication?.activeCompound ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Text("Cambiar", color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        DropdownMenu(
                            expanded = showMedDropdown,
                            onDismissRequest = { showMedDropdown = false }
                        ) {
                            medications.forEach { med ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(med.name, fontWeight = FontWeight.Bold)
                                            Text("${med.activeCompound} (${med.presentation})", fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedMedication = med
                                        showMedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Tarjeta del medicamento fijo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = selectedMedication?.name ?: initialReminder?.medicationName ?: "Medicamento",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Compuesto: ${selectedMedication?.activeCompound ?: initialReminder?.activeCompound ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TealPrimary
                            )
                        }
                    }
                }

                // Comparativa de Horas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (scheduledMillis > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("⏰ Alarma programada:", style = MaterialTheme.typography.bodySmall)
                                Text(formattedScheduledTime, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("💊 Hora real de la toma:", style = MaterialTheme.typography.bodySmall)
                            Text(formattedActualTime, fontWeight = FontWeight.Bold, color = TealPrimary, style = MaterialTheme.typography.bodyMedium)
                        }

                        // Indicador de retraso si aplica
                        if (scheduledMillis > 0L && delayMinutes > 5) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val h = delayMinutes / 60
                            val m = delayMinutes % 60
                            val delayStr = if (h > 0) "${h}h ${m}m" else "${m} minutos"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PillPending.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "⚠️ Tomada con $delayStr de retraso",
                                    color = PillPending,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else if (scheduledMillis > 0L) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PillTaken.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✅ Toma a tiempo",
                                    color = PillTaken,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Ajustes rápidos de hora
                Text(
                    text = "Ajustar hora exacta en que lo tomaste:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            val now = Calendar.getInstance()
                            actualHour = now.get(Calendar.HOUR_OF_DAY)
                            actualMinute = now.get(Calendar.MINUTE)
                        },
                        label = { Text("Justo ahora", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, -15) }
                            actualHour = cal.get(Calendar.HOUR_OF_DAY)
                            actualMinute = cal.get(Calendar.MINUTE)
                        },
                        label = { Text("-15 min", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, -30) }
                            actualHour = cal.get(Calendar.HOUR_OF_DAY)
                            actualMinute = cal.get(Calendar.MINUTE)
                        },
                        label = { Text("-30 min", fontSize = 11.sp) }
                    )
                }

                // Botón para selector de hora personalizada
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir otra hora específica ($formattedActualTime)")
                }

                // Notas opcionales
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas opcionales") },
                    placeholder = { Text("Ej. Tomada con comida, retraso por tráfico...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val med = selectedMedication
                    if (med != null) {
                        val dose = dosageAmountStr.toDoubleOrNull() ?: 1.0
                        onConfirm(
                            med.id,
                            initialReminder?.reminder?.id,
                            med.name,
                            med.activeCompound,
                            dose,
                            scheduledMillis,
                            actualMillis,
                            notes
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirmar Toma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
