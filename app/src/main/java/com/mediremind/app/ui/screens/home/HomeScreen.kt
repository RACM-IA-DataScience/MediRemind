package com.mediremind.app.ui.screens.home

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
import androidx.compose.ui.unit.sp
import com.mediremind.app.data.local.dao.ReminderWithMedication
import com.mediremind.app.ui.theme.*
import com.mediremind.app.ui.viewmodels.AuthViewModel
import com.mediremind.app.ui.viewmodels.MedicationViewModel
import com.mediremind.app.ui.viewmodels.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    medicationViewModel: MedicationViewModel,
    reminderViewModel: ReminderViewModel,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToPharmacies: () -> Unit,
    onNavigateToPlaces: () -> Unit,
    onNavigateToDrugDatabase: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val medUiState by medicationViewModel.uiState.collectAsState()
    val reminderUiState by reminderViewModel.uiState.collectAsState()

    var showRecordDoseDialog by remember { mutableStateOf(false) }
    var selectedReminderForDialog by remember { mutableStateOf<ReminderWithMedication?>(null) }

    val todayDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hola, ${currentUser?.displayName ?: "Usuario"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = todayDateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tarjeta de Adherencia y Resumen de Salud
            item {
                AdherenceCard(
                    adherencePercentage = reminderUiState.stats.percentage,
                    takenCount = reminderUiState.stats.takenCount,
                    totalCount = reminderUiState.stats.totalCount
                )
            }

            // 2. Alerta de Stock Bajo (Si aplica)
            if (medUiState.lowStockMeds.isNotEmpty()) {
                item {
                    LowStockAlertBanner(
                        lowStockCount = medUiState.lowStockMeds.size,
                        onFindPharmacies = onNavigateToPharmacies
                    )
                }
            }

            // 3. Accesos Rápidos (incluye botón directo para Registrar Toma)
            item {
                QuickActionsRow(
                    onRecordDose = {
                        selectedReminderForDialog = null
                        showRecordDoseDialog = true
                    },
                    onAddMed = onNavigateToAddMedication,
                    onPharmacies = onNavigateToPharmacies,
                    onPlaces = onNavigateToPlaces,
                    onCatalog = onNavigateToDrugDatabase
                )
            }

            // 4. Encabezado de Dosis de Hoy
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Dosis Programadas para Hoy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${reminderUiState.reminders.size} recordatorios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // 5. Lista de Dosis Programadas
            if (reminderUiState.reminders.isEmpty()) {
                item {
                    EmptyRemindersCard(onAddMed = onNavigateToAddMedication)
                }
            } else {
                items(reminderUiState.reminders) { item ->
                    DoseScheduleItem(
                        item = item,
                        onTakeNow = { reminderViewModel.markDoseTaken(item) },
                        onTakeCustomTime = {
                            selectedReminderForDialog = item
                            showRecordDoseDialog = true
                        },
                        onSkip = { reminderViewModel.markDoseSkipped(item, "Omitido por el usuario") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Diálogo para Registrar Toma (con hora personalizada y retraso)
    if (showRecordDoseDialog) {
        RecordDoseDialog(
            initialReminder = selectedReminderForDialog,
            medications = medUiState.medications,
            onDismiss = {
                showRecordDoseDialog = false
                selectedReminderForDialog = null
            },
            onConfirm = { medId, remId, medName, compound, dosage, scheduledMillis, actualMillis, notes ->
                reminderViewModel.recordManualDose(
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
private fun AdherenceCard(adherencePercentage: Int, takenCount: Int, totalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TealPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Adherencia Semanal",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$takenCount de $totalCount dosis completadas a tiempo",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "$adherencePercentage%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LowStockAlertBanner(lowStockCount: Int, onFindPharmacies: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StockLow.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StockLow,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Stock Bajo Detectado",
                    style = MaterialTheme.typography.titleSmall,
                    color = StockLow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tienes $lowStockCount medicamento(s) a punto de agotarse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(onClick = onFindPharmacies) {
                Text("Comprar", color = StockLow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onRecordDose: () -> Unit,
    onAddMed: () -> Unit,
    onPharmacies: () -> Unit,
    onPlaces: () -> Unit,
    onCatalog: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.CheckCircleOutline,
            label = "Registrar",
            modifier = Modifier.weight(1f),
            isHighlighted = true,
            onClick = onRecordDose
        )
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "Nuevo",
            modifier = Modifier.weight(1f),
            onClick = onAddMed
        )
        QuickActionButton(
            icon = Icons.Default.LocalPharmacy,
            label = "Farmacias",
            modifier = Modifier.weight(1f),
            onClick = onPharmacies
        )
        QuickActionButton(
            icon = Icons.Default.LocationOn,
            label = "Geovalla",
            modifier = Modifier.weight(1f),
            onClick = onPlaces
        )
        QuickActionButton(
            icon = Icons.Default.Search,
            label = "OpenFDA",
            modifier = Modifier.weight(1f),
            onClick = onCatalog
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) TealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlighted) TealPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlighted) TealPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DoseScheduleItem(
    item: ReminderWithMedication,
    onTakeNow: () -> Unit,
    onTakeCustomTime: () -> Unit,
    onSkip: () -> Unit
) {
    val formattedTime = remember(item.reminder.hour, item.reminder.minute) {
        val h = item.reminder.hour
        val m = String.format(Locale.US, "%02d", item.reminder.minute)
        val amPm = if (h >= 12) "PM" else "AM"
        val h12 = if (h % 12 == 0) 12 else h % 12
        "$h12:$m $amPm"
    }

    // Comprobar si la hora de la alarma ya pasó hoy
    val isPastDue = remember(item.reminder.hour, item.reminder.minute) {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val alarmMinutes = item.reminder.hour * 60 + item.reminder.minute
        currentMinutes > alarmMinutes
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isPastDue) PillPending else TealPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPastDue) PillPending else TealPrimary
                    )
                    if (isPastDue) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PillPending.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Alarma sonó",
                                color = PillPending,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Indicador de Stock
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.currentStock <= 5) StockLow.copy(alpha = 0.15f) else StockGood.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Stock: ${item.currentStock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.currentStock <= 5) StockLow else StockGood,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.medicationName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Compuesto: ${item.activeCompound}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )

            Text(
                text = "Presentación: ${item.presentation} (${item.concentration}) • Dosis: ${item.reminder.dosageAmount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de acción directa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Tomar Ahora
                Button(
                    onClick = onTakeNow,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PillTaken),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tomar Ya", fontSize = 13.sp)
                }

                // Botón Registrar con Hora (clave para registrar si se tomó después o con retraso)
                OutlinedButton(
                    onClick = onTakeCustomTime,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Poner Hora", fontSize = 13.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                }

                // Botón Omitir
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Omitir", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun EmptyRemindersCard(onAddMed: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AlarmOn,
                contentDescription = null,
                tint = TealPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No tienes alarmas pendientes hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Registra un medicamento o programa un recordatorio para comenzar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddMed,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Programar Medicamento")
            }
        }
    }
}
